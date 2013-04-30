#!/usr/bin/python
"""
    This module gets all social publications.
"""
from get_top_authors_by_category import TopAuthorsByDomain
from get_publications_by_author import TopPublicationsByAuthor
import constants


if __name__ == "__main__":
    social_sciences = TopAuthorsByDomain(domain_id=constants.SOCIAL_SCIENCES_DOMAIN_ID)
    author_IDs = social_sciences.get_top_authors()


    for author_id in author_IDs:
        print "Downloading pdfs for author id {0}".format(author_id)
        publications = TopPublicationsByAuthor(author_id=author_id)
        publications.get_top_publications()

