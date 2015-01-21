package com.github.diplodoc.diploexec

import com.github.diplodoc.diplobase.domain.diploexec.ProcessRun

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
class ProcessCallEvent {

    enum Type { PROCESS_RUN_STARTED, PROCESS_RUN_ENDED }

    Type type
    LocalDateTime time
    ProcessRun processRun

    static ProcessCallEvent started(ProcessRun processRun) {
        ProcessCallEvent event = new ProcessCallEvent()
        event.time = LocalDateTime.now()
        event.type = ProcessCallEvent.Type.PROCESS_RUN_STARTED
        event.processRun = processRun

        return event
    }

    static ProcessCallEvent ended(ProcessRun processRun) {
        ProcessCallEvent event = new ProcessCallEvent()
        event.time = LocalDateTime.now()
        event.type = ProcessCallEvent.Type.PROCESS_RUN_ENDED
        event.processRun = processRun

        return event
    }
}
