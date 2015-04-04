package com.github.diplodoc.diplobase.domain.jpa.diploexec

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * @author yaroslav.yermilov
 */
@Entity
@Table(schema = 'diploexec')
@EqualsAndHashCode(includes = 'id')
@ToString(excludes = [ 'processRun', 'type', 'value' ])
class ProcessRunParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    String key

    String type

    String value

    @ManyToOne
    ProcessRun processRun
}
