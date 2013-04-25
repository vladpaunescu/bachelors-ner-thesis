import java.util.TreeSet;


public class MainNew {

	/*
	 * Exemplu cautare.
	 * 
	 * Intoarce JSON-ul aferent
	 * */
	
	
	public static void main(String[] args)
	{
		HttpGet x = new HttpGet();
		JsonAnalyzer tool = new JsonAnalyzer();
		
		tool.Analyzer("traian rebedea", new TreeSet<String>());
		
	}
}
