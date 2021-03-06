package com.github.diplodoc.modules.data

import com.github.diplodoc.domain.mongodb.data.Source
import com.github.diplodoc.domain.mongodb.orchestration.Module
import com.github.diplodoc.domain.mongodb.orchestration.ModuleMethod
import com.github.diplodoc.domain.mongodb.orchestration.ModuleMethodRun
import com.github.diplodoc.domain.repository.mongodb.data.SourceRepository
import com.github.diplodoc.modules.services.AuditService
import org.bson.types.ObjectId
import spock.lang.Specification

/**
 * @author yaroslav.yermilov
 */
class SourcesSpec extends Specification {

    SourceRepository sourceRepository = Mock(SourceRepository)
    AuditService auditService = Mock(AuditService)

    Sources sources = new Sources(sourceRepository: sourceRepository, auditService: auditService)

    def 'def all()'() {
        when:
            1 * auditService.runMethodUnderAudit('data.Sources', 'all', _) >> { it ->
                Module module = new Module()
                ModuleMethod moduleMethod = new ModuleMethod()
                ModuleMethodRun moduleMethodRun = new ModuleMethodRun()

                return it[2].call(module, moduleMethod, moduleMethodRun)
            }

            sourceRepository.findAll() >> [ new Source(id: new ObjectId('111111111111111111111111')), new Source(id: new ObjectId('222222222222222222222222')) ]

        then:
            Map actual = sources.all()

        expect:
            actual.keySet().size() == 2
            actual['result'] == [ '111111111111111111111111', '222222222222222222222222' ]
            actual['metrics'] == [ 'sources count': 2 ]
    }
}
