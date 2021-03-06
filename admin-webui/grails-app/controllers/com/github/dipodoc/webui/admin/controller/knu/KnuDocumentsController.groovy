package com.github.dipodoc.webui.admin.controller.knu

import com.github.dipodoc.webui.admin.domain.data.Doc
import grails.transaction.Transactional
import org.bson.types.ObjectId
import org.springframework.security.access.annotation.Secured

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

@Transactional(readOnly = true)
@Secured([ 'ROLE_ADMIN' ])
class KnuDocumentsController {

    static allowedMethods = [ delete: 'DELETE' ]

    def list() {
        params.max = 20
        params.sort = 'loadTime'
        params.order = 'desc'

        def knuDocuments = Doc.where { knu == 'document' }

        respond knuDocuments.list(params), model: [ docInstanceCount: knuDocuments.count() ]
    }

    def show(Doc docInstance) {
        def similar = null
        if (docInstance.knu_similarities) {
            similar = docInstance.knu_similarities
                            .collect({ ObjectId key, Double value ->
                                [ key: key, value: value  ]
                            })
                            .sort({ o1, o2 -> Double.compare(o1.value, o2.value) })
                            .take(5)
                            .collect({
                                Doc.get(it.key)
                            })
        }

        respond docInstance, model: [ similar: similar ]
    }

    @Transactional
    def delete(Doc docInstance) {
        if (docInstance == null) {
            notFound()
            return
        }

        docInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [ message(code: 'doc.label', default: 'Doc'), docInstance.id ])
                redirect action: 'list', method: 'GET'
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [ message(code: 'doc.label', default: 'Doc'), params.id ])
                redirect action: 'list', method: 'GET'
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
