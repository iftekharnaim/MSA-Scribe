import java.io.FileNotFoundException;
import java.util.*;

/*import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellCorrection;
import org.xeustechnologies.googleapi.spelling.SpellResponse;*/

//import dk.dren.hunspell.Hunspell;

/**
 * 
 */

/**
 * @author inaim
 *
 */
public class SpellCheckerForCaptions {
	 
	 /*
	 public static String getSpellCheckedString(String caption)
	 {		 
		 SpellChecker checker = new SpellChecker();
		 String[] parts = caption.split(" ");
		 String output ="";
		 
	    for(int i =0;i<parts.length;i++)
	    {
		    SpellResponse spellResponse = checker.check( parts[i] );
		    
		    if(spellResponse==null || spellResponse.getCorrections() == null)
		    {
		    	output += parts[i] + " ";		    	
		    }
		    else
		    {
		    	
		    	SpellCorrection[] sc = spellResponse.getCorrections();
		    	
		    	output += sc[0].getValue() + " ";
		    }
	
	    }
	    return output.trim();
	 }
	 */
	 
	 public static String getSpellCheckedStringHunspell(String caption)
	 {		 
		 String[] parts = caption.split(" ");
		 String output ="";
		 
		 /*
		 Hunspell.Dictionary d = null;

		 try
		 {
			 d = Hunspell.getInstance().getDictionary("/localdisk/en_US");
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }

	    for(int i = 0;i<parts.length;i++)
	    {
	    	String word = parts[i];
	    	
	    	if (d.misspelled(word)) 
	    	{
			
	    		java.util.List<String> suggestions = d.suggest(word);
	    		
				//System.out.println("Misspelt: "+word);
				if (!suggestions.isEmpty() && suggestions.get(0).split(" ").length == 1) 
				{
					
					output+= suggestions.get(0) + " ";
					//System.out.println("Correct: "+suggestions.get(0));
				} 
				else 
				{
					output += parts[i] + " ";
				}
	    	}
	    	else 
	    	{
	    		output += parts[i] + " ";
	    	}
	    }
	    
	    */
	    return output.trim();
	    
	 }
}
