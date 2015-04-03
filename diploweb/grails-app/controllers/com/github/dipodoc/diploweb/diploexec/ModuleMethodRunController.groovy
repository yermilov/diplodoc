package com.github.dipodoc.diploweb.diploexec

import grails.transaction.Transactional

@Transactional(readOnly = true)
class ModuleMethodRunController {

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        params.sort = 'startTime'
        params.order = 'desc'
        respond ModuleMethodRun.list(params), model: [ moduleMethodRunInstanceCount: ModuleMethodRun.count() ]
    }

    def show(ModuleMethodRun moduleMethodRunInstance) {
        respond moduleMethodRunInstance
    }
}
