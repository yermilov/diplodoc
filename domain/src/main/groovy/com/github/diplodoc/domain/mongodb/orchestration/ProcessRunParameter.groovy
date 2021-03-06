package com.github.diplodoc.domain.mongodb.orchestration

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * @author yaroslav.yermilov
 */
@EqualsAndHashCode
@ToString
class ProcessRunParameter {

    String key

    String type

    String value
}
