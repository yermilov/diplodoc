from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.svm import SVC
from sklearn.pipeline import Pipeline

import random
import Queue

import pickle
import base64

from flask import Flask
from flask import request

from pymongo import MongoClient
from bson.objectid import ObjectId
from bson.dbref import DBRef

from datetime import datetime


app = Flask(__name__)


@app.route("/doc-type-classifier/doc/<doc_id>/classify")
def classify(doc_id):
    start_time = str(datetime.now())

    client = MongoClient()
    db = client['diplodata']

    doc = db.doc.find_one({"_id": ObjectId(doc_id)})

    topic_map = {}
    topic_ref_list = []

    queue = Queue.Queue()
    immidiate_children = get_immediate_children()
    for child in immidiate_children:
        queue.put(child)

    while not queue.empty():
        record = queue.get()
        depickled = record['classifier']
        topic = record['label']
        decoded = base64.b64decode(depickled)
        text_clf = pickle.loads(decoded)

        if text_clf is None:
            score = 0.0
        else :
            predicted = text_clf.predict_proba([doc['meaningText']])[0]
            score = predicted[0]

        if score > 0.4:
            topic_map[topic] = score
            topic_ref_list.append({'topic_id': record['_id'], 'score': score})

            children = children_list(record)
            for child in children:
                queue.put(child)

    doc['predicted_topics'] = topic_ref_list
    db.doc.update({"_id": doc["_id"]}, doc)

    end_time = str(datetime.now())

    module = db.module.find_one({ "name": "DocTypeClassifier" })
    moduleMethod = db.moduleMethod.find_one({ "name": "classify", "module": module['_id'] })
    moduleMethodRun = { "moduleMethod": moduleMethod['_id'], "startTime": start_time, "endTime": end_time }

    db.moduleMethodRun.insert(moduleMethodRun)

    return 'RESULT: ' + str(topic_map)


@app.route("/doc-type-classifier/train-from-all-docs", methods=['POST', 'GET'])
def train():
    start_time = str(datetime.now())

    content = request.json
    partial_train = False
    if content is not None and 'partial_train' in content:
        partial_train = True

    client = MongoClient()
    db = client['diplodata']
    print 'start loading data from db...'
    train_texts, train_labels = [], []
    for doc in db.doc.find():
        if 'train_topics' in doc.keys():
            train_texts.append(doc['meaningText'])
            train_labels.append([dereference_topic(db, x)['label'] for x in doc['train_topics']])

    topics = []
    for record in db.topic.find():
        topic = record['label']
        if not partial_train:
            topics.append(topic)
        elif 'classifier' not in record:
            topics.append(topic)
    print 'finished loading data from db...'

    train_labels = extend_label_list(train_labels)
    print 'extended training labels...'

    print 'start building classifiers...'
    (classifiers, quality_score) = build_classifiers(train_texts, train_labels, topics)

    print 'save classifiers'
    for i in range(len(classifiers)):
        topic = topics[i]
        classifier = classifiers[i]
        pickled = pickle.dumps(classifier)
        encoded = base64.b64encode(pickled)

        record = db.topic.find_one({'label': topic})
        record['classifier'] = encoded
        db.topic.update({"_id": record["_id"]}, record, upsert=True)

    end_time = str(datetime.now())

    print 'save perf data...'
    module = db.module.find_one({ "name": "DocTypeClassifier" })
    moduleMethod = db.moduleMethod.find_one({ "name": "train", "module": module['_id'] })
    moduleMethodRun = { "moduleMethod": moduleMethod['_id'], "startTime": start_time, "endTime": end_time, "metrics": {'aggregated_score': quality_score, 'num_topics': len(topics)} }

    db.moduleMethodRun.insert(moduleMethodRun)

    return "YOUR CLASSIFIER IS READY TO USE: " + str(quality_score)


def get_immediate_children():
    client = MongoClient()
    db = client['diplodata']
    children = []
    for x in db.topic.find({'parent': {'$exists': 0}}):
        children.append(x)

    return children


def extend_label_list(train_labels):
    extended_train_labels = [None]*len(train_labels)
    for i in range(len(train_labels)):
        extended_train_labels[i] = list(set(reduce(lambda acc, v: acc + parent_list(v), train_labels[i], [])))
    return extended_train_labels


