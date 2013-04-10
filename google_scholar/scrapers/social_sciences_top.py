#!/usr/bin/python
"""
    this class is used for retrieving top publications in Social Sciences.
    Language is English.
    URL format is http://scholar.google.com/citations?view_op=top_venues&hl=en&vq=soc
"""
class SocialSciencesTopPublications:

    def __init__(self):
        self.urlRoot = "http://scholar.google.com/citations"
        self.query = {"language": {"hl" : "en"},
                      "order" :  {"view_op" : "top_venues"},
                      "category" : {"vq" : "soc"}
                     }

    def getTopPublications(self):
        url = self.buildUrl()


    def buildUrl(self):
        language

