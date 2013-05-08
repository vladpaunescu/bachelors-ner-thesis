#!/usr/bin/python
"""
    This class is used for publications corresponding to an author id
    URL format is
   http://academic.research.microsoft.com/json.svc/search?AppId=c4442c4f-22a6-461d-bca9-f199020a24ce&
   ResultObjects=Publication&AuthorID=1759605&StartIdx=0&EndIdx=99
"""
import json
import os
import socket

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

    def __init__(self, author_id, domain_dir):
        self.author_id = author_id
        self.start_idx = 0
        self.end_idx = 99
        self.domain_dir = domain_dir
        self.path = "{0}/{1}/{2}".format(self.ROOT_DIR, domain_dir, author_id)
        self.author_name = ""

        self.query = {"AppId": constants.APP_ID,
                      "ResultObjects": "Publication",
                      "AuthorID": self.author_id,
                      "StartIdx": self.start_idx,
                      "EndIdx": self.end_idx
                      }

    def set_author_name(self, author_name):
        self.author_name = author_name
        self.path += u" - {0}".format(author_name)
        self.print_unicode(u"Directory path is {0}".format(self.path))

    def get_top_publications(self):
        url = self.encode_url()
        try:
            json_resp = self.get_json(url)
            self.create_directory()
            self.save_json(json_resp)
            publications = self.parse_publications(json_resp)
            return publications
        except socket.error as e:
            print e
        return []

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
        self.print_unicode(u"Saving publications for author {0} - {1} to db...".format(self.author_id, self.author_name))

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
        self.print_unicode(u"Saving publications for author {0} - {1} to disk".format(self.author_id,
                                                                                      self.author_name))
        map(self.save_publication, publications)

    def create_directory(self):
        print "Creating directory for {author_id}".format(author_id=self.author_id)
        dirs = u"{0}/json".format(self.path)
        self.print_unicode(dirs)
        if not os.path.exists(dirs):
            os.makedirs(dirs)

    def save_publication(self, publication):
        url = publication['FullVersionURL'][0]
        self.print_unicode(u"Downloading publication {0} from url {1}".format(publication['Title'], url))

        req = urllib2.Request(url=url,
                              headers={'User-Agent': constants.USER_AGENT})
        try:
            response = urllib2.urlopen(req)
            # check content type:
            content_type = response.info().getheader('Content-Type')
            print content_type
            if 'application/pdf' in content_type:
                self.print_unicode(u"Publication {0} is PDF file. Saving it to disk...".format(publication[
                    'Title']))
                filename = self.save_pdf(response, url)
                update_values = {"filename": filename, "content_type": content_type, "downloaded": "1"}
                self.update_entry_in_db(publication, update_values)

            else:
                update_values = {"content_type": content_type}
                self.update_entry_in_db(publication, update_values)
                response.close()

        except urllib2.HTTPError as e:
            print e.code
            update_values = {"content_type": e.code}
            self.update_entry_in_db(publication, update_values)

        except urllib2.URLError as e:
            print e.reason
            update_values = {"content_type": e.reason}
            self.update_entry_in_db(publication, update_values)
        except:
            print "Unexpected error:", sys.exc_info()[0]

    def save_pdf(self, response, pdf_url):
        url_title = pdf_url.split('/').pop()

        if not url_title.endswith('.pdf'):
            self.print_unicode(u"Adding pdf extension to {0}".format(url_title))
            url_title += '.pdf'
            self.print_unicode(u"Pdf filename is {0}".format(url_title))

        self.print_unicode(u"Saving pdf to file {0}".format(url_title))
        with open(u"{0}/{1}".format(self.path, url_title), "wb") as f:
            f.write(response.read())
        response.close()
        return url_title

    def save_json(self, json_resp):
        print "Saving json to disk..."
        with open(u"{0}/json/response.json".format(self.path), "w") as f:
            json.dump(json_resp, f)

    def update_entry_in_db(self, publication, update_values):
        publication_id = publication['ID']
        publication_title = publication['Title']

        self.print_unicode(u"Updating publication {0} - {1} as in database...".format(publication_id,
                                                                                      publication_title))
        storage = Storage()
        storage.connect()
        publications_table = meta.tables['ms_academic_publications']
        query = sqlalchemy.update(publications_table).where(publications_table.c.publication_id == publication_id)\
            .values(update_values)
        print query
        storage.execute(query)
        storage.disconnect()

    def print_unicode(self, string):
        try:
            print string
        except UnicodeEncodeError as e:
            print u"Unicode Error: {0}".format(e.encoding)


if __name__ == "__main__":
    publications_scraper = TopPublicationsByAuthor(author_id=1759605)
    publications = publications_scraper.get_top_publications()
    publications_scraper.save_to_db(publications)
    publications_scraper.save_to_disk(publications)



