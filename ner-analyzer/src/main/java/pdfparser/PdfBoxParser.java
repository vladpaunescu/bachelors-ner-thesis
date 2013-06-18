package pdfparser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: vlad Date: 5/4/13 Time: 12:03 AM To change this template use File | Settings | File Templates.
 */
public class PdfBoxParser implements PdfParser {

    private String _filename;

    public PdfBoxParser(String filename) {
        _filename = filename;
    }

    @Override
    public String parse() {
        PDDocument doc = null;
        String text = null;
        try {
            doc = PDDocument.load(_filename);

            PDFTextStripper stripper = new PDFTextStripper();


            text = stripper.getText(doc);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
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
        String filename = "D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/"
                + "716514 - Eric  Neumayer/2001_The_human_development_index_and_sustainability_a_constructive_proposal.pdf";
        //filename ="D:/Work/NLP/corpuses/ms_academic/out/22 - Social Science/1021291 - Wolff-Michael  Roth/Profess.pdf";

        PdfParser parser = new PdfBoxParser(filename);

        PdfParser tikaParser = new TikaPdfParser(filename);
        String text = tikaParser.parse();
        System.out.println(">>>>>>>>>>>>>>>");
        System.out.println("Text tika length " + text.length());

        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("outfilename.txt"), "UTF-8"));
        try {
            out.write(text);
        } finally {
            out.close();
        }

        System.out.println(text);
        System.out.println("Concateneate newlines");
        
        // remove newlines
        text = text.replaceAll("([^\\n-])\\n", "$1 ");
        
        // eliminate hyphenation
        MerriamWebesterHyphenationChecker checker = new MerriamWebesterHyphenationChecker();
        Pattern p = Pattern.compile("([^\\s]+)(-)((\\r\\n)|[\\n\\r])([^\\s!,\\.:!?'’]+)");
        Matcher m = p.matcher(text);
        
        while(m.find()){
           // System.out.println(m.group());
            System.out.println(m.group());
            String wordWithHyphen = m.group(1) + m.group(2) + m.group(5);
            String wordNoHyphen = m.group(1) + m.group(5);
            System.out.println(checker.chekcHyphenatedWordExists(wordWithHyphen, wordNoHyphen));
        }
        
        
//        System.out.println(text);
        text = parser.parse();

        Writer out2 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("outfilename_pdfbox.txt"), "UTF-8"));
        try {
            out2.write(text);
        } finally {
            out2.close();
        }

//
//        System.out.println(">>>>>>>>>>>>>>>");
//        Properties props = System.getProperties();
//        for (Object key : props.keySet()) {
//            System.out.println(props.get(key));
//        }
    }
}
