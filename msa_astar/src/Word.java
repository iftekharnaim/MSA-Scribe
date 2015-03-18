/**
 * 
 */

/**
 * @author inaim
 *
 */
public class Word {
	
	String text;
	public int length;
	long timeStamp;	
	String auth;
	
	Word(String inText, long inTime, String workerID) 
	{
		//super();		
		this.text = inText;
		this.length = inText.length();
		timeStamp = inTime;		
		auth = workerID;
	}
		
	Word(Word oldWord) {
		//super();
		this.text = oldWord.text;
		this.length = this.text.length();
		this.timeStamp = oldWord.timeStamp;
		this.auth = oldWord.auth;
	}
	
	public String getText() 
	{
		return this.text;
	}
	
	boolean matches(Word otherWord) 
	{
		// Determine if a string is an exact match (exact up to multiple strings in text)
		return (text.toString().equals(otherWord.toString()) && timeStamp == otherWord.timeStamp);
	}
	
	boolean matchesEpsilon(Word otherWord, int epsilon) 
	{
		// Relaxed notion of a match
		return (this.equals(otherWord) && Math.abs(timeStamp - otherWord.timeStamp) <= epsilon);
	}

}
