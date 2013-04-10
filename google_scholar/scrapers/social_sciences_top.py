#!/usr/bin/python
"""
    this class is used for retrieving top publications in Social Sciences.
    Language is English.
    URL format is http://scholar.google.com/citations?view_op=top_venues&hl=en&vq=soc
"""
class SocialSciencesTopPublications:

    def __init__(self):
        self.urlRoot = "http://scholar.google.com/citations"
        self.query = {"language":  {"key" : "hl" , "value" : "en"},
                      "order" :    {"key" : "view_op" , "value" :"top_venues"},
                      "category" : {"key" : "vq" , "value" : "soc"}
                     }

    def getTopPublications(self):
        url = self.buildUrl()


    def buildUrl(self):
        language = self.query["language"]
        order = self.query["order"]
        category = self.query["category"]
        queries = [language, order, category]

        queryStringList = ["{0}={1}".format(el["key"], el["value"]) for el in queries]
        queryString = "&".join(queryStringList)

        url = "".join([self.urlRoot, "?", queryString])

        print url


if __name__ == "__main__":
    socialSciences = SocialSciencesTopPublications()
    socialSciences.buildUrl()


