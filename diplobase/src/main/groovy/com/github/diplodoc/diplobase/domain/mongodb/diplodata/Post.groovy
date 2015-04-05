package com.github.diplodoc.diplobase.domain.mongodb.diplodata

import com.mongodb.DBRef
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field

/**
 * @author yaroslav.yermilov
 */
@EqualsAndHashCode
@ToString(excludes = [ 'meaningText', 'html' ])
class Post {

    @Id
    String id


    String url

    @Field('source')
    ObjectId sourceId

    String loadTime


    String title

    String description

    String publishTime

    String html


    @Field('train_meaningHtml')
    String trainMeaningHtml

    String meaningHtml

    String meaningText
}
