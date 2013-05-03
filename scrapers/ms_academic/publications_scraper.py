#!/usr/bin/python
"""
    This module gets all social publications.
"""

import sys

from get_top_authors_by_category import TopAuthorsByDomain
from get_publications_by_author import TopPublicationsByAuthor
import constants



def download_social_sciences():
    social_sciences = TopAuthorsByDomain(domain_id=constants.SOCIAL_SCIENCES_DOMAIN_ID)
    authors = social_sciences.get_top_authors()
    # social_sciences.save_to_db(authors)

    map(download_publications, authors)

def download_publications(author):
    author_name = " ".join([author['FirstName'], author['MiddleName'], author['LastName']])
    author_id = author['ID']
    print "Downloading pdfs for author {0} - {1}".format(author_id, author_name)
    publications_scraper = TopPublicationsByAuthor(author_id=author_id)
    publications_scraper.set_author_name(author_name)
    publications = publications_scraper.get_top_publications()

    publications_scraper.save_to_db(publications)
    publications_scraper.save_to_disk(publications)

if __name__ == "__main__":

    sys.stdout = open('publications_log.txt', 'w')
    download_social_sciences()


