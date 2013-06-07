#!/usr/bin/python
"""
    This class is used for retrieving named entities using Alchemy API.
    URL format is
    "http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities?
    apikey=b92c95cc3bb59ff63ee0fc358f251f20b8e5c3e9&
    text=%22John%20Doe%20is%20cool.%22&
    outputMode=json&
    disambiguate=0"
"""
import json
import re

import urllib2
import urllib

# user imports
import constants


class NerAlchemyApiCall:
    URL_ROOT = constants.ALCHEMY_API_ROOT_URL
    API_KEY = constants.APY_KEY
    ENTITIES_PATH = constants.ALCHEMY_API_ENTITIES_PATH

    def __init__(self, text, max_retrieve):
        self.text, self.max_retrieve = text, max_retrieve
        self.eliminate_hyphenation()
        self.query = {"apikey": self.API_KEY,
                      "outputMode": "json",
                      "maxRetrieve": self.max_retrieve,
                      "text": self.text
        }

        self.pers_found, self.orgs_found = set(), set()
        self.locs_found, self.misc_found = set(), set()
        self.entities_indices = []
        self.annotated_text = self.text


    def annotate_text(self):
        entities = self.extract_named_entities()
        self.check_entity_types(entities)

        print "Total entities count {0}".format(len(entities))
        print "All entities", [ent["text"] for ent in entities]
        for entity in entities:
            print entity['relevance'], entity['count'], entity['type'], entity['text']
            self.annotate_entity(entity)

        return self.text

    def extract_named_entities(self):
        url = self.encode_url()
        json_resp = self.get_json(url)
        print json_resp
        self.save_json(json_resp)
        return json_resp['entities']

    def save_json(self, json_resp):
        print "Saving json to disk..."
        with open(u"ner_parse.json", "w") as f:
            json.dump(json_resp, f)

    def annotate_entity(self, entity):
        print "Annotatating entity " + entity['text']
        entity_name, entity_type = entity['text'], entity['type']
        entity_count = entity['count']

        if entity_type in constants.PERS:
            if not self.is_new_entity(entity_name, self.pers_found):
                return
            entity_type = "PER"
        elif entity_type in constants.ORGS:
            if not self.is_new_entity(entity_name, self.orgs_found):
                return
            entity_type = "ORG"
        elif entity_type in constants.LOCS:
            if not self.is_new_entity(entity_name, self.pers_found):
                return
            entity_type = "LOC"
        else:
            if not self.is_new_entity(entity_name, self.misc_found):
                return
            entity_type = "MISC"

        annotated_entity = u"[{0} {1}]".format(entity_type, entity_name)
        index = self.text.find(entity_name)
        count = 0
        while index != -1:
            count += 1
            index = self.text.find(entity_name, index + 1)
            if self.is_outside_entity(index):
                head, tail = self.text[0:index], self.text[index + len(entity_name):]
                subtext = self.text[index:index + len(entity_name)]
                self.text = head + subtext.replace(entity_name, annotated_entity) + tail
        print "Count ", count, entity_count


    def is_outside_entity(self, index):
        found = False
        i = index
        start, end = len(self.text), -1
        while not found and i < len(self.text):
            if self.text[i] == "]":
                end = i
                found = True
            if self.text[i] == "[":
                found = True
            i += 1
        found = False
        i = index

        while not found and i >= 0:
            if self.text[i] == "[":
                start = i
                found = True
            if self.text[i] == "]":
                found = True
            i -= 1
        if start <= end:
            return False

        return True

    def is_new_entity(self, entity_name, entities_found):
        if entity_name in entities_found:
            print "Already found entity {0}".format(entity_name)
            return False
        entities_found.add(entity_name)
        return True

    def encode_url(self):
        url = "".join([self.URL_ROOT, self.ENTITIES_PATH])
        print url
        return url

    def get_json(self, url):
        req = urllib2.Request(url=url,  data=urllib.urlencode(self.query),
                              headers={'User-Agent': constants.USER_AGENT,
                                       })

        response = urllib2.urlopen(req)
        data = json.load(response)
        print data
        return data

    def check_entity_types(self, entities):
        entity_types = [entity["type"] for entity in entities]
        unique_entites = set(entity_types)
        print "Entity types already known", constants.ENTITY_TYPES
        print "Unique entities types found ", unique_entites
        for entity_type in unique_entites:
            if entity_type not in constants.ENTITY_TYPES:
                print "Found new entity type {0}".format(entity_type)

    def eliminate_hyphenation(self):
        self.text = self.text.replace("-\n", "")


if __name__ == "__main__":
    path = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/716514 - Eric  Neumayer/" \
           "2001_The_human_development_index_and_sustainability_a_constructive_proposal.txt"
    with open(path, 'r') as f:
        text = f.read()
        text = unicode(text, errors='ignore')

    ner_alchemyapi = NerAlchemyApiCall(text, 100000)
    annotated_text = ner_alchemyapi.annotate_text()
    print annotated_text

    with open("annotation.txt", "w") as f:
        f.write(annotated_text)


