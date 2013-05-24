/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdf_parser;

import db.MsAcademicPublications;
import db.NerHibernateUtil;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author vlad.paunescu
 */
public class DirectoryPdfScanner {
    
   static Logger log = Logger.getLogger(
                      DirectoryPdfScanner.class.getName());
   
   
   
    
    private String directoryRoot;
    
    public DirectoryPdfScanner(String dirName){
        directoryRoot = dirName;
        BasicConfigurator.configure();
        
    }
    
    public List<File> scanForPdfs(){       
       return getAllPdfs(new File(directoryRoot));       
    }
    
    private List<File> getAllPdfs(File root){
        
        List<File> pdfs =  new LinkedList<>(Arrays.asList(getPdfs(root)));
        File[] dirs = getDirectories(root); 
        log.debug("Pdfs count are " + pdfs.size());
        log.debug("Directories are");
        for(File dir : dirs){
           log.debug(dir);
           pdfs.addAll(getAllPdfs(dir));
        }
       
       return pdfs;
    }
    
    private File[] getDirectories(File parent){
        File[] dirs = parent.listFiles(new FileUtils.DirectoryFileFilter());
        return dirs;
    }
    
    private File[] getPdfs(File parent){
        File[] pdfs = parent.listFiles(new FileUtils.PdfFileFilter());
        return pdfs;
    } 
   
    
  
    
    public static void main(String[] args){
        //String rootDir = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science";
        String rootDir = "D:/Facultate/Anul 4/Licenta/licenta-ner/scrapers/ms_academic/out/22 - Social Science";
        DirectoryPdfScanner pdfScanner = new DirectoryPdfScanner(rootDir);
        List<File> pdfs = pdfScanner.scanForPdfs();
        System.out.println("Total number of pdfs " + pdfs.size());
        
        int nonParsable = 0;
        for (File pdf : pdfs){
            PdfParser pdfParser = new PdfBoxParser(pdf.getAbsolutePath());
            String text = pdfParser.parse();
            
            if (text == null){
                log.warn("Non parsable pdf " + pdf.getAbsolutePath());
                ++nonParsable;
            }
            System.out.println("Parsed " + pdf.getAbsolutePath());
            
            System.out.println("Updating pdf with name " + pdf.getName());            
            Session session = NerHibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();            
            List<MsAcademicPublications> publications = session.createCriteria(MsAcademicPublications.class)
                    .add(Restrictions.eq("filename", pdf.getName())).list();
            System.out.println("Publications retrieved from db, count " + publications.size());
            if (publications.size() > 1) {
                System.out.println("More than one publication found " + publications.size());
            }
            
            for(MsAcademicPublications publication : publications){
                publication.setContent(text);
                session.update(publication);
            }
            System.out.println("Successfully updated");
            session.getTransaction().commit();  
            session.close();

            
            
        }
        log.info(String.format("Finished parsing %d PDFs. %d unparsed", 
                pdfs.size(), nonParsable));
                
    }
}
    
    
    
