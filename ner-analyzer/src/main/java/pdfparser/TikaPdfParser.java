package pdfparser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: vlad
 * Date: 5/4/13
 * Time: 12:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class TikaPdfParser implements PdfParser{

    private String  _filename;

    public TikaPdfParser(String filename){
        _filename = filename;
    }

    @Override
    public String parse(){
        try(BufferedInputStream is = getInputStream()){
            Parser parser = new AutoDetectParser();
            StringWriter sw = new StringWriter();
            BodyContentHandler handler = new BodyContentHandler(sw);

            Metadata metadata = new Metadata();

            parser.parse(is, handler, metadata, new ParseContext());

            for (String name : metadata.names()) {
                String value = metadata.get(name);

                if (value != null) {
                    System.out.println("Metadata Name:  " + name);
                    System.out.println("Metadata Value: " + value);
                }
            }
            return sw.toString();

    } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TikaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private BufferedInputStream getInputStream() throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(new File(_filename)));
    }

    public static void main(String[] args){
        final String filename = "D:\\Shared\\Dropbox\\Work\\licenta-ner\\scrapers\\ms_academic\\out\\34111958\\17258_Chapter_8.pdf";
        TikaPdfParser parser = new TikaPdfParser(filename);
        String text  = parser.parse();
        System.out.println(text);
    }
}
