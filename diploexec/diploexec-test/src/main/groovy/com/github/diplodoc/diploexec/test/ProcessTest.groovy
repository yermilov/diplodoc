package com.github.diplodoc.diploexec.test

import com.github.diplodoc.diplobase.domain.jpa.diploexec.Process
import com.github.diplodoc.diplocore.modules.Bindable

/**
 * @author yaroslav.yermilov
 */
class ProcessTest {

    Process process
    DiploexecTest diploexecTest

    Process processUnderTest
    Map<String, String> mockedModules = [:]
    Closure verifications
    Map<String, String> inputParameters = [:]

    List<Map> outputs = []
    List<String> requiredModules = []

    ProcessTest(Process process, DiploexecTest diploexecTest) {
        this.process = process
        this.diploexecTest = diploexecTest
    }

    TestResults test() {
        try {
            new GroovyShell(testDefinitionBinding()).evaluate(process.definition)
        } catch (Throwable e) {
            return TestResults.wrongTestDefinition(e)
        }

        try {
            new GroovyShell(testRunBinding(inputParameters)).evaluate(processUnderTest.definition)
        } catch (Throwable e) {
            return TestResults.runFailed(e)
        }

        try {
            verifications.call(new Expando(required: requiredModules, output: outputs))
        } catch (Throwable e) {
            return TestResults.verificationFailed(e)
        }

        return TestResults.passed()
    }

    private void send(String destination, Map parameters) {
        Map output = new HashMap(parameters)
        output['destination'] = destination
        outputs << output
    }

    private void output(Map parameters) {
        Map output = new HashMap(parameters)
        outputs << output
    }

    private void event(String name, Map parameters) {
        Map output = new HashMap(parameters)
        output['destination'] = name
        outputs << output
    }

    private Binding testDefinitionBinding() {
        Binding binding = new Binding()

        bindTest   binding
        bindMock   binding
        bindVerify binding
        bindParams binding

        return binding
    }

    private Binding testRunBinding(Map<String, Object> parameters) {
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

    private Binding bindTest(Binding binding) {
        binding.test = { String processUnderTestName ->
            processUnderTest = diploexecTest.getProcess(processUnderTestName)
        }
        return binding
    }

    private Binding bindMock(Binding binding) {
        binding.mock = { Map param ->
            mockedModules[param.module] = param.by
        }
        return binding
    }

    private Binding bindVerify(Binding binding) {
        binding.verify = { Closure verifications ->
            this.verifications = verifications
        }
        return binding
    }

    private Binding bindParams(Binding binding) {
        binding.params = { Map params ->
            inputParameters.putAll(params)
        }
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
                requiredModules << moduleName
                Bindable module = diploexecTest.getModule(mockedModules[moduleName]?:moduleName)
                module.bindSelf binding
            }
        }
    }

    private void bindSend(Binding binding) {
        binding.send = { Map<String, Object> parameters ->
            String destination = parameters.to
            parameters.remove 'to'

            send(destination, parameters)
        }
    }

    private void bindOutput(Binding binding) {
        binding.output = {Map<String, Object> parameters ->
            output(parameters)
        }
    }

    private void bindNotify(Binding binding) {
        binding.notify = { Map<String, Object> parameters ->
            String eventName = parameters.that
            parameters.remove 'that'

            event(eventName, parameters)
        }
    }

    private void bindListen(Binding binding) {
        binding.listen = { /* do nothing */ }
    }

    private void bindWaiting(Binding binding) {
        binding.waiting = { /* do nothing */ }
    }
}
