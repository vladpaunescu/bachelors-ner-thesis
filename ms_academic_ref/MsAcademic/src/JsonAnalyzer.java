import java.util.Collection;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
 
/*
 * Pentru parsare JSON efectiva, consultati linia 116 -> sfarsit modul
 * 
 * Pentru testare, vezi modulul MainNew
 * 
 * */


public class JsonAnalyzer {
    JSONParser parser = new JSONParser();
    HttpGet jsonTEXT = new HttpGet();
    XMLProcesser alpha = new XMLProcesser();
     
    
   public Boolean isPerson()
   {
	   Scanner sc = new Scanner(System.in);
	   String str;
	   if(sc.hasNextLine())str = sc.next();
	   else str = "";
	   if(str.contains("y")){
		   //sc.close(); 
		   return true;
	   }
	   //sc.close();
	   return false;
   }
    
    
   public void Analyzer(String query, Collection<String> dataBase) {
	
	try {
            
				/* Partea 1. Caut autori, vad daca exista un match si intreb utilizatorul.
				 * Scopul: sa obtin un authorID.
				 * Se poate sari direct la partea 2.
				 * */
		
                long authorID = -1;
                query = query.replace(",", " ");
                query = query.toLowerCase();
                
                Object one = parser.parse(jsonTEXT.generateJSON(-1, query, "Author", "author", 0));
                JSONArray jObj = (JSONArray)((JSONObject)(((JSONObject)((JSONObject)one).get("d")).get("Author"))).get("Result");
                
                Boolean approval = false;
                long aID = -1;
                
                for(int i = 0; i < jObj.size(); i++)
                {
                    String firstName, lastName, middleName, affiliation = "";
                    
                    firstName = (String)((JSONObject)jObj.get(i)).get("FirstName");
                    
                    middleName = (String)((JSONObject)jObj.get(i)).get("MiddleName");
                    
                    lastName = (String)((JSONObject)jObj.get(i)).get("LastName");
                    
                    if(((JSONObject)jObj.get(i)).get("Affiliation") != null)
                    	affiliation = (String)((JSONObject)((JSONObject)jObj.get(i)).get("Affiliation")).get("Name");
                    
                    middleName = middleName.toLowerCase();
                    firstName = firstName.toLowerCase();
                    lastName = lastName.toLowerCase();
                    query = query.toLowerCase();
                    

                    if(middleName == null || middleName.equals(""))
                    	middleName = "@#$";

                   
                    if((query.contains(firstName) || query.contains(middleName)) && query.contains(lastName))
                    {

                        aID = (long)((JSONObject)jObj.get(i)).get("ID");
                        if((affiliation.contains("Polytechnic") || affiliation.contains("Politehnica") || 
                        		affiliation.contains("Politechnica")) && (affiliation.contains("Bucuresti") || 
                        				affiliation.contains("Bucharest")))
                        	System.out.println("Din Politehnica!!!");

                        
                        
                        	System.out.println("http://academic.research.microsoft.com/Author/" + String.valueOf(aID));
                        	System.out.println("Is this the droid you are looking for?(y/n)");

                        	approval = isPerson();
                        	System.out.println();
                        
                    	
                        if(approval)
                        {
                        	authorID = aID;
                        	break;
                        }
                    }
                }
                if(authorID == -1)return;
                
                
                
        /*
         * Partea 2. Caut dupa authorID toate publicatiile.
         * 
         * */
        int startIndex = 0;
        int okLength = 1;
        while(okLength == 1)
        {
        	Object obj = parser.parse(jsonTEXT.generateJSON(authorID, "", "AllInfo", "publication", startIndex));
        	
        	/* Parsare JSON*/
        	JSONObject jsonObject = (JSONObject) obj;
        	jsonObject = (JSONObject) jsonObject.get("d");
        	jsonObject = (JSONObject) jsonObject.get("Publication");

        	JSONArray jsonArray = (JSONArray) jsonObject.get("Result");
        	int k;
        	for(k = 0; k < jsonArray.size() ; k++)
        	{	
        		JSONObject jsonAux = (JSONObject) jsonArray.get(k);

        		String title = (String)jsonAux.get("Title");
        		String abstractData = (String)jsonAux.get("Abstract");
        		long year = (long)jsonAux.get("Year");
        		
        		JSONArray authorArray = (JSONArray)jsonAux.get("Author");
        		String[] authors = new String[authorArray.size() + 1];
        		int ok = 0;
        		
        		for(int j = 0; j < authorArray.size(); j++)
        		{
        			JSONObject author = (JSONObject)authorArray.get(j);
        			String authorName = (String)author.get("LastName") + " " + (String)author.get("MiddleName") + 
        								" " + (String)author.get("FirstName");
        			String authorAddress = (String)author.get("HomepageURL");
        			long idOfAuthor = (long)author.get("ID");
                    if(idOfAuthor == authorID)
                    	ok = 1;
                    authors[j] = authorName;
        		}
                    if(ok == 0)
                    	continue;
			
                JSONObject conference = (JSONObject)jsonAux.get("Conference");
                String conferenceName = "";
                if(conference != null)
                	conferenceName = (String)conference.get("FullName") + " (" + (String)conference.get("ShortName") + ")";
		
                JSONArray keyword = (JSONArray)jsonAux.get("Keyword");
                String keywords = "";
                for(int i = 0; i < keyword.size(); i++)
                	keywords +=(String)((JSONObject)keyword.get(i)).get("Name") + ",";
			
                JSONObject journalClass = (JSONObject)jsonAux.get("Journal");
                String journal;
                if(journalClass == null)
                	journal = "";
                else 
                	journal = (String)journalClass.get("FullName") + " (" + (String)journalClass.get("ShortName") + ")";
                String DOI = (String)jsonAux.get("DOI");
                if(DOI == null)DOI = "";
			
                long publicationID = (long)jsonAux.get("ID"); 
                if(dataBase.contains(String.valueOf(publicationID)))
                	continue;
                else 
                	dataBase.add(String.valueOf(publicationID));
            
                /* Afisare random pentru verificare. */
                
                System.out.println(authors + " " + title + " " + year);
                
                //alpha.writeFile(authors, keywords, title, conferenceName, abstractData, publicationID, year, journal, DOI);
                }
		startIndex += 100;
		if(k < 99) 
			okLength = 0;
        }
	} catch (ParseException e) {
		e.printStackTrace();
	}
 
     }
 
}