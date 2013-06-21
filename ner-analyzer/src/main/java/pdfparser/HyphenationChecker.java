/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfparser;

import db.HyphenatedWords;
import db.NerHibernateUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DefaultPaths;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

/**
 *
 * @author vlad.paunescu
 */
public class HyphenationChecker {

    private static final String MERIAM_WEBSTER_ROOT = "http://www.merriam-webster.com";
    private static final String FREE_DICTIONARY_ROOT = "http://www.thefreedictionary.com";
    private static final String DICTIONARY_REFERENCE_ROOT = "http://dictionary.reference.com/browse";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36";
    private static final long DELAY = 2000;
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            HyphenationChecker.class.getName());
    private Set<String> _correctWords;
    private Set<String> _wordsWithHyphen;
    private StanfordCoreNLP _snlp;
    private String _currentNer;
    private long _previousTime;

    public HyphenationChecker() {
        _correctWords = new HashSet<>();
        _wordsWithHyphen = new HashSet<>();
        System.setProperty("http.agent", "");

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("ner.model", DefaultPaths.DEFAULT_NER_CONLL_MODEL);
        _snlp = new StanfordCoreNLP(props);
        _previousTime = System.currentTimeMillis();
    }

    public boolean chekcHyphenatedWordExists(String word, String wordNoHyphen) {
        String lemmatizedWord = lemmatizeWord(wordNoHyphen);
        lemmatizedWord = lemmatizedWord.toLowerCase();

        log.info(String.format("Checking if word %s is in local dictionary.", lemmatizedWord));
        if (_correctWords.contains(lemmatizedWord)) {
            log.info(String.format("Word %s in cache.", lemmatizedWord));
            return true;
        }

        log.info(String.format("Checking if word %s is in database.", lemmatizedWord));
        if (isInDatabase(lemmatizedWord)) {
            _correctWords.add(lemmatizedWord);
            return true;
        }

        if (_currentNer.equals("PERSON") || _currentNer.equals("ORGANIZATION") || _currentNer.equals("LOCATION")) {
            log.info(String.format("%s is a %s.", wordNoHyphen, _currentNer));
            _correctWords.add(_currentNer.toLowerCase());
            insertWordIntoDb(word, lemmatizedWord);
            return true;
        }

        log.info(String.format("Checking if the word with hyphen %s is in local dictionary.", word));
        if (_wordsWithHyphen.contains(word.toLowerCase())) {
            log.info(String.format("Word %s in cache.", word));
            return false;
        }

        log.info(String.format("Checking if the word with hyphen %s is in database", word));
        if (isInDatabase(word.toLowerCase())) {
            log.info(String.format("Word %s in cache.", word));
            return false;
        }



        if (checkOnDictionaryReference(lemmatizedWord)) {
            insertWordIntoDb(word, lemmatizedWord);
            return true;
        }

        if (checkOnMeriamDictionary(lemmatizedWord)) {
            insertWordIntoDb(word, lemmatizedWord);
            return true;
        }

        log.info("Waiting for Free Dictionary.");
        long currentTime = System.currentTimeMillis();
        long elapsedTime = Math.min(currentTime - _previousTime, DELAY);
        log.info(String.format("Elapsed time %.2f s. Waiting for %.2f s.", elapsedTime/1000., (DELAY - elapsedTime) /1000.));
        try {
            Thread.sleep(DELAY - elapsedTime);
        } catch (InterruptedException ex) {
            log.error(ex);
        }

        if (checkOnFreeDctionary(lemmatizedWord)) {
            insertWordIntoDb(word, lemmatizedWord);
            return true;
        }

        _wordsWithHyphen.add(word.toLowerCase());
        insertWordIntoDb(word, word.toLowerCase());

        return false;
    }

    private boolean checkOnDictionaryReference(String lemmatizedWord) {
        log.info(String.format("Checking lemma %s on Dictionary Reference for existance.", lemmatizedWord));

        try (BufferedReader in = getUrlResponse(DICTIONARY_REFERENCE_ROOT, lemmatizedWord)) {
            return processDictionaryReferenceHtml(in, lemmatizedWord);
        } catch (IOException ex) {
            log.error(ex);
        }

        return false;
    }

    private boolean checkOnFreeDctionary(String lemmatizedWord) {
        log.info(String.format("Checking lemma %s on Free Dictionary for existance.", lemmatizedWord));

        try (BufferedReader in = getUrlResponse(FREE_DICTIONARY_ROOT, lemmatizedWord)) {
            _previousTime = System.currentTimeMillis();
            return processFreeDictionaryHtml(in, lemmatizedWord);
        } catch (IOException ex) {
            log.error(ex);
        }

        return false;
    }

    private boolean checkOnMeriamDictionary(String lemmatizedWord) {
        log.info(String.format("Checking lemma %s on Meriam-WebsterDictionary for existance.", lemmatizedWord));
        String urlRoot = String.format("%s/thesaurus", MERIAM_WEBSTER_ROOT);

        try (BufferedReader in = getUrlResponse(urlRoot, lemmatizedWord)) {
            return processMeriamHtml(in, lemmatizedWord);

        } catch (IOException ex) {
            urlRoot = String.format("%s/dictionary", MERIAM_WEBSTER_ROOT);
            try (BufferedReader in = getUrlResponse(urlRoot, lemmatizedWord)) {
                return processMeriamHtml(in, lemmatizedWord);

            } catch (IOException ex1) {
                log.error(ex1);
            }
        }

        return false;
    }

    private BufferedReader getUrlResponse(String root, String query) throws IOException {
        try {
            query = URLEncoder.encode(query, "UTF-8");
            log.info("Encoded query " + query);
            URL url = new URL(String.format("%s/%s", root, query));
            log.info("Url is " + url);

            URLConnection c = url.openConnection();
            c.setRequestProperty("User-Agent", USER_AGENT);

            return new BufferedReader(
                    new InputStreamReader(
                    c.getInputStream()));

        } catch (MalformedURLException ex) {
            log.error(ex);
        }
        return null;
    }

    private String lemmatizeWord(String wordNoHyphen) {
        log.info(String.format("Lemmatizing word %s", wordNoHyphen));

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(wordNoHyphen);

        // run all Annotators on this text
        _snlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String originalText = token.originalText();
                String word = token.word();
                String lemma = token.lemma();
                log.info(String.format("Lemma for word %s is %s. Word is %s.", wordNoHyphen, lemma, word));
                _currentNer = token.ner();
                log.info(String.format("NER for word %s is %s. Word is %s.", wordNoHyphen, _currentNer, word));
                return lemma;
            }
        }
        return wordNoHyphen;
    }

    private boolean isInDatabase(String word) {
        log.info("Checking for word lemma in db " + word);

        Session session = NerHibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<HyphenatedWords> words = session.createCriteria(HyphenatedWords.class)
                .add(Restrictions.eq("normalWord", word)).list();
        log.info("Words retrieved from db, count " + words.size());
        if (words.size() > 1) {
            log.warn("More than one word found " + words.size());
        }
        session.close();

        if (words.size() > 0) {
            log.info(String.format("Word lemma %s found in db", word));
            return true;
        }

        return false;
    }

    private void insertWordIntoDb(String word, String normalWord) {
        log.info(String.format("Inserting pair (hyphenated , normal word): %s , %s into db.", word, normalWord));

        Session session = NerHibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        HyphenatedWords row = new HyphenatedWords();
        row.setHyphenatedWord(word);
        row.setNormalWord(normalWord);
        session.save(row);
        session.getTransaction().commit();
        session.close();

        log.info("Finished inserting words into db.");
    }

    private boolean processDictionaryReferenceHtml(BufferedReader in, String lemmatizedWord) {
        try {
            HtmlCleaner cleaner = new HtmlCleaner();

            TagNode node = cleaner.clean(in);
            TagNode nodes[] = node.getElementsByAttValue("class", "me", true, false);
            if (nodes.length > 0) {
                if (nodes[0].getName().equals("h2")) {
                    String correctWord = nodes[0].getText().toString();
                    log.info("Word found " + correctWord);
                    _correctWords.add(lemmatizedWord.toLowerCase());
                    return true;
                }
            } else {
                nodes = node.getElementsByAttValue("class", "nr", true, false);
                if (nodes.length > 0) {
                    if (nodes[0].getName().equals("span")) {
                        String noWordFound = nodes[0].getText().toString();
                        log.info("Word not found " + noWordFound);
                        return false;
                    }
                }
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return false;
    }

    private boolean processFreeDictionaryHtml(BufferedReader in, String lemmatizedWord) {
        try {
            HtmlCleaner cleaner = new HtmlCleaner();

            TagNode node = cleaner.clean(in);
            TagNode nodes[] = node.getElementsByAttValue("class", "hw", true, false);
            if (nodes.length > 0) {
                if (nodes[0].getName().equals("span")) {
                    String correctWord = nodes[0].getText().toString();
                    log.info("Word found " + correctWord);
                    _correctWords.add(lemmatizedWord.toLowerCase());
                    return true;
                }
            }
        } catch (IOException ex) {
            log.error(ex);
        }
        return false;
    }

    private boolean processMeriamHtml(BufferedReader in, String lemmatizedWord) {
        lemmatizedWord = lemmatizedWord.toLowerCase();
        try {
            HtmlCleaner cleaner = new HtmlCleaner();

            TagNode node = cleaner.clean(in);
            TagNode nodes[] = node.getElementsByAttValue("class", "definition", true, true);
            if (nodes.length > 0) {
                if (nodes[0].getName().equals("div")) {
                    TagNode h1[] = nodes[0].getElementsByName("h1", false);
                    if (h1.length > 0) {
                        List children = h1[0].getAllChildren();
                        String correctWord = ((ContentNode) children.get(0)).getContent();
                        log.info("Corrected word " + correctWord);
                        if (lemmatizedWord.contains(correctWord)) {
                            _correctWords.add(lemmatizedWord.toLowerCase());
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
        HyphenationChecker checker = new HyphenationChecker();
        System.out.println(checker.chekcHyphenatedWordExists("John-son", "John-son"));
        System.out.println(checker.chekcHyphenatedWordExists("mis-sion", "mission"));
        System.out.println(checker.chekcHyphenatedWordExists("mis-sion", "mission"));
        System.out.println(checker.chekcHyphenatedWordExists("ap-peritive", "apperitive"));
    }
}
