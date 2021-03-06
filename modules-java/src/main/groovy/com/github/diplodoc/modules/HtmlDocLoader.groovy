package com.github.diplodoc.modules

import com.github.diplodoc.domain.mongodb.data.Doc
import com.github.diplodoc.domain.repository.mongodb.data.DocRepository
import com.github.diplodoc.modules.services.AuditService
import com.github.diplodoc.modules.services.HtmlService
import groovy.util.logging.Slf4j
import org.bson.types.ObjectId
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus

import java.time.LocalDateTime

/**
 * @author yaroslav.yermilov
 */
@Controller
@RequestMapping('/html-doc-loader')
@Slf4j
class HtmlDocLoader {

    @Autowired
    HtmlService htmlService

    @Autowired
    DocRepository docRepository

    @Autowired
    AuditService auditService

    @RequestMapping(value = '/doc/{id}/load', method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    def loadDoc(@PathVariable('id') String docId) {
        auditService.runMethodUnderAudit('HtmlDocLoader', 'loadDoc') { module, moduleMethod, moduleMethodRun ->
            moduleMethodRun.parameters = [ 'docId': docId ]

            Doc doc = docRepository.findOne new ObjectId(docId)

            Document document = htmlService.load doc.uri
            doc.html = document.html()
            doc.binary = doc.html.bytes
            doc.type = 'text/html'
            doc.loadTime = LocalDateTime.now()

            docRepository.save doc

            [ 'moduleMethodRun': moduleMethodRun ]
        }
    }
}
