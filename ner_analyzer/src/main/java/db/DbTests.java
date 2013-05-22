/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.util.List;
import org.hibernate.Session;

/**
 *
 * @author vlad.paunescu
 */
public class DbTests {

    public List<MsAcademicAuthors> listDomains() {
        Session session = NerHibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List<MsAcademicAuthors> result = session.createQuery("SELECT NEW MsAcademicAuthors(a.first_Name) from MsAcademiAuthors as a").list();
        session.getTransaction().commit();
        return result;
    }
    
    public static void main(String[] args){
        DbTests dbTests = new DbTests();
        List domains = dbTests.listDomains();
        System.out.println(domains);
        
    }
}

