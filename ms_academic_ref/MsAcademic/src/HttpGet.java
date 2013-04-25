import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HttpGet {

	public static final String address = "http://academic.research.microsoft.com/json.svc/search?AppId=b66b7cf2-208a-46d8-ad15-69c41cf8b989&";
	static String FullTextQuery, AuthorQuery;
	static String ResultObjects;
	static String PublicationContent, OrderBy;
	static long AuthorID, ConferenceID, StartIdx, EndIdx;

	
    public static void setQuery(String query)
	{
            
            AuthorQuery = query.replace(" ", "+");	
	}
	
	public static void setAuthorID(long authorID)
	{
		AuthorID = authorID;
	}

    /*
     * Campuri:
     * 
     * authorID: ID autor, daca dorim sa nu setam initializam cu -1.
     * query: query-ul dupa care facem cautarea ex: "data mining"
     * content: cel mai sigur setam pe "AllInfo", mai multe optiuni gasim in documentatia API-ului
     * info: tip informatie, de obicei "publication", mai multe detalii in documentatia API-ului
     * startIndex: index-ul de la care se afiseaza(protocolul returneaza doar primele 100 de intrari)
     * 
     * exemplu:
     * 
     * 	generateJSON(-1, "computer", "AllInfo", "publication", 0);
     * 
     * String-ul rezultat este chiar text-ul JSON rezultat.
     * Se poate analiza pe:
     * 
     * 			http://jsonviewer.stack.hu/
     * 
     * */ 
    public String generateJSON(long authorID, String query, String content, String info, int startIndex) 
    {

    String result = "" + address;
	setQuery(query); 
	
	result += "AuthorQuery=" + AuthorQuery + "&";
	
	if(authorID != -1)
	{
		setAuthorID(authorID); 
		result += "AuthorID=" + AuthorID + "&";
		}
	OrderBy = "Rank"; 
	result += "OrderBy=" + OrderBy + "&";
	
	PublicationContent = content; 
	result += "PublicationContent=" + PublicationContent + "&";
	
	ResultObjects = info;	
	result += "ResultObjects=" + ResultObjects + "&";
	
	int endIndex = startIndex + 99;
	result += "StartIdx=" + startIndex + "&EndIdx=" + endIndex;
	
	String str = "";
	try {
        URL url = new URL(result);
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(url.openStream()));
        str = in.readLine();
        in.close();
    } 
    catch (MalformedURLException e) {} 
    catch (IOException e) {}

	//result - adresa accesata pentru a primi JSON-ul
	//str - JSON-ul efectiv
	return str;// putem returna si result daca vrem sa facem noi cererea
}
}