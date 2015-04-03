package com.github.dipodoc.diploweb.diploexec

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

@EqualsAndHashCode
class Module {

    static mapWith = 'mongo'

    ObjectId id


    String name

    Map data
}
