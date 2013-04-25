#!/usr/bin/python
"""
    This module gets all social publications.
"""
from social_sciences_top import SocialSciencesTopPublications
from get_pdfs_by_publication import PublicationPdfCrawler


if __name__ == "__main__":
    socialSciences = SocialSciencesTopPublications()
    topPublications = socialSciences.getTopPublications()

    for publication in topPublications:
        print "Downloading pdfs for publication " + publication
        pubPdfCrawler = PublicationPdfCrawler(publication)
        pubPdfCrawler.getPdfs()
