package com.github.dipodoc.webui.admin.domain.data

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

@EqualsAndHashCode
class Doc {

    static mapWith = 'mongo'

    ObjectId id


    String uri

    String type

    static belongsTo = [ source: Source ]

    String loadTime


    String title

    String description

    String publishTime

    String html


    String train_meaningHtml

    String meaningHtml

    String meaningText


    static hasMany = [ train_topics: Topic ]

    List predicted_topics


    String knu

    Map<ObjectId, Double> knu_similarities

    Double knu_social_predicted_sentiment_score


    static constraints = {
        uri nullable: true
        type nullable: true
        source nullable: true
        loadTime nullable: true
        title nullable: true
        description nullable: true
        publishTime nullable: true
        html nullable: true
        train_meaningHtml nullable: true
        meaningHtml nullable: true
        meaningText nullable: true
        predicted_topics nullable: true
        knu nullable: true
        knu_similarities nullable: true
        knu_social_predicted_sentiment_score nullable: true
    }

    static mapping = {
        version false
    }
}
