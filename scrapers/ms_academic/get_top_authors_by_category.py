#!/usr/bin/python
"""
    This class is used for retrieving top authors by category.
    URL format is
    http://academic.research.microsoft.com/json.svc/search?AppId=c4442c4f-22a6-461d-bca9-f199020a24ce&
    &ResultObjects=Author&DomainID=22&StartIdx=0&EndIdx=99
"""
import json
import os

import urllib2
import urllib

# user imports
import sqlalchemy
from db.database import Storage, meta
from ms_academic import constants


class TopAuthorsByDomain:

    URL_ROOT = constants.MS_ACADEMIC_URL
    APP_ID = constants.APP_ID
    ROOT_DIR = "out"

    def __init__(self, domain_id, domain_name):
        self.domain_id = domain_id
        self.start_idx = 0
        self.end_idx = 99
        self.path = "{0}/{1} - {2}".format(self.ROOT_DIR, domain_id, domain_name)
        self.query = {"AppId": constants.APP_ID,
                      "ResultObjects": "Author",
                      "DomainID": self.domain_id,
                      "StartIdx": self.start_idx,
                      "EndIdx": self.end_idx
                      }

    def get_top_authors(self):
        url = self.encode_url()
        json_resp = self.get_json(url)
        self.save_json(json_resp)
        authors = self.parse(json_resp)
        return authors

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

    def save_json(self, json_resp):
        self.create_directory()
        print "Saving json to disk..."
        with open(u"{0}/json/response.json".format(self.path), "w") as f:
            json.dump(json_resp, f)

    def parse(self, json_resp):
        authors = json_resp['d']['Author']['Result']
        return authors

    def save_to_db(self, authors):
        storage = Storage()
        storage.connect()
        print "Saving authors to db..."
        map(lambda author: self.save_author_to_db(author, storage), authors)
        storage.disconnect()

    def save_author_to_db(self, author, storage):
        authors_table = meta.tables['ms_academic_authors']
        insert_data = {"first_name": author['FirstName'],
                       "last_name": author['LastName'],
                       "middle_name": author['MiddleName'],
                       "citation_count": author['CitationCount'],
                       "publication_count": author['PublicationCount'],
                       "g_index": author['GIndex'],
                       "h_index": author['HIndex'],
                       "author_id": author['ID'],
                       "homepage_url": author['HomepageURL'],
                       "display_photo_url": author['DisplayPhotoURL'],
                       "domain_id": self.domain_id
                       }

        query = sqlalchemy.insert(authors_table, insert_data)
        print query
        storage.execute(query)

    def create_directory(self):
        print "Creating directory for {0}".format(self.domain_id)
        dirs = u"{0}/json".format(self.path)
        self.print_unicode(dirs)
        if not os.path.exists(dirs):
            os.makedirs(dirs)

    def print_unicode(self, string):
        try:
            print string
        except UnicodeEncodeError as e:
            print u"Unicode Error: {0}".format(e.encoding)

if __name__ == "__main__":
    social_sciences = TopAuthorsByDomain(domain_id=22, domain_name="Social Science")
    authors = social_sciences.get_top_authors()
    print authors
    social_sciences.save_to_db(authors)


