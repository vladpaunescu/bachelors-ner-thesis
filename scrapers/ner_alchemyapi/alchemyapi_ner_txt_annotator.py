#!/usr/bin/python
"""
    This class is used to process the txt files.
"""

from os import  walk
from ner_alchemyapi import constants
from ner_alchemyapi.ner_alchemyapi_call import NerAlchemyApiCall


class NerTxtAnnotator:

    def __init__(self, root_path):
        self.root_path = root_path

    def annotate_documents(self):
        print "Root path is ", self.root_path
        for (dirpath, dirnames, filenames) in walk(self.root_path):
            # print dirpath, dirnames, filenames
            self.process_filenames(dirpath, filenames)

    def process_filenames(self, dirpath, filenames):
        text_files = filter(lambda filename: filename.endswith('.txt'), filenames)
        if text_files:
            print "Dir path is ", dirpath
            print "Text files", text_files

        for text_file in text_files:
            self.process_text_file(dirpath, text_file)

    def process_text_file(self, dirpath, text_file):
        filepath = "/".join([dirpath, text_file])
        print "File path is ", filepath
        with open(filepath, "r") as f:
            text = f.read()
            text = unicode(text, errors='ignore')
            ner_alchemyapi = NerAlchemyApiCall(text, constants.MAX_ENTITY_COUNT)
            annotated_text = ner_alchemyapi.annotate_text()


if __name__ == "__main__":
    root_path = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science"

    ner_annotator = NerTxtAnnotator(root_path)
    ner_annotator.annotate_documents()




