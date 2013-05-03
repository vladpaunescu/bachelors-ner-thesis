from __future__ import division
import sqlalchemy
import db_config


connection_string = 'mysql://%s:%s@%s/%s?charset=utf8&use_unicode=1' % (db_config.USER, db_config.PASSWORD,
                                                                        db_config.HOST, db_config.DB)
engine = sqlalchemy.create_engine(connection_string, pool_recycle=3600, pool_size=5, max_overflow=10)
meta = sqlalchemy.MetaData()
meta.reflect(bind=engine)


class Storage(object):
    def __init__(self):
        self.conn = None

    def connect(self):
        self.conn = engine.connect()

    def disconnect(self):
        try:
            self.conn.close()
            self.conn = None
        except Exception, e:
            print e

    def execute(self, query):
        try:
            result = self.conn.execute(query)
            return result
        except Exception, e:
            print e


if __name__ == "__main__":

    storage = Storage()
    storage.connect()
    domains = meta.tables['ms_academic_domains']
    query = sqlalchemy.select([domains.c.id, domains.c.domain_id, domains.c.name])
    results = storage.execute(query)
    for result in results:
        print result['name']

    storage.disconnect()

