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

import urllib2
import urllib

# user imports
import constants
import sqlalchemy
from db.database import Storage, meta


class NerAlchemyApiCall:
    URL_ROOT = constants.ALCHEMY_API_ROOT_URL
    API_KEY = constants.APY_KEY
    ENTITIES_PATH = constants.ALCHEMY_API_ENTITIES_PATH

    def __init__(self, text, max_retrieve):
        self.text, self.max_retrieve = text, max_retrieve
        self.query = {"apikey": self.API_KEY,
                      "outputMode": "json",
                      "maxRetrieve": self.max_retrieve,
                      "text": self.text
        }

    def extract_named_entities(self):
        url = self.encode_url()
        json_resp = self.get_json(url)
        print json_resp
        return json_resp['entities']

    def encode_url(self):
        query = urllib.urlencode(self.query)
        url = "".join([self.URL_ROOT, self.ENTITIES_PATH])
        print url
        return url

    def get_json(self, url):

        # # make a string with the request type in it:
        # method = "POST"
        # # create a handler. you can specify different handlers here (file uploads etc)
        # # but we go for the default
        # handler = urllib2.HTTPHandler()
        # # create an openerdirector instance
        # opener = urllib2.build_opener(handler)
        # # build a request
        # request = urllib2.Request(url, data=urllib.urlencode(self.query))
        # # add any other information you want
        # request.add_header("Content-Type", 'application/json')
        # # overload the get method function with a small anonymous function...
        # request.get_method = lambda: method
        # # try it; don't forget to catch the result
        # try:
        #     connection = opener.open(request)
        # except urllib2.HTTPError,e:
        #     connection = e
        #
        # # check. Substitute with appropriate HTTP code.
        # if connection.code == 200:
        #     data = connection.read()
        #     print data
        #     return data
        # else:
        #     pass
        #     # handle the error case. connection.read() will still contain data
        #     # if any was returned, but it probably won't be of any use

        req = urllib2.Request(url=url,  data=urllib.urlencode(self.query),
                              headers={'User-Agent': constants.USER_AGENT,
                                       })

        response = urllib2.urlopen(req)
        data = json.load(response)
        print data
        return data


if __name__ == "__main__":
    path = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/716514 - Eric  Neumayer/" \
           "2001_The_human_development_index_and_sustainability_a_constructive_proposal.txt"
    with open(path, 'r') as f:
        text = f.read()

    ner_alchemyapi = NerAlchemyApiCall(text, 100)
    entities = ner_alchemyapi.extract_named_entities()
    for entity in entities:
        print entity

