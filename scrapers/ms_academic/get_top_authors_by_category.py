#!/usr/bin/python
"""
    This class is used for retrieving top authors by category.
    URL format is
    http://academic.research.microsoft.com/json.svc/search?AppId=c4442c4f-22a6-461d-bca9-f199020a24ce&
    &ResultObjects=Author&DomainID=22&StartIdx=0&EndIdx=99
"""
import json

import urllib2
import urllib

# user imports
import constants

class TopAuthorsByDomain:

    URL_ROOT = constants.MS_ACADEMIC_URL
    APP_ID = constants.APP_ID

    def __init__(self, domain_id ):
        self.domain_id = domain_id
        self.start_idx = 0
        self.end_idx = 99
        self.query = {
                      "AppId": constants.APP_ID,
                      "ResultObjects": "Author",
                      "DomainID": self.domain_id,
                      "StartIdx": self.start_idx,
                      "EndIdx": self.end_idx
                     }

    def getTopAuthors(self):
        url = self.encodeUrl()
        json_resp = self.get_json(url)
        self.parse(json_resp)

    def encodeUrl(self):
        query = urllib.urlencode(self.query)
        url = "".join([self.URL_ROOT, "?", query])
        print url
        return url

    def get_json(self, url):
        req = urllib2.Request(url=url,
                              headers={'User-Agent': constants.USER_AGENT})

        response = urllib2.urlopen(req)
        data = json.load(response)
        print data
        return data

    def parse(self, json_resp):
        authors = json_resp['d']['Author']['Result']
        for author in authors:
            print author


if __name__ == "__main__":
    socialSciences = TopAuthorsByDomain(domain_id=22)
    socialSciences.getTopAuthors()


