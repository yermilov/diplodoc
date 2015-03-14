package com.github.diplodoc.diplocore.modules

import static spock.util.matcher.HamcrestMatchers.*
import static spock.util.matcher.HamcrestSupport.that

import com.github.diplodoc.diplobase.domain.mongodb.Post
import com.github.diplodoc.diplobase.domain.mongodb.Topic
import com.github.diplodoc.diplobase.repository.mongodb.PostRepository
import com.github.diplodoc.diplobase.repository.mongodb.TopicRepository
import com.github.diplodoc.diplocore.services.RestService
import spock.lang.Specification

/**
 * @author yaroslav.yermilov
 */
class CrossValidatorSpec extends Specification {

    RestService restService = Mock(RestService)
    PostRepository postRepository = Mock(PostRepository)
    TopicRepository topicRepository = Mock(TopicRepository)

    CrossValidator crossValidator = new CrossValidator(restService: restService, postRepository: postRepository, topicRepository: topicRepository)

    def 'boolean isCrossValidation(Post post)'() {
        expect:
            crossValidator.isCrossValidation(post) == expectedCrossValidation

        where:
            post                                    | expectedCrossValidation
            new Post(train_topics: null)            | false
            new Post(train_topics: [])              | false
            new Post(train_topics: [ new Topic() ]) | true
    }

    def 'long calculateNeedMoreTime(int pageNumber, int totalPages, long validationStart, long now)'() {
        expect:
            expectedNeedMoreTime == crossValidator.calculateNeedMoreTime(pageNumber, totalPages, validationStart, now)

        where:
            pageNumber | totalPages | validationStart | now | expectedNeedMoreTime
            0          | 5          | 100             | 200 | 400
            1          | 5          | 100             | 300 | 300
            0          | 1          | 100             | 200 | 0
            4          | 5          | 100             | 200 | 0
    }

    def 'def classify(Post post)'() {
        setup:
            Post post = new Post(id: 'post-id')

            postRepository.findOne('post-id') >> new Post(id: 'post-id', predicted_topics: [ new Topic(label:  'predicted') ])

        when:
            def actual = crossValidator.classify(post)

        then:
            actual.post == new Post(id: 'post-id', predicted_topics: [ new Topic(label: 'predicted') ])
            actual.time != null
    }

    def 'double calculateScore(Post post)'() {
        setup:
            Post post = new Post(id: 'post-id')
            post.train_topics = [
                new Topic(id: 'topic-1', parent: new Topic(id: 'topic-3')),
                new Topic(id: 'topic-2', parent: new Topic(id: 'topic-4')),
                new Topic(id: 'topic-3')
            ]
            post.predicted_topics = [
                [ topic_id: 'topic-1', score: 0.1 ],
                [ topic_id: 'topic-2', score: 0.2 ],
                [ topic_id: 'topic-3', score: 0.3 ],
                [ topic_id: 'topic-4', score: 0.4 ],
                [ topic_id: 'topic-5', score: 0.6 ]
            ]

            topicRepository.findOne('topic-1') >> new Topic(id: 'topic-1', parent: new Topic(id: 'topic-3'))
            topicRepository.findOne('topic-2') >> new Topic(id: 'topic-2', parent: new Topic(id: 'topic-4'))
            topicRepository.findOne('topic-3') >> new Topic(id: 'topic-3')
            topicRepository.findOne('topic-4') >> new Topic(id: 'topic-4')
            topicRepository.findOne('topic-5') >> new Topic(id: 'topic-5', parent: new Topic(id: 'topic-4'))

        when:
            double actual = crossValidator.calculateScore(post)

        then:
            that actual, closeTo(0.72, 1e-4)
    }

    def 'List getPredictedTopics(Post post)'() {
        setup:
            Post post = new Post(id: 'post-id')
            post.predicted_topics = [
                [ topic_id: 'topic-1', score: 0.1 ],
                [ topic_id: 'topic-2', score: 0.5 ],
                [ topic_id: 'topic-3', score: 0.3 ],
                [ topic_id: 'topic-4', score: 0.4 ],
                [ topic_id: 'topic-5', score: 0.2 ]
            ]

            topicRepository.findOne('topic-1') >> new Topic(id: 'topic-1', label: 'label-1',parent: new Topic(label: 'topic-3'))
            topicRepository.findOne('topic-2') >> new Topic(id: 'topic-2', label: 'label-2', parent: new Topic(label: 'topic-4'))
            topicRepository.findOne('topic-3') >> new Topic(id: 'topic-3', label: 'label-3')
            topicRepository.findOne('topic-4') >> new Topic(id: 'topic-4', label: 'label-4')
            topicRepository.findOne('topic-5') >> new Topic(id: 'topic-5', label: 'label-5', parent: new Topic(label: 'topic-4'))

        when:
            List actual = crossValidator.getPredictedTopics(post)

        then:
            actual == [ 'label-2: 0.5', 'label-4: 0.4', 'label-3: 0.3', 'label-5: 0.2', 'label-1: 0.1' ]
    }

    def 'Map getPostDump(Post post, List predictedTopics, double postScore, double classificationTime)'() {
        setup:
            Post post = new Post(id: 'post-id', url: 'url', title: 'title')
            post.train_topics = [
                new Topic(id: 'topic-1', label: 'label-1', parent: new Topic(id: 'topic-3', label: 'label-3')),
                new Topic(id: 'topic-2', label: 'label-2', parent: new Topic(id: 'topic-4', label: 'label-4')),
                new Topic(id: 'topic-3', label: 'label-3')
            ]
            List predictedTopics = [ 'label-2: 0.5', 'label-4: 0.4', 'label-3: 0.3', 'label-5: 0.2', 'label-1: 0.1' ]
            double postScore = 0.28
            long classificationTime = 12345

        when:
            Map actual = crossValidator.getPostDump(post, predictedTopics, postScore, classificationTime)

        then:
            actual == [
                'id': 'post-id',
                'url': 'url',
                'title': 'title',
                'train-topics': ['label-1', 'label-2', 'label-3', 'label-4' ],
                'predicted-topics': [ 'label-2: 0.5', 'label-4: 0.4', 'label-3: 0.3', 'label-5: 0.2', 'label-1: 0.1' ],
                'post-score': 0.28,
                'classification-time': '12.345s'
            ]
    }

    def 'Collection<Topic> unrollTopics(Collection<Topic> original)'() {
        setup:
            Topic topic1 = new Topic(label: 'topic-1')
            Topic topic2 = new Topic(label: 'topic-2')
            Topic topic3 = new Topic(label: 'topic-3')
            Topic topic4 = new Topic(label: 'topic-4')
            topic2.parent = topic1
            topic3.parent = topic2

        when:
            Collection<Topic> actual = crossValidator.unrollTopics([ topic2, topic3, topic4 ])

        then:
            actual == [ topic1, topic2, topic3, topic4 ] as Set
    }
}
