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
        Session session = NerHibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<MsAcademicAuthors> result = session.createQuery("from MsAcademicAuthors").list();
        session.getTransaction().commit();
        return result;
    }
    
    public static void main(String[] args){
        DbTests dbTests = new DbTests();
        List<MsAcademicAuthors> authors = dbTests.listDomains();
        for (MsAcademicAuthors author : authors){
            System.out.println(author.getFirstName());
        }
        
    }
}

