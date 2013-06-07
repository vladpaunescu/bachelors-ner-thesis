#!/usr/bin/python
ALCHEMY_API_ROOT_URL = "http://access.alchemyapi.com/calls/text/"
APY_KEY = "b92c95cc3bb59ff63ee0fc358f251f20b8e5c3e9"
USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 "\
                 "(KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31"
ALCHEMY_API_ENTITIES_PATH="TextGetRankedNamedEntities"

PERS = [u'Person']
ORGS = [u'Company', u'Organization', u'FieldTerminology', u'PrintMedia']
LOCS = [u'City', u'Country', u'State', u'StateOrCounty', u'StateOrCountry']


ENTITY_TYPES = PERS + ORGS + LOCS
MAX_ENTITY_COUNT = 100000



