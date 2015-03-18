/**
 * 
 */

/**
 * @author inaim
 *
 */
public class EditDistanceCalculator {

	
	/*
	 * A method to perform pairwise string alignment
	 */
	
	public static EditDistanceMatrix getPairwiseSenteceAlignmentCostReverseOrder(String[] wordlist11, String[] wordlist22, double gapOpenPenalty, double gapExtensionPenalty)
	{
		int i, j, n, m;
		double[][] E;
		double [][]G;
		//
		double substitution_cost;
		double temp_sub, temp_gap;

		// get the length of sentence parts
		n = wordlist11.length;
		m = wordlist22.length;	

		String[] wordlist1 = new String[n];
		String[] wordlist2 = new String[m];
		
		// reverse the two strings
		for(i=0;i<n;i++)
		{
			wordlist1[i] = wordlist11[n-i-1];
		}
		for(j=0;j<m;j++)
		{
			wordlist2[j] = wordlist22[m-1-j];
		}
		
		// initialize the edit distance matrix
		E = new double [n+1][m+1];
		G = new double [n+1][m+1];
		
		E[0][0] = 0;
		G[0][0] = 0;
		
		for(i=1;i<=n;i++)
		{
			//E[i][0] = gapOpenPenalty + (i-1) * gapExtensionPenalty;
			E[i][0] = Double.MAX_VALUE;
			
			G[i][0] = gapOpenPenalty + (i-1) * gapExtensionPenalty;
			//G[i][0] = Double.MAX_VALUE;
		}
		for(j=1;j<=m;j++)
		{
			//E[0][j] = gapOpenPenalty + (j-1) * gapExtensionPenalty;
			E[0][j] = Double.MAX_VALUE;


			G[0][j] = gapOpenPenalty + (j-1) * gapExtensionPenalty;
			//G[0][j] = Double.MAX_VALUE;

		}
		
		// perform edit distance at sentence level, where each word is an atom
		for(i=1;i<=n;i++)
		{
			for(j=1;j<=m;j++)
			{
				
				/// Update the G values first
				double temp1 = E[i-1][j] + gapOpenPenalty;
				double temp2 = E[i][j-1] + gapOpenPenalty;
				double temp3 = G[i-1][j] + gapExtensionPenalty;
				double temp4 = G[i][j-1] + gapExtensionPenalty;
				
				G[i][j] = getMinValue(temp1,temp2, temp3, temp4);				
				//////////// Update the E values
				//1. cost of substitution
				substitution_cost = getEditDistance(wordlist1[i-1], wordlist2[j-1]);
				//substitution_cost = this.getDLDistance(wordlist1[i-1], wordlist2[j-1]);
				temp_sub = E[i-1][j-1] + substitution_cost;
				temp_gap = G[i-1][j-1] + substitution_cost;
				
				E[i][j] = Math.min(temp_sub, temp_gap);
				
				
			}
		}
		
		for(i=0;i<=n;i++)
		{
			for(j=0;j<=m;j++)
			{
				E[i][j] = Math.min(E[i][j], G[i][j]);
			}
			
		}
		
		return new EditDistanceMatrix(E);
	}
	
	
	
	/*
	public static EditDistanceMatrix getPairwiseSenteceAlignmentCostReverseOrder(String[] wordlist11, String[] wordlist22, double gapOpenPenalty, double gapExtensionPenalty)
	{
		int i, j, n, m;
		double [][] E;
		double [][] F;
		double [][] V;
		double [][] G;
		//
		double substitution_cost;
		double temp_sub, temp_gap;

		// get the length of sentence parts
		n = wordlist11.length;
		m = wordlist22.length;	

		String[] wordlist1 = new String[n];
		String[] wordlist2 = new String[m];
		
		// reverse the two strings
		for(i=0;i<n;i++)
		{
			wordlist1[i] = wordlist11[n-i-1];
		}
		for(j=0;j<m;j++)
		{
			wordlist2[j] = wordlist22[m-1-j];
		}
		
		// initialize the edit distance matrix
		V = new double [n+1][m+1];
		E = new double [n+1][m+1];
		F = new double [n+1][m+1];
		G = new double [n+1][m+1];
		
		V[0][0] = 0;
		E[0][0] = 0;
		F[0][0] = 0;
		
		for(i=1;i<=n;i++)
		{
			E[i][0] = Double.MAX_VALUE;
			V[i][0] = gapOpenPenalty + (i-1) * gapExtensionPenalty;
		}
		for(j=1;j<=m;j++)
		{
			F[0][j] = Double.MAX_VALUE;			
			V[0][j] = gapOpenPenalty + (j-1) * gapExtensionPenalty;
		}
		
		// perform edit distance at sentence level, where each word is an atom
		for(i=1;i<=n;i++)
		{
			for(j=1;j<=m;j++)
			{
				
				double temp1 = V[i-1][j] + gapOpenPenalty;				
				double temp2 = F[i-1][j] + gapExtensionPenalty;
				F[i][j] = Math.min(temp1, temp2);
				
				double temp3 = E[i][j-1] + gapExtensionPenalty;
				double temp4 = V[i][j-1] + gapOpenPenalty;
				E[i][j] = Math.min(temp3, temp4);
				
				substitution_cost = getEditDistance(wordlist1[i-1], wordlist2[j-1]);
				G[i][j] = V[i-1][j-1] + substitution_cost;
				
				V[i][j] = getMinValue(G[i][j], E[i][j], F[i][j], Double.MAX_VALUE);
			}
		}
		
		return new EditDistanceMatrix(V);
	}
	*/
	
