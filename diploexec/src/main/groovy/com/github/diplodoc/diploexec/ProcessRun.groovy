package com.github.diplodoc.diploexec

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
class ProcessRun {

    DiploexecRuntimeEnvironment runtime

    def process
    def params

    def id = UUID.randomUUID()
    def description
    def status = 'WAITING'
    def startTime
    def endTime

    def run() {
        status = 'RUNNING'
        startTime = LocalDateTime.now()

        new GroovyShell(binding(params)).evaluate(process.definition)

        status = 'FINISHED'
        endTime = LocalDateTime.now()
    }

    private def binding(Map input) {
        def binding = new Binding()

        bindInputParameters binding, input
        bindInput binding
        bindDescription binding
        bindRequire binding
        bindSend binding
        bindOutput binding
        bindNotify binding
        bindListen binding
        bindWaiting binding

        binding
    }

    private def bindInputParameters(Binding binding, Map input) {
        input.each {
            binding."${it.key}" = it.value
        }
    }

    private def bindInput(Binding binding) {
        binding.input = {
            String[] args ->
                println "Input parameters:"
                args.each {
                    def key = it
                    def value = binding."${key}"
                    println "${key} = ${value}"
                }
        }
    }

    private def bindDescription(Binding binding) {
        binding.description = {
            String description -> this.description = description
        }
    }

    private def bindRequire(Binding binding) {
        binding.require = {
            String[] processNames ->
                println "Loading required processes:"
                processNames.each {
                    String processName ->
                        println processName
                        def process = runtime.getProcess(processName)
                        process.bind binding
                }
        }
    }

    private def bindSend(Binding binding) {
        binding.send = {
            Map params ->
                def destination = params.to
                params.remove 'to'

                runtime.startProcess(destination, params)
        }
    }

    private def bindOutput(Binding binding) {
        binding.output = {
            Map params ->
                runtime.outputed(process, params)
        }
    }

    private def bindNotify(Binding binding) {
        binding.notify = {
            Map params ->
                def that = params.that
                params.remove 'that'

                runtime.notified(that, params)
        }
    }

    private def bindListen(Binding binding) {
        binding.listen = { }
    }

    private def bindWaiting(Binding binding) {
        binding.waiting = { }
    }
}