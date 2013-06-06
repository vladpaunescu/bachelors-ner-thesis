#!/usr/bin/python
"""
    This module gets all social publications.
"""

from get_top_authors_by_category import TopAuthorsByDomain
from get_publications_by_author import TopPublicationsByAuthor
import constants


def download_domain(domain_id, domain_name, resume_from_author_id=None):
    social_sciences = TopAuthorsByDomain(domain_id, domain_name)
    authors = social_sciences.get_top_authors()
    domain_dir = "{0} - {1}".format(domain_id, domain_name)
    if not resume_from_author_id:
        social_sciences.save_to_db(authors)
        map(lambda author: download_publications(author, domain_dir), authors)
    else:
        #sys.stdout = open('arts_and_humanities_log.txt', 'a')
        print "Resuming from author id {0}".format(resume_from_author_id)
        print "Authors"
        print authors
        indices = [i for i, el in enumerate(authors) if el['ID'] == resume_from_author_id]
        print "Resuming indices"
        print indices
        authors = authors[indices[0]:]
        map(lambda author: download_publications(author, domain_dir), authors)


def download_publications(author, domain_dir, ):
    author_name = " ".join([author['FirstName'], author['MiddleName'], author['LastName']])
    author_id = author['ID']

    author_name.encode('utf-8')

    print u"Downloading pdfs for author {0} - {1}".format(author_id, author_name)

    publications_scraper = TopPublicationsByAuthor(author_id=author_id, domain_dir=domain_dir)
    publications_scraper.set_author_name(author_name)
    publications = publications_scraper.get_top_publications()

    publications_scraper.save_to_db(publications)
    publications_scraper.save_to_disk(publications)


if __name__ == "__main__":
    #sys.stdout = open('publications_log_social_science.txt', 'a')
    #download_domain(constants.SOCIAL_SCIENCES_DOMAIN_ID, "Social Science",
    #                resume_from_author_id=2034223)

    #sys.stdout = open('publications_log_arts_and_humanities.txt', 'w')
    #download_domain(constants.ARTS_AND_HUMANITIES_DOMAIN_ID, "Arts & Humanities",
    #                resume_from_author_id=43306259)

    #sys.stdout = open('publications_log_computer_science.txt', 'w')
    download_domain(constants.COMPUTER_SCIENCE_DOMAIN_ID, "Computer Science",
                    resume_from_author_id=469846)

