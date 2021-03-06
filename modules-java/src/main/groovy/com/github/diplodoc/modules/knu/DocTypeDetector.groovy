package com.github.diplodoc.modules.knu

import com.github.diplodoc.domain.mongodb.data.Doc
import com.github.diplodoc.domain.repository.mongodb.data.DocRepository
import com.github.diplodoc.modules.services.AuditService
import com.github.diplodoc.modules.services.RawDataService
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author yaroslav.yermilov
 */
@Controller
@RequestMapping('/knu/doc-type-detector')
@Slf4j
class DocTypeDetector {

    @Autowired
    DocRepository docRepository

    @Autowired
    AuditService auditService

    @Autowired
    RawDataService rawDataService

    @RequestMapping(value = '/doc/{id}/type', method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    def detectType(@PathVariable('id') String docId) {
        auditService.runMethodUnderAudit('knu.DocTypeDetector', 'detectType') { module, moduleMethod, moduleMethodRun ->
            moduleMethodRun.parameters = [ 'docId': docId ]

            Doc doc = docRepository.findOne new ObjectId(docId)

            doc.type = rawDataService.detectType(doc.uri, doc.binary)

            log.info "${doc.uri} detected as ${doc.type}"

            docRepository.save doc

            [ 'moduleMethodRun': moduleMethodRun ]
        }
    }
}
