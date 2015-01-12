package com.github.diplodoc.diploexec

import com.github.diplodoc.diplobase.client.ProcessRunDataClient
import com.github.diplodoc.diplobase.domain.diploexec.ProcessRun
import com.github.diplodoc.diplocore.modules.Module
import groovy.json.JsonSlurper

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
class ProcessCall implements Runnable {

    JsonSlurper jsonSlurper = new JsonSlurper()

    Diploexec diploexec
    ProcessRun processRun

    ProcessCall(Diploexec diploexec, ProcessRun processRun) {
        this.diploexec = diploexec
        this.processRun = processRun
    }

    @Override
    void run() {
        diploexec.notify(ProcessCallEvent.started(processRun))

        String script = processRun.process.definition
        Map<String, Object> parameters = processRun.parameters.collectEntries { parameter ->
            [ parameter.key,  Class.forName(parameter.type).newInstance([jsonSlurper.parseText(parameter.value)]) ]
        }

        new GroovyShell(binding(parameters)).evaluate(script)

        diploexec.notify(ProcessCallEvent.ended(processRun))
    }

    private Binding binding(Map<String, Object> parameters) {
        Binding binding = new Binding()

        bindInputParameters binding, parameters
        bindInput binding
        bindDescription binding
        bindRequire binding
        bindSend binding
        bindOutput binding
        bindNotify binding
        bindListen binding
        bindWaiting binding

        return binding
    }

    private void bindInputParameters(Binding binding, Map<String, Object> parameters) {
        parameters.each {
            binding."${it.key}" = it.value
        }
    }

    private void bindInput(Binding binding) {
        binding.input = { String[] args -> /* do nothing */ }
    }

    private void bindDescription(Binding binding) {
        binding.description = { String description -> /* do nothing */ }
    }

    private void bindRequire(Binding binding) {
        binding.require = { String[] modulesNames ->
            modulesNames.each { String moduleName ->
                Module module = diploexec.getModule(moduleName)
                module.bindSelf binding
            }
        }
    }

    private void bindSend(Binding binding) {
        binding.send = { Map<String, Object> parameters ->
            String destination = parameters.to
            parameters.remove 'to'

            diploexec.notify(new SendEvent(destination, parameters))
        }
    }

    private void bindOutput(Binding binding) {
        binding.output = {Map<String, Object> parameters ->
            diploexec.notify(new OutputEvent(processRun, parameters))
        }
    }

    private void bindNotify(Binding binding) {
        binding.notify = { Map<String, Object> parameters ->
            String eventName = parameters.that
            parameters.remove 'that'

            diploexec.notify(new NotifyEvent(eventName, parameters))
        }
    }

    private void bindListen(Binding binding) {
        binding.listen = { /* do nothing */ }
    }

    private void bindWaiting(Binding binding) {
        binding.waiting = { /* do nothing */ }
    }
}
