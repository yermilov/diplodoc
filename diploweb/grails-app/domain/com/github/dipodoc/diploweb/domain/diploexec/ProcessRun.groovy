package com.github.dipodoc.diploweb.domain.diploexec

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

import java.time.LocalDateTime

@EqualsAndHashCode
class ProcessRun {

    static mapWith = 'mongo'

    ObjectId id


    static belongsTo = [ process: Process ]

    LocalDateTime startTime

    LocalDateTime endTime

    String exitStatus

    static hasMany = [ parameters: ProcessRunParameter ]

    static embedded = [ 'parameters' ]
}
