
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.Calendar;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bogdan
 */

public class XMLProcesser {
 
    static String header = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n<dublin_core schema=\"dc\">";
    static String[] auxString = {  "    <dcvalue element=\"", "\" qualifier=\"", "\">", "</dcvalue>"};
    static int auxIndex = 35;
    
    public static String printArray(String[] array)
    {
    	String result = "";
    	String[] aux;
    	int i; 
    	for(i = 0; i+1 < array.length; i++)
    	{
    		aux = array[i].split(" ");
    		if(i == 0){result += aux[aux.length - 1].charAt(0) +  "." + " " + aux[0];continue;}


    		result +=  ", " + aux[aux.length - 1].charAt(0) + "." + " " + aux[0] ;
    	}

    	result += ";";
    	return result;
    }

    
    
    static String setAuthor(String author)
    {
        return (auxString[0] + "contributor" + auxString[1] + "author" + auxString[2] + author + auxString[3]);
    }

    static String setDate(String type)
    {
    	String data= "2012-11-11";
        if(!type.equals("issued"))data += "T00:00:00Z";

        return (auxString[0] + "date" + auxString[1] + type + auxString[2] + data + auxString[3]);
    }
    
    static String setIdentifier(String type, String ID)
    {
        String text = "";
        if(ID == null)
        {
        	text += "http://hdl.handle.net/123456789/" + auxIndex;auxIndex++;
        	}
        else text = "ID: " + ID;
        return (auxString[0] + "identifier" + auxString[1] + type + auxString[2] + text +  auxString[3]);
    }
    
    static String setLink(long publicationID)
    {
        String text = "http://academic.research.microsoft.com/Publication/" + publicationID;
        auxIndex++;
        return auxString[0] + "identifier" + auxString[1] + "uri" + auxString[2] + text +  auxString[3];
    }
    
    static String setDescription(String qualifier, String text , String Message, String byWhom)
    {
        
        String data = "on " + getDate();
        String common = auxString[0] + "description" + auxString[1] + qualifier + "\" language=\"en" + auxString[2];
        
        if(qualifier.equals("provenance"))
            return (common + forProvenance(Message, byWhom, data) + auxString[3]);
        else return(common + text + auxString[3]);
    }
    
    private static String getDate()
    {
        String data= "2012-11-11";
        data += "T00:00:00Z";
        return data;
    }
    
    
    //No need for this
    private static String forProvenance(String message, String byWhom, String date)
    {
        String result = "";
        result += message + byWhom + date;        
        
        result += " No. of bitstreams: 1\nnewdoc: 23 bytes, checksum: d046bbec0c188bf2bdf30de240ea7b3e (MD5)";
        return result;
    }
    
    
    static String setLanguage(String language)
    {
        return (auxString[0] + "language" + auxString[1] + "iso" + "\" language=\"" + language + "\""  + auxString[2] + "fr" +  auxString[3]);
    }
    
    static String setRelation(String series)
    {
        return (auxString[0] + "relation" + auxString[1] + "ispartofseries" + auxString[2] + series +  auxString[3]);
    }
    
    static String setSubject(String series)
    {
        return (auxString[0] + "subject" + auxString[1] + "none" + auxString[2] + series +  auxString[3]);
    }
    
    static String setTitle(String series)
    {
        return (auxString[0] + "title" + auxString[1] + "none" + auxString[2] + series +  auxString[3]);
    }
    
    static String setOther(String text, String message)
    {
        return (auxString[0] + "description" +  auxString[1] + text + "\" language=\"en_US"  + auxString[2] + message +  auxString[3]);
    }
    
    static String finalLine()
    {
        return ("</dublin_core>");
    }
    
    public void writeFile(String[] author, String domain, String title, String conference, String abstractData, long publicationID, long year, String journal, String DOI)
    {
        String filename = "/home/bogdan/searchResult";//dublin_core.xml";
        filename += auxIndex + "dublin_core.xml";
        try{
            FileWriter outFile = new FileWriter(filename);
            BufferedWriter out = new BufferedWriter(outFile);
            
            out.write(header);
            out.newLine();
            

            for(int i = 0; i < author.length; i++)
            {
                if(author[i] == null)break;
                out.write(setAuthor(author[i]));
                out.newLine();
            }//getAuthor
            
            out.write(setDate("accessioned"));
            out.newLine();
            out.write(setDate("available"));
            out.newLine();
            out.write(auxString[0] + "date" + auxString[1] + "issued" + auxString[2] + year + auxString[3]);
            out.newLine();
            
            out.write(setLink(publicationID));
            out.newLine();
            
            abstractData = abstractData.replace("&", "&amp;");
            out.write(setDescription("abstract", abstractData, null, null));//affiliation
            out.newLine();
            conference = conference.replace("&", "&amp;");
            
            String description="";
            journal = journal.replace("&", "&amp;");
            
            if(!journal.equals(""))description = printArray(author) + " (" + String.valueOf(year) + ")" + journal + "; ";// + "http://dx.doi.org/" + DOI ;  
            else description = printArray(author) + " (" + String.valueOf(year) + ")" + conference + "; ";// + "\nhttp://dx.doi.org/" + DOI ;
            
            out.write(setDescription("none", description, null, null));
            if(DOI != "")out.write(setDescription("none", "\nhttp://dx.doi.org/" + DOI, null, null));

            out.newLine();
            out.write(setOther("sponsorship", "none"));
            
            out.newLine();
            if(!journal.equals(""))out.write(setRelation(journal));
            else out.write(setRelation(conference));
            out.newLine();
            
            String[] auxDomain = domain.split(",");
            for(int i = 0; i < auxDomain.length; i++)
            {
            out.write(setSubject(auxDomain[i]));//domain
            out.newLine();
            }

            
            title = title.replace("&", "&amp;");
            out.write(setTitle(title));//title
            out.newLine();
            out.write(finalLine());
            
            out.close();
            
            
        } catch(IOException e) {
    }
    }
    

	
}