	/*
	 * The function estimates the pairwise edit distance between two strings.
	 * This is necessary to estimate the cost function for substitution of two words.
	 * 
	 * If two words are exactly the same, then the distance will be zero. 
	 */
	public static double getEditDistance(String str1, String str2)
	{
		int n, m, i, j;
		double[][] E;
		// get the lengths of both the strings
		n = str1.length();
		m = str2.length();
		// initialize the array
		E = new double [n+1][m+1];

		// Perform initialization
		for(i=0;i<=n;i++)
			E[i][0] = i;
		for(j=0;j<=m;j++)
			E[0][j] = j; 
		
		// perform iteration using DP recursion
		for(i=1;i<=n;i++)
		{
			for(j=1;j<=m;j++)
			{
				if(str1.charAt(i-1) == str2.charAt(j-1))
				{
					E[i][j] = E[i-1][j-1];
				}
				else
				{
					E[i][j] = 1 + Math.min(E[i-1][j], E[i][j-1]);
				}
			}
		}
		
		double percentage = (E[n][m])/(Math.min(m,n));
		double retval = 0;
		
		if(percentage > 0.30)
		{
			retval = 0.5;
		}
		else
		{
			retval = 0;
		}
		return retval;
	}
	
	public static double getMinValue(double a, double b, double c, double d)
	{
		double temp1 = Math.min(a, b);
		double temp2 = Math.min(c, d);
		
		
		return Math.min(temp1, temp2);
	}
	
	
	
//	/*
//	 * A method to perform pairwise string alignment
//	 */
//	public static EditDistanceMatrix getPairwiseSenteceAlignmentCost(String[] wordlist1, String[] wordlist2, double gapPenalty)
//	{
//		int i, j, n, m;		
//		double[][] E;
//		//
//		double substitution_cost;
//		double temp_sub, temp_ins, temp_del;
//
//		// get the length of sentence parts
//		n = wordlist1.length;
//		m = wordlist2.length;	
//
//		// initialize the edit distance matrix
//		E = new double [n+1][m+1];
//		E[0][0] = 0;
//		
//		for(i=1;i<=n;i++)
//		{
//			E[i][0] = gapPenalty*i;			
//		}
//		for(j=1;j<=m;j++)
//		{
//			E[0][j] = gapPenalty*j;			
//		}
//
//		// perform edit distance at sentence level, where each word is an atom
//		for(i=1;i<=n;i++)
//		{
//			for(j=1;j<=m;j++)
//			{
//				//1. cost of substitution
//				substitution_cost = getEditDistance(wordlist1[i-1], wordlist2[j-1]);
//				//substitution_cost = this.getDLDistance(wordlist1[i-1], wordlist2[j-1]);
//				temp_sub = E[i-1][j-1] + substitution_cost;
//				//2. cost of insertion
//				temp_ins = E[i-1][j] + gapPenalty;				
//				//3. cost of deletion
//				temp_del = E[i][j-1] + gapPenalty;
//				
//				if(temp_sub <= temp_ins && temp_sub <= temp_del)
//				{
//					E[i][j] = temp_sub;
//				}
//				else if(temp_ins <= temp_sub && temp_ins <= temp_del)
//				{
//					E[i][j] = temp_ins;
//				}
//				else if(temp_del <= temp_sub && temp_del <= temp_ins)
//				{
//					E[i][j] = temp_del;
//				}
//				
//			}
//		}
//		
//		return new EditDistanceMatrix(E);
//	}
	
	
}
