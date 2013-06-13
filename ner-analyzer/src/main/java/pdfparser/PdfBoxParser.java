package pdfparser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;
import org.apache.pdfbox.encoding.Encoding;

/**
 * Created with IntelliJ IDEA.
 * User: vlad
 * Date: 5/4/13
 * Time: 12:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class PdfBoxParser implements PdfParser {

    private String  _filename;
    

    public PdfBoxParser(String filename){
        _filename = filename;        
    }

    @Override
    public String parse(){
        PDDocument doc = null;
        String text = null;
        try {
            doc = PDDocument.load(_filename);
            
            PDFTextStripper stripper = new PDFTextStripper();
      
            
            text = stripper.getText(doc);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return text;
    }

    public static void main(String[] args) throws IOException {
        String filename = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/" +
                "716514 - Eric  Neumayer/2001_The_human_development_index_and_sustainability_a_constructive_proposal.pdf";
        filename ="D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/1021291 - Wolff-Michael  Roth/Profess.pdf";
        
        PdfParser parser = new PdfBoxParser(filename);

        PdfParser tikaParser = new TikaPdfParser(filename);
        String text  = tikaParser.parse();
        System.out.println(">>>>>>>>>>>>>>>");
        System.out.println("Text tika length " + text.length());
        PrintWriter pw1 = new PrintWriter("parser_tika.txt");
        PrintWriter pw2 = new PrintWriter("parser_pdfbox.txt");
        System.out.println(text);
        pw1.println(text);
        text = parser.parse();
        System.out.println(">>>>>>>>>>>>>>>");
        System.out.println(text);
        pw2.println(text);
        pw1.close();
        pw2.close();

    }
}
