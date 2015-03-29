package com.github.dipodoc.diploweb

import com.github.dipodoc.diploweb.diplodata.Post
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Transactional(readOnly = true)
class TrainMeaningHtmlController {

    static allowedMethods = [ saveAndNext: 'PUT' ]

    Random random = new Random()

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def trainSet = Post.findAllByTrain_meaningHtmlIsNotNull(params)

        respond trainSet, model: [ postInstanceCount: Post.countByTrain_meaningHtmlIsNotNull() ]
    }

    def trainNext() {
        int index = random.nextInt(Post.count())
        def params = [ offset: index, max: 1 ]

        Post randomUntrainedPost = Post.findByTrain_meaningHtmlIsNull(params)
        [ postToTrain: randomUntrainedPost ]
    }

    @Transactional
    def saveAndNext() {
        Post postToTrain = Post.get(params.id)
        postToTrain.train_meaningHtml = params.train_meaningHtml

        if (postToTrain == null) {
            notFound()
            return
        }

        if (postToTrain.hasErrors()) {
            respond postToTrain.errors, view: 'trainNext'
            return
        }

        postToTrain.save flush:true

        redirect action: 'trainNext'
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'post.label', default: 'Post'), params.id])
                redirect action: 'list', method: 'GET'
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
