package com.github.diplodoc.diplobase.domain.mongodb.diploexec

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Field

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
@EqualsAndHashCode(excludes = 'parameters')
@ToString
class ProcessRun {

    @Id
    ObjectId id


    @Field('process')
    ObjectId processId

    String startTime

    String endTime

    String exitStatus

    Collection<ProcessRunParameter> parameters
}