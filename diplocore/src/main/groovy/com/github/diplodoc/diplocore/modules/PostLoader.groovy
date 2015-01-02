package com.github.diplodoc.diplocore.modules

import com.github.diplodoc.diplobase.domain.diplodata.Post
import com.github.diplodoc.diplobase.domain.diplodata.Source
import com.github.diplodoc.diplobase.repository.diplodata.PostRepository
import com.github.diplodoc.diplocore.services.Web
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author yaroslav.yermilov
 */
class PostLoader {

    Web web

    @Autowired
    PostRepository postRepository

    def bind(Binding binding) {
        binding.load = {
            Map params -> load(params.from, params.url)
        }
    }

    def load(Source source, String url) {
        def document = web.load(url)
        def post = new Post(url: url, html: document.html(), source: source)

        postRepository.save post
    }
}