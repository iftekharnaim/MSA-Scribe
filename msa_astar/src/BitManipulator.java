/**
 * @author inaim
 */


public class BitManipulator {
	
	public BitManipulator()
	{
		
	}
	
	public int[] getBitsFromInteger(int x, int numBits)
	{
		// initialize the mask
		int mask = 1;
		// get the bitmap for the given number of bits
		int []bitmap = new int[numBits];
		
		/*
		 * Run a loop by masking the bits
		 */
		for(int i = 0; i < numBits; ++i) {
			
		    bitmap[i] = Math.abs(mask & x);
		    
		    if(bitmap[i]>0)
		    	bitmap[i] = 1;
		    
		    mask = mask << 1;
		}		
		return bitmap;
	}

}