def build_classifiers(train_texts, train_labels, topics):
    classifiers = []
    m = len(train_labels)
    zipped_data = zip(train_texts, train_labels)
    random.shuffle(zipped_data)
    shuffled_train_texts, shuffled_train_labels = zip(*zipped_data)
    cv_train_texts, cv_test_texts = shuffled_train_texts[:m/5], shuffled_train_texts[m/5 + 1:]
    cv_train_labels, cv_test_labels = shuffled_train_labels[:m/5], shuffled_train_labels[m/5 + 1:]
    print 'splitted data for cross-validation'

    scores = [None]*len(cv_test_texts)
    for topic in topics:
        (cv_train_labels_adjusted, cv_train_texts_adjusted) = adjust_and_filter(cv_train_labels, cv_train_texts, topic)
        text_clf = Pipeline([('vect', CountVectorizer()), ('tfidf', TfidfTransformer()), ('clf', SVC(probability=True))])

        if len(set(cv_train_labels_adjusted)) <= 1:
            (cv_train_labels_adjusted, cv_train_texts_adjusted) = adjust_and_filter(train_labels, train_texts, topic)
            if len(set(cv_train_labels_adjusted)) <= 1:
                text_clf = None
            else:
                text_clf = text_clf.fit(cv_train_texts_adjusted, cv_train_labels_adjusted)
        else:
            text_clf = text_clf.fit(cv_train_texts_adjusted, cv_train_labels_adjusted)

        classifiers.append(text_clf)
        print 'classifier for topic %s created' % topic

        for i, test_text in enumerate(cv_test_texts):
            if text_clf is None:
                score = 0.0
            else:
                score = text_clf.predict_proba(test_text)[0][0]
            if scores[i] is None:
                scores[i] = {topic: score}
            else:
                scores[i][topic] = score

    quality_score = final_score(cv_test_labels, scores)

    return classifiers, quality_score


def final_score(test_label_list, predicted_labels_map):
    res_list = []

    for scores_map in predicted_labels_map:
        cur_res = 0.0
        for i, topic in enumerate(scores_map.keys()):
            if topic in test_label_list[i]:
                cur_res += abs(1 - scores_map[topic])
            else:
                cur_res += scores_map[topic]
        res_list.append(cur_res)

    return sum(res_list)/len(res_list)


def children_list(topic):
    client = MongoClient()
    db = client['diplodata']

    if topic is None:
        return get_immediate_children()

    if isinstance(topic, basestring):
        topic_record = db.topic.find_one({'label': topic})
    else:
        topic_record = topic

    children_record = []
    for record in db.topic.find({'parent': DBRef('topic', topic_record['_id'])}):
        children_record.append(record)

    return children_record


def get_direct_parent(topic):
    client = MongoClient()
    db = client['diplodata']
    record = db.topic.find_one({'label': topic})

    if 'parent' not in record:
        return None

    return dereference_topic(db, record['parent'])


def parent_list(topic):
    parents = [topic]

    client = MongoClient()
    db = client['diplodata']
    record = db.topic.find_one({'label': topic})
    iterator = record
    while 'parent' in iterator.keys():
        iterator = dereference_topic(db, iterator['parent'])
        parents.append(iterator['label'])

    return parents


def adjust_and_filter(train_labels, train_texts, topic):
    adjusted_labels, adjusted_texts = [], []

    parent_topic = get_direct_parent(topic)
    eligible_topics = set([x['label'] for x in children_list(parent_topic)])

    for i in range(len(train_labels)):
        if topic in train_labels[i]:
            adjusted_labels.append(0)
            adjusted_texts.append(train_texts[i])
        elif len(eligible_topics.intersection(train_labels[i])) > 0:
            adjusted_labels.append(1)
            adjusted_texts.append(train_texts[i])

    return adjusted_labels, adjusted_texts


def dereference_topic(db, topic):
    if isinstance(topic, DBRef):
        return db.dereference(topic)
    else:
        return db.topic.find_one({ "_id": ObjectId(str(topic)) })


if __name__ == '__main__':
    app.run(debug=True)
