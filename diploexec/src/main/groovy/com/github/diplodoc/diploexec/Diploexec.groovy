package com.github.diplodoc.diploexec

import com.github.diplodoc.diplobase.domain.jpa.diploexec.Process
import com.github.diplodoc.diplobase.domain.jpa.diploexec.ProcessRun
import com.github.diplodoc.diplobase.repository.jpa.diploexec.ProcessRepository
import com.github.diplodoc.diplobase.repository.jpa.diploexec.ProcessRunRepository
import groovy.util.logging.Log4j
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import javax.annotation.PostConstruct

/**
 * @author yaroslav.yermilov
 */
class Diploexec {

    ThreadPoolTaskExecutor threadPool
    ProcessRepository processRepository
    ProcessRunRepository processRunRepository

    Collection<Process> processes
    Map<Process, Collection<String>> waitingMap
    Map<Process, Collection<String>> outputMap

    @PostConstruct
    void init() {
        println 'initializing diploexec runtime...'

        println 'loading processes...'
        processes = processRepository.findByActiveIsTrue()
        waitingMap = new HashMap<>()
        outputMap = new HashMap<>()

        println 'creating process interaction map...'
        processes.each { Process process ->
            waitingMap[process] = waitsFor(process)
            outputMap[process] = inputFor(process)
        }
    }

    void run(ProcessRun processRun) {
        println "starting process ${processRun}..."
        threadPool.execute(new ProcessCall(this, processRun))
    }

    void notify(DiploexecEvent event) {
        println "event fired ${event}..."
        event.notifiedRuns(this).each { ProcessRun processRun -> run(processRun) }
    }

    void notify(ProcessCallEvent event) {
        println "event fired ${event}..."

        switch (event.type) {
            case ProcessCallEvent.Type.PROCESS_RUN_STARTED:
                event.processRun.exitStatus = 'NOT FINISHED'
                event.processRun.startTime = event.time
                processRunRepository.save event.processRun
            break;

            case ProcessCallEvent.Type.PROCESS_RUN_SUCCEED:
                event.processRun.exitStatus = 'SUCCEED'
                event.processRun.endTime = event.time
                processRunRepository.save event.processRun
            break;

            case ProcessCallEvent.Type.PROCESS_RUN_FAILED:
                event.processRun.exitStatus = 'FAILED'
                event.processRun.endTime = event.time
                processRunRepository.save event.processRun
                break;

            default:
                assert false : "unknown ProcessCallEvent: ${event.type}"
        }
    }

    Process getProcess(String name) {
        processes.find { Process process -> process.name == name }
    }

    Collection<Process> getWaitProcesses(String eventName) {
        waitingMap.findAll { Process process, Collection<String> waitsFor -> waitsFor.contains(eventName) }.keySet()
    }

    Collection<Process> getInputProcesses(Process outputProcess) {
        outputMap.findAll { Process process, Collection<String> inputFor -> inputFor.contains(outputProcess.name) }.keySet()
    }

    Collection<String> waitsFor(Process process) {
        Collection<String> waitsFor = []

        String processWaitingDefinition = process.definition.readLines().findAll({ String line -> line.startsWith('waiting') }).join('\n')
        Binding binding = new Binding()
        binding.waiting = { Map parameters -> waitsFor << parameters.for }
        new GroovyShell(binding).evaluate(processWaitingDefinition)

        return waitsFor
    }

    Collection<String> inputFor(Process process) {
        Collection<String> inputFor = []

        String processListenDefinition = process.definition.readLines().findAll({ String line -> line.startsWith('listen') }).join('\n')
        Binding binding = new Binding()
        binding.listen = { Map parameters -> inputFor << parameters.to }
        new GroovyShell(binding).evaluate(processListenDefinition)

        return inputFor
    }
}
