#!/usr/bin/python
"""
    This class is used for publications corresponding to an author id
    URL format is
   http://academic.research.microsoft.com/json.svc/search?AppId=c4442c4f-22a6-461d-bca9-f199020a24ce&
   ResultObjects=Publication&AuthorID=1759605&StartIdx=0&EndIdx=99
"""
import json
import os

import urllib2
import urllib

# user imports
import constants
import sys


class TopPublicationsByAuthor:

    URL_ROOT = constants.MS_ACADEMIC_URL
    APP_ID = constants.APP_ID
    ROOT_DIR = "out"

    def __init__(self, author_id):
        self.author_id = author_id
        self.start_idx = 0
        self.end_idx = 99
        self.path = "{0}/{1}".format(self.ROOT_DIR, author_id)

        self.query = {"AppId": constants.APP_ID,
                      "ResultObjects": "Publication",
                      "AuthorID": self.author_id,
                      "StartIdx": self.start_idx,
                      "EndIdx": self.end_idx
                      }

    def get_top_publications(self):
        url = self.encode_url()
        json_resp = self.get_json(url)
        publication_urls = self.parse(json_resp)
        publication_urls = [url[0] for url in publication_urls]
        self.save_urls(publication_urls)

    def encode_url(self):
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
        publications = json_resp['d']['Publication']['Result']
        for publication in publications:
            print publication

        return filter(lambda el: el != '' and el, [publication['FullVersionURL'] for publication in publications])

    def save_urls(self, urls):
        print urls
        self.create_directory()
        map(self.save_url, urls)

    def create_directory(self):
        print "Creating directory for {author_id}".format(author_id=self.author_id)
        if not os.path.exists(self.path):
            os.makedirs(self.path)

    def save_url(self, url):
        print "Downloading url " + url
        req = urllib2.Request(url=url,
                              headers={'User-Agent': constants.USER_AGENT})
        try:
            response = urllib2.urlopen(req)
            # check content type:
            content_type = response.info().getheader('Content-Type')
            print content_type
            if content_type in ['application/pdf']:
                self.save_pdf(response, url)
                downloaded_urls.append(url)

        except urllib2.HTTPError as e:
            print e.code
            print e.read
        except urllib2.URLError as e:
            print e.reason
        except:
            print "Unexpected error:", sys.exc_info()[0]


    def save_pdf(self, response, pdf_url):
        url_title = pdf_url.split('/').pop()
        print "Saving pdf to file " + url_title
        with open("{0}/{1}".format(self.path, url_title), "wb") as f:
            f.write(response.read())


if __name__ == "__main__":
    publications = TopPublicationsByAuthor(author_id=1759605)
    publications.get_top_publications()



