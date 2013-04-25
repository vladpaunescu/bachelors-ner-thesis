
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import java.util.TreeSet;


/**
 *
 * @author bogdan
 */
public class Main {
	
	 /*
	  * Citim lista de nume autori dintr-un fisier predat ca argument. 
	  * pidFILE este o masura de impiedicare a aparitiei duplicatelor(retin fiecare pid unic)
	  * 
	  * */
    public static void main(String[] args) throws FileNotFoundException
    {
        File alpha = new File(args[0]);
    	File beta = new File("pidFILE.txt");
        
        Scanner sc = new Scanner(alpha);
        Scanner sc2 = new Scanner(beta);
        Collection<String> alha = new TreeSet<String>();
        
        while(sc2.hasNext())
        {
        	alha.add(sc2.nextLine());
        }
        sc2.close();

        
        String strLine;
        JsonAnalyzer tool = new JsonAnalyzer();
        
        
        //Citesc autor, extrag toate informatiile posibile despre el.
        while(sc.hasNext())
        {
            strLine = sc.nextLine();
            System.out.println(strLine);                        
            
            tool.Analyzer(strLine, alha);
        }
        sc.close();
		
        try {//actualizez pidFILE
	        BufferedWriter out = new BufferedWriter(new FileWriter("pidFILE.txt"));
			for(String elem: alha)
			{
				out.write(elem.toString());
				out.newLine();
			}

			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
		
		}


    }        
}
