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
import sqlalchemy
import sys

# user imports
import constants


from scrapers.db.database import Storage, meta



class TopPublicationsByAuthor:

    URL_ROOT = constants.MS_ACADEMIC_URL
    APP_ID = constants.APP_ID
    ROOT_DIR = "out"

    def __init__(self, author_id):
        self.author_id = author_id
        self.start_idx = 0
        self.end_idx = 99
        self.path = "{0}/{1}".format(self.ROOT_DIR, author_id)
        self.author_name = ""

        self.query = {"AppId": constants.APP_ID,
                      "ResultObjects": "Publication",
                      "AuthorID": self.author_id,
                      "StartIdx": self.start_idx,
                      "EndIdx": self.end_idx
                      }

    def set_author_name(self, author_name):
        self.author_name = author_name
        self.path += " - {0}".format(author_name)
        print "Directory path is {0}".format(self.path)

    def get_top_publications(self):
        url = self.encode_url()
        json_resp = self.get_json(url)
        publications = self.parse_publications(json_resp)
        return publications

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

    def parse_publications(self, json_resp):
        publications = json_resp['d']['Publication']['Result']
        for publication in publications:
            print publication

        return filter(lambda el: el['FullVersionURL'] != '' and el['FullVersionURL'], publications)

    def save_to_db(self, publications):
        storage = Storage()
        storage.connect()
        print "Saving publications for author {0} - {1} to db...".format(self.author_id, self.author_name)
        map(lambda publication: self.save_publication_to_db(publication, storage), publications)
        storage.disconnect()

    def save_publication_to_db(self, publication, storage):
        publications_table = meta.tables['ms_academic_publications']
        insert_data = {"title": publication['Title'],
                       "year": publication['Year'],
                       "full_version_url": publication['FullVersionURL'][0],
                       "abstract": publication['Abstract'],
                       "citation_count": publication['CitationCount'],
                       "reference_count": publication['ReferenceCount'],
                       "publication_id": publication['ID'],
                       "author_id": self.author_id
                       }

        query = sqlalchemy.insert(publications_table, insert_data)
        print query
        storage.execute(query)

    def save_to_disk(self, publications):
        print "Saving publications for author {0} - {1} to disk".format(self.author_id, self.author_name)
        self.create_directory()
        map(self.save_publication, publications)

    def create_directory(self):
        print "Creating directory for {author_id}".format(author_id=self.author_id)
        print self.path
        if not os.path.exists(self.path):
            os.makedirs(self.path)

    def save_publication(self, publication):
        url = publication['FullVersionURL'][0]

        try:
            print "Downloading publication {0} from url {1}".format(publication['Title'], url)
        except UnicodeEncodeError as e:
            print "Unicode error " + e.message

        req = urllib2.Request(url=url,
                              headers={'User-Agent': constants.USER_AGENT})
        try:
            response = urllib2.urlopen(req)
            # check content type:
            content_type = response.info().getheader('Content-Type')
            print content_type
            if 'application/pdf' in content_type:
                try:
                    print "Publication {0} is PDF file. Saving it to disk...".format(publication['Title'])
                except UnicodeEncodeError as e:
                    print "Unicode error " + e.message

                filename = self.save_pdf(response, url)
                update_values = {"filename": filename, "content_type": content_type, "downloaded": "1"}
                self.update_entry_in_db(publication, update_values)

            else:
                update_values = {"content_type": content_type}
                self.update_entry_in_db(publication, update_values)

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
        response.close()
        return url_title

    def update_entry_in_db(self, publication, update_values):
        publication_id = publication['ID']
        publication_title = publication['Title']

        try:
            print "Updating publication {0} - {1} as in database...".format(publication_id, publication_title)
        except UnicodeEncodeError as e:
            print "Unicode error " + e.message

        storage = Storage()
        storage.connect()
        publications_table = meta.tables['ms_academic_publications']
        query = sqlalchemy.update(publications_table).where(publications_table.c.publication_id == publication_id)\
            .values(update_values)
        print query
        storage.execute(query)
        storage.disconnect()


if __name__ == "__main__":
    publications_scraper = TopPublicationsByAuthor(author_id=1759605)
    publications = publications_scraper.get_top_publications()
    publications_scraper.save_to_db(publications)
    publications_scraper.save_to_disk(publications)



