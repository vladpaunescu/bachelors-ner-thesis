/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author vlad.paunescu
 */
public class MerriamWebesterHyphenationChecker {

    private final String ROOT_URL = "http://www.merriam-webster.com";
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            MerriamWebesterHyphenationChecker.class.getName());

    public MerriamWebesterHyphenationChecker() {
    }

    public boolean chekcHyphenatedWordExists(String word, String wordNoHyphen) {
        URL url;
        String query="";
        try {
            query = URLEncoder.encode(word, "UTF-8");
            log.info("Encoded query " + query);


            url = new URL(String.format("%s/thesaurus/%s", ROOT_URL, query));
            log.info("Url is " + url.getPath());

            URLConnection mc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    mc.getInputStream()));


            return processHtml(in, word, wordNoHyphen);

        } catch (MalformedURLException ex) {
            log.error(ex);
        } catch (IOException ex) {
            try {
                url = new URL(String.format("%s/dictionary/%s", ROOT_URL, query));
                log.info("Url is " + url.getPath());

                URLConnection mc = url.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        mc.getInputStream()));

                return processHtml(in, word, wordNoHyphen);

            } catch (MalformedURLException ex1) {
                log.error(ex1);
            } catch (IOException ex1) {
                log.error(ex1);
            }
        }

        return false;
    }

    private boolean processHtml(BufferedReader html, String word, String wordNoHyphen) {
        word = word.toLowerCase();
        wordNoHyphen = wordNoHyphen.toLowerCase();
        try {
            HtmlCleaner cleaner = new HtmlCleaner();

            TagNode node = cleaner.clean(html);
            TagNode nodes[] = node.getElementsByAttValue("class", "definition", true, true);
            if (nodes.length > 0) {
                if (nodes[0].getName().equals("div")) {
                    TagNode h1[] = nodes[0].getElementsByName("h1", false);
                    if (h1.length > 0) {
                        List children = h1[0].getAllChildren();
                        String correctWord = ((ContentNode) children.get(0)).getContent();
                        log.info("Corrected word " + correctWord);
                        if (wordNoHyphen.contains(correctWord)) {
                            return true;
                        }
                        return false;
                    }
                }
            }

        } catch (IOException ex) {
            log.error(ex);
        }
        return false;
    }

    public static void main(String[] args) {
        MerriamWebesterHyphenationChecker checker = new MerriamWebesterHyphenationChecker();
        System.out.println(checker.chekcHyphenatedWordExists("mis-sion", "mission"));
        System.out.println(checker.chekcHyphenatedWordExists("mis-sion", "mission"));
        System.out.println(checker.chekcHyphenatedWordExists("ap-peritive", "apperitive"));
    }
}
