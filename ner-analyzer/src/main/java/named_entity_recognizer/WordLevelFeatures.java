/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package named_entity_recognizer;

/**
 *
 * @author vlad.paunescu
 */
public class WordLevelFeatures {
    
    private static final WordLevelFeatures INSTANCE = new WordLevelFeatures();
    
    private final static String FIRST_CAPITAL_LETTER_REGEXP = "\\s*[^A-Za-z0-9\\.]*[A-Z][A-Za-z]*[\\.,]*\\s*";
    private final static String ALL_UPERCASE_REGEXP = "\\s*[A-Z\\.]*\\s*";
    private final static String MIXED_CASE_REGEXP = "\\s*[a-z\\.]*[A-Z]+[A-Za-z\\.]*\\s*";
    protected WordLevelFeatures(){
        
    }
    
    public static WordLevelFeatures getInstance(){
        return INSTANCE;
    }
    
    public boolean isCandidateEntity(String word){
        return startsWithCapitalLetter(word) || allUpercase(word) || mixedCase(word);
    }
    
    public boolean startsWithCapitalLetter(String word){
        return word.matches(FIRST_CAPITAL_LETTER_REGEXP);
    }
    
    public boolean allUpercase(String word){
        return word.matches(ALL_UPERCASE_REGEXP);
    }
    
    public boolean mixedCase(String word){
        return word.matches(MIXED_CASE_REGEXP);
    }
    
    
    public static void main(String[] args){
        WordLevelFeatures test = WordLevelFeatures.getInstance();
        
        
        String text = "Capital,";
        
        System.out.println("Starts with capital letter test "+ text);
        System.out.println(test.startsWithCapitalLetter(text));
        text = "AADS";
        System.out.println("All uppercase test "+ text);
        System.out.println(test.allUpercase(text));
        text = "MixEdCase";
        System.out.println("Mixed case test "+ text);
        System.out.println(test.mixedCase(text));
        
    }
}
