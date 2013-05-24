/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;
import pdf_parser.DirectoryPdfScanner;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author vlad.paunescu
 */
public class NerHibernateUtil {

    private static final SessionFactory sessionFactory;
    static Logger log = Logger.getLogger(
                      NerHibernateUtil.class.getName());
    
    static {
        try {
            log.info("Trying to create a test connection with the database.");
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(configuration
                            .getProperties());
            sessionFactory = configuration
                            .buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
            Session session = sessionFactory.openSession();
            log.info("Test connection with the database created successfuly.");
        } catch (Throwable ex) {
            // Log the exception. 
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
