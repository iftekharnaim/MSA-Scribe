/**
 * 
 */

/**
 * @author inaim
 *
 */
public class StringProcessor {
	
	public static int LEFT = 1;
	public static int UP = 2;
	public static int DIAG = 3;
	
	public static double estimateWER(String str1, String str2)
	{
		int i, j, n, m;			
		double[][] E;
		int[][]B;
		//		
		double substitution_cost = 0;
		double temp_sub, temp_ins, temp_del;

		// first split the sentence into words
		String[] wordlist1 = str1.split(" ");
		String[] wordlist2 = str2.split(" ");

		// get the length of sentence parts
		n = wordlist1.length;
		m = wordlist2.length;	

		// initialize the edit distance matrix
		E = new double [n+1][m+1];
		B = new int[n+1][m+1];
		E[0][0] = 0;
	
	
		for(i=1;i<=n;i++)
		{
			E[i][0] = i;	
			B[i][0] = StringProcessor.UP;
		}
		for(j=1;j<=m;j++)
		{
			E[0][j] = j;			
			B[0][j] = StringProcessor.LEFT;
		}
	
	
		// perform edit distance at sentence level, where each word is an atom
		for(i=1;i<=n;i++)
		{
			for(j=1;j<=m;j++)
			{
				// look for all the three different options
				
				//1. cost of substitution
				DamerauLevenshtein dl = new DamerauLevenshtein(wordlist1[i-1],wordlist2[j-1]);
				int mm = wordlist1[i-1].length();
				int nn = wordlist2[j-1].length();
				
				double percentage = (double)(dl.getSimilarity())/(Math.min(mm,nn));
	
				if(percentage<=0.3)
				{
					substitution_cost = 0;
				}
				else
				{
					substitution_cost = 1;
				}
				/*
				if(wordlist1[i-1].equals(wordlist2[j-1]))
				{
					substitution_cost = 0;
				}
				else
				{
					substitution_cost = 1;
				}
				*/
				
				//1. get the substitution cost
				temp_sub = E[i-1][j-1] + substitution_cost;
				//2. cost of insertion
				temp_ins = E[i-1][j] + 1;				
				//3. cost of deletion
				temp_del = E[i][j-1] + 1;
				
				if(temp_sub <= temp_ins && temp_sub <= temp_del)
				{
					E[i][j] = temp_sub;
					B[i][j] = StringProcessor.DIAG;

				}
				else if(temp_ins <= temp_sub && temp_ins <= temp_del)
				{
					E[i][j] = temp_ins;
					B[i][j] = StringProcessor.UP;
				}
				else if(temp_del <= temp_sub && temp_del <= temp_ins)
				{
					E[i][j] = temp_del;
					B[i][j] = StringProcessor.LEFT;
				}
				
			}
		}
	
		printPairwiseAlignedString(B, wordlist1, wordlist2);
		System.out.println(E[n][m]);
		return E[n][m]; 
	}
	
	/*
	 * Print the pairwise aligned string, using the alignment backtracking matrix B
	 */
	public static void printPairwiseAlignedString(int[][] B, String[] wordlist1, String[] wordlist2)
	{
		int n, m, i, j;
		// get the length of sentence parts
		n = wordlist1.length;
		m = wordlist2.length;
		
		i = n;
		j = m;
		
		String outputA = "";
		String outputB = "";
		
		/*
		 * Loop over the B matrix to print the aligned output string
		 */
		while(true)
		{
			if(i==0 && j==0)
				break;
			
			// substitution
			if(B[i][j] == StringProcessor.DIAG)
			{
				i--;
				j--;
				
				//System.out.print( "(" + wordlist1[i] + "," + wordlist2[j] + ") ");
				outputA = wordlist1[i] + " " + outputA;
				outputB = wordlist2[j] + " " + outputB;
			}
			// move left
			else if(B[i][j] == StringProcessor.LEFT)
			{				
				j--;
				//System.out.print( " " + wordlist2[j]);
				outputA = "_____" + " " + outputA;
				outputB = wordlist2[j] + " " + outputB;

			}
			// move right
			else
			{
				i--;
				//System.out.print( " " + wordlist1[i]);
				outputA = wordlist1[i] + " " + outputA;
				outputB = "_____" + " " + outputB;

			}
			
		}
		System.out.println(outputA);
		System.out.println(outputB);
	}
}
