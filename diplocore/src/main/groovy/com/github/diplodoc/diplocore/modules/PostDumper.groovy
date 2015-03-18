package com.github.diplodoc.diplocore.modules

import com.github.diplodoc.diplobase.domain.mongodb.Post
import com.github.diplodoc.diplobase.repository.mongodb.PostRepository
import com.github.diplodoc.diplocore.services.ResourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author yaroslav.yermilov
 */
@Controller
@RequestMapping('/post-dumper')
class PostDumper {

    @Autowired
    PostRepository postRepository

    @Autowired
    ResourceService resourceService

    @RequestMapping(value = '/post/{id}/dump', method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    void dumpPost(@PathVariable('id') String postId, @RequestBody String folder) {
        Post post = postRepository.findOne(postId)
        String postDump = toDump(post)
        resourceService.writeToFile(folder, "post-${post.id}.dump", postDump.join('\n'))
    }

    List<String> toDump(Post post) {
        List<String> postDump = []
        postDump << post.id
        postDump << post.url
        postDump << post.title
        postDump << post.meaningText

        return postDump
    }
}