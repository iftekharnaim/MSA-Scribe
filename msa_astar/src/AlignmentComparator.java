/**
 * 
 */

/**
 * @author inaim
 *
 */


import java.util.Comparator;

public class AlignmentComparator implements Comparator<Alignment>
{
	    @Override
	    public int compare(Alignment x, Alignment y)
	    {
	    	// Always give higher priority to the alignment with low cost
	        if (x.getAlignmentCost() > y.getAlignmentCost())
	        {
	            return 1;
	        }
	        
	        if (x.getAlignmentCost() < y.getAlignmentCost())
	        {
	            return -1;
	        }
	        return 0;
	    }
}
