package com.github.diplodoc.diplocore.services

import com.github.diplodoc.diplobase.domain.mongodb.diploexec.Module
import com.github.diplodoc.diplobase.domain.mongodb.diploexec.ModuleMethod
import com.github.diplodoc.diplobase.domain.mongodb.diploexec.ModuleMethodRun
import com.github.diplodoc.diplobase.repository.mongodb.diploexec.ModuleMethodRepository
import com.github.diplodoc.diplobase.repository.mongodb.diploexec.ModuleMethodRunRepository
import com.github.diplodoc.diplobase.repository.mongodb.diploexec.ModuleRepository
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
            ModuleMethod moduleMethod = moduleMethodRepository.findByName(methodName).find({ it.moduleId == module.id })
            ModuleMethodRun moduleMethodRun = new ModuleMethodRun(moduleMethodId: moduleMethod.id, startTime: LocalDateTime.now())

            def response = originalMethod.call(module, moduleMethod, moduleMethodRun)

            moduleMethodRun.endTime = LocalDateTime.now()
            moduleMethodRun.metrics = response['metrics']

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
