import java.util.ArrayList;

/**
 * 
 * @author inaim
 *
 */

public class Sequence {
	public String sequenceString;
	public String timeString;
	public ArrayList<Integer> wokerIDList;
	
	public Sequence(String seqstring, String timestring)
	{
		this.sequenceString = seqstring;
		this.timeString = timestring;
		this.wokerIDList = null;
	}
	
	public Sequence(String seqstring, String timestring, ArrayList<Integer> workerlist)
	{
		this.sequenceString = seqstring;
		this.timeString = timestring;
		this.wokerIDList = workerlist;
	}
	

}
