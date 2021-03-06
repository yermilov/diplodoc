package com.github.diplodoc.modules.services

import com.github.diplodoc.domain.mongodb.orchestration.Module
import com.github.diplodoc.domain.mongodb.orchestration.ModuleMethod
import com.github.diplodoc.domain.mongodb.orchestration.ModuleMethodRun
import com.github.diplodoc.domain.repository.mongodb.orchestration.ModuleMethodRepository
import com.github.diplodoc.domain.repository.mongodb.orchestration.ModuleMethodRunRepository
import com.github.diplodoc.domain.repository.mongodb.orchestration.ModuleRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
@Service
@Slf4j
class AuditService {

    @Autowired
    ModuleRepository moduleRepository

    @Autowired
    ModuleMethodRepository moduleMethodRepository

    @Autowired
    ModuleMethodRunRepository moduleMethodRunRepository

    def runMethodUnderAudit(String moduleName, String methodName, Closure originalMethod) {
        try {
            Module module = moduleRepository.findOneByName(moduleName)
            if (!module) {
                throw new RuntimeException("Module ${moduleName} does not exists")
            }

            ModuleMethod moduleMethod = moduleMethodRepository.findByName(methodName).find({ it.moduleId == module.id })
            if (!moduleMethod) {
                throw new RuntimeException("Module method ${moduleName}::${methodName} does not exists")
            }

            ModuleMethodRun moduleMethodRun = new ModuleMethodRun(moduleMethodId: moduleMethod.id, startTime: LocalDateTime.now())

            log.info "Starting ${moduleName}::${methodName}..."
            def response = originalMethod.call(module, moduleMethod, moduleMethodRun)

            moduleMethodRun.endTime = LocalDateTime.now()
            moduleMethodRun.metrics = response['metrics']

            log.info "${moduleName}::${methodName}(${moduleMethodRun?.parameters ?: ''}) finished"

            if (response['module']) {
                moduleRepository.save response['module']
            }
            if (response['moduleMethod']) {
                moduleMethodRepository.save response['moduleMethod']
            }
            moduleMethodRunRepository.save moduleMethodRun

            return response['result']
        } catch (e) {
            log.error "${moduleName}::${methodName} failed: ${e.message}", e
            throw e
        }
    }
}
