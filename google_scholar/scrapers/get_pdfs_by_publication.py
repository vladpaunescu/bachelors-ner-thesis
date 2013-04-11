#!/usr/bin/python
"""
    this class is used for retrieving pdf corresponding to one publication
    Language is English.
    URL format is http://scholar.google.com/scholar?start=0&q=IZA+Discussion+Papers&hl=en
"""
import os

import  urllib2
from lxml import etree


class PublicationPdfCrawler:
    ROOT_DIR = "out"
    URL_ROOT = "http://scholar.google.com/"
    USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 " \
                 "(KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31"

    def __init__(self, publication):
        self.url = self.URL_ROOT + "scholar"
        self.publication = publication
        self.path = "{0}/{1}".format(self.ROOT_DIR, publication)

        pubQuery = publication.replace(' ', '+')
        self.query = {"language":  {"key": "hl", "value": "en"},
                      "page":    {"key": "start", "value": "0"},
                      "publication": {"key": "q", "value": pubQuery}
                      }

    def getPdfs(self, pageCount = 1):
        url = self.buildUrl()
        pdfUrls = self.getPdfUrls(url)

        self.saveUrls(pdfUrls)

        return pdfUrls

    def buildUrl(self):
        language = self.query["language"]
        page = self.query["page"]
        publication = self.query["publication"]
        queries = [language, page, publication]

        queryStringList = ["{0}={1}".format(el["key"], el["value"]) for el in queries]
        queryString = "&".join(queryStringList)

        url = "".join([self.url, "?", queryString])
        print url
        return url

    def saveUrls(self, pdfUrls):

        self.createDirectory()
        print "Saving pdfs..."
        map(self.saveUrl, pdfUrls)

    def saveUrl(self, pdfUrl):
        print "Downloading pdf " + pdfUrl
        req = urllib2.Request(url=pdfUrl,
                              headers={'User-Agent': self.USER_AGENT})

        response = urllib2.urlopen(req)
        urlTitle = pdfUrl.split('/').pop()

        print "Saving to file " + urlTitle

        f = open("{0}/{1}".format(self.path, urlTitle), "wb")
        f.write(response.read())
        f.close()

    def createDirectory(self):
        print "Creating directory for {publication}".format(publication=self.publication)
        if not os.path.exists(self.path):
            os.makedirs(self.path)

    def getPdfUrls(self, url):
        req = urllib2.Request(url=url,
                              headers={'User-Agent': self.USER_AGENT})

        response = urllib2.urlopen(req)

        htmlparser = etree.HTMLParser()
        tree = etree.parse(response, htmlparser)
        pdfUrlSelector = "/html//div[@class='gs_md_wp']/a[1]"
        pdfTitleSelector = "/html//div/h3[@class='gs_rt']/a"
        pdfAuthorSelector = "/html//div/div[@class='gs_a']"

        #xpathselector = "/html/body/div/div[5]/div/div[2]/div/div/div/a'

        urlPdfResult = tree.xpath(pdfUrlSelector)
        titlePdfResult = tree.xpath(pdfTitleSelector)
        authorPdfResult = tree.xpath(pdfAuthorSelector)


        for element in urlPdfResult:
            print element.attrib["href"]

        for element in titlePdfResult:
            print element.text

        for element in authorPdfResult:
            if element.findall('a'):
                print [ el.text for el in element.findall('a')],
            print element.text


        return [element.attrib["href"] for element in urlPdfResult]


if __name__ == "__main__":

    pubPdfCrawler = PublicationPdfCrawler("American journal of public health")
    pubPdfCrawler.getPdfs()




