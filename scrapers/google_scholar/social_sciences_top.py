#!/usr/bin/python
"""
    this class is used for retrieving top publications in Social Sciences.
    Language is English.
    URL format is http://scholar.google.com/citations?view_op=top_venues&hl=en&vq=soc
"""

import  urllib2
from BeautifulSoup import BeautifulSoup
from lxml import etree

class SocialSciencesTopPublications:

    URL_ROOT = "http://scholar.google.com/"
    USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 "\
                 "(KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31"

    def __init__(self):
        self.url = self.URL_ROOT + "citations"
        self.query = {"language":  {"key" : "hl", "value" : "en"},
                      "order":    {"key" : "view_op" , "value" :"top_venues"},
                      "category": {"key" : "vq", "value" : "soc"}
                     }

    def getTopPublications(self):
        url = self.buildUrl()
        papers = self.parse(url)
        print papers
        return papers


    def buildUrl(self):
        language = self.query["language"]
        order = self.query["order"]
        category = self.query["category"]
        queries = [language, order, category]

        queryStringList = ["{0}={1}".format(el["key"], el["value"]) for el in queries]
        queryString = "&".join(queryStringList)

        url = "".join([self.url, "?", queryString])
        print url
        return url

    def parse(self, url):
        req = urllib2.Request(url=url,
            headers={'User-Agent': self.USER_AGENT})

        response = urllib2.urlopen(req)
        htmlparser = etree.HTMLParser()
        tree = etree.parse(response, htmlparser)
        xpathselector = "/html/body/div/div[5]/div[2]/table/tr/td[2]"
        result = tree.xpath(xpathselector)

        for element in result:
            print element.text

        return [element.text for element in result]


if __name__ == "__main__":
    socialSciences = SocialSciencesTopPublications()
    socialSciences.getTopPublications()


