/**
 * 
 * @author inaim
 *
 * The class defines an alignment object, which stores the alignments over all the sequences.
 * An alignment object stores a sequence of integers, that indicate the substitution/gap in that alignment.
 */

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Alignment class
 */
public class Alignment {
	
	/*
	 * Define the attributes
	 */
	
	
	/*********************** Static Variables, shared by all **************/
	/*
	 * A list of the strings/sequences over which we are estimating alignment
	 */
	public static ArrayList<String[]> wordsList = null;
	
	/*
	 * A list of the strings containing time
	 */
	public static ArrayList<long[]> timeList = null;
	
	/*
	 * Stores the number of words per sequence
	 */
	public static int[] numWordsList = null;
	
	/*********************** Non-Static Variables **************/
	/*
	 * Total number of sequences, that will be aligned
	 */
	public int nrows; 

	/*
	 * indicator whether to include time or not
	 */
	public boolean isIncludeTime = false;
	
	/*
	 * The number of columns in the alignment matrix.
	 * At each  expansion step, we'll increase the ncols by one. 
	 * No matter whether the states have zeros (gaps) or ones (substitutions), 
	 * we will still increase the output column numbers.
	 */
	public int ncols;
	
	/*
	 *  An array to keep track of the current position in each of the K sequences.
	 *  Initially, we'll all start with the position zero (first position).
	 *  The position will be advanced only if the alignment has one at a position.
	 *  That means we've covered that position and will move forward to the next postion. 
	 */	
	public int[] currentPos;
	
	public String currentPosString;
	
	/*
	 * The cost of the current alignment 
	 */
	public double cost;
	
	public double gcost;
	
	/*
	 * The gap penalty for pairwise alignment (needed for heuristic estimation)
	 */
	//public double gapPenalty;
	
	//public double gapExtesionPenalty = 0.05;
	
	/*
	 * An object to manipulate the bits of the state variable
	 */
	private BitManipulator bm;
	
	
	/*
	 * A list of alignments over all the positions. Each position will have any 
	 * values between 1 and 2^nrows
	 */
	public ArrayList<Integer> alignList;
	
	
	/*
	 * stores the upper bound for A* search
	 */
	public double upperBound;
	
	/*
	 * the weight of heuristic cost (1 for admissible cost, >1 for non-admissible heuristic)
	 */
	public double heuristicWeight;
	
	
	public static EditDistanceMatrix[][] listOfeditDistanceMatrices = null;

	
	/*
	 * The constructor function for the Alignment class
	 */
	public Alignment(ArrayList<String[]> myWordlist, ArrayList<long[]> myTimelist, int nseq, int state, double heuristic_weight, double upper_bound, 	EditDistanceMatrix[][] listEdt)
	{	
		
		if(state > Math.pow(2, nseq))
		{
			System.out.println("Error: the state number must be smaller than 2^K");
			return;
		}
		
		if(Alignment.wordsList == null)
		{
			Alignment.wordsList = myWordlist;
		}

		if(Alignment.timeList == null)
		{
			Alignment.timeList = myTimelist;
		}

		
		
		this.nrows = nseq;
		this.ncols = 1;
		// set the heuristic weight
		this.heuristicWeight = heuristic_weight;
		// set the upper bound for A* search
		this.upperBound = upper_bound;
		
		// insert the state in the alignment array
		alignList = new ArrayList<Integer>();
		alignList.add(new Integer(state));

		// initialize the current position array.
		this.currentPos = new int[this.nrows];
		this.bm = new BitManipulator();
		
		// get the set bits for the state
		int[] bits = bm.getBitsFromInteger(state, this.nrows);
		this.currentPosString = "";
		for(int i=0;i<this.nrows; i++)
		{
			this.currentPos[i] = bits[i];
			this.currentPosString += bits[i] + ",";
		}
		
		// generate the number of words per string		
		if(Alignment.numWordsList == null)
		{
			Alignment.numWordsList = new int[this.nrows];
			
			for(int i=0;i<this.nrows;i++)
			{
				Alignment.numWordsList[i]= Alignment.wordsList.get(i).length;				
			}
		}
		
		if(Alignment.listOfeditDistanceMatrices == null)
		{
			Alignment.listOfeditDistanceMatrices = listEdt;
		}

		// update the currentPos values. If the state bit is 1, then advance. Else stay at the same position.		
		boolean isValidAlignment = true;		
		// update the current position
		for(int i=0;i<this.nrows; i++)
		{			
			// This part is failing, because the actual string length is much larger than the number of words
			if(this.currentPos[i] > Alignment.numWordsList[i])
			{
				isValidAlignment = false;
				break;
			}
		}
		
		if(isValidAlignment == false)
		{
			this.cost = -1;
		}
		else
		{
			this.cost = this.estimateCostFunction(0.0);			
		}


		//long startTimeforCost = System.currentTimeMillis();
		// get the cost of the current alignment

		
		//System.out.println(". ="+(System.currentTimeMillis()-startTimeforCost));
	}
	
	
	/*
	 * A default constructor. All values are empty.
	 */
	public Alignment()
	{	
		this.nrows = 0;
		this.ncols = 0;
		this.upperBound = Double.MAX_VALUE;
		this.heuristicWeight = 1.0;

		// initialize empty lists
		this.alignList = null;
		this.currentPos = null;
		this.bm = null;		
		// get the cost of the current alignment
		this.cost = -1;
		
		// Do not touch the static variables
	}

	/*
	 * clone an instance of the class, to avoid hazards with object references.
	 */
	public Alignment getClone()
	{	
		// get a new alignment object. initially empty.
		Alignment cloned = new Alignment();
		
		cloned.nrows = this.nrows;
		cloned.ncols = this.ncols;
		cloned.heuristicWeight = this.heuristicWeight;
		cloned.upperBound = this.upperBound;
				
		cloned.alignList = (ArrayList<Integer>)(this.alignList.clone());
		cloned.currentPos = this.currentPos.clone();
		cloned.currentPosString = this.currentPosString;
		cloned.bm = new BitManipulator();
		cloned.cost = this.cost;
		
		return cloned;
	}
	
	/*
	 * Get the successor alignment for a given state. The state will be sent as input parameter.
	 * We need to generate a proper alignment object for the given state bitmap.
	 */
	public Alignment getSuccessor(int successorState)
	{
		// create a clone of the current state
		Alignment newAlign = this.getClone();
		
		// the successor has one additional column
		newAlign.ncols = this.ncols + 1;
		
		// add the new state in the alignList
		newAlign.alignList.add(new Integer(successorState));
		
		// get the set bits for the state
		int[] bits = newAlign.bm.getBitsFromInteger(successorState, this.nrows);		
		// update the currentPos values. If the state bit is 1, then advance. Else stay at the same position.		
		boolean isValidAlignment = true;		

		// update the current position
		this.currentPosString = "";
		for(int i=0;i<newAlign.nrows; i++)
		{			
			newAlign.currentPos[i] = newAlign.currentPos[i] + bits[i];
			newAlign.currentPosString += newAlign.currentPos[i] + ","; 
		
			// This part is failing, because the actual string length is much larger than the number of words
			if(newAlign.currentPos[i] > Alignment.numWordsList[i])
			{
				isValidAlignment = false;
				break;
			}
		}
		
		/*
		 * Check if we got a valid alignment object. Sometimes when we insert gaps at the end of a string, 
		 * we must ensure that the current position is not overflowing the string length. 
		 */
		if(!isValidAlignment)
		{			
			//System.out.println("HUGE TROUBLE. FOUND AN INVALID STATE!!!!!!!!!!!");
			newAlign.alignList.clear();
			newAlign.bm = null;
			newAlign.currentPos = null;			
			newAlign.currentPosString = null;
			newAlign = null;
			return null;
		}
		
		// get the cost of the current alignment
		newAlign.cost = newAlign.estimateCostFunction(this.gcost);
		
		// return the successor alignment
		return newAlign;		
	}
	
	/*
	 * 
	 */
	public boolean isValidAlignmentInTermsOfTime(double timeThreshold)
	{	
		// initialize if the current state is valid. 
		boolean isValidTime = true;
		// initialize the time array
		long[] timeArray = new long[this.nrows];
		int i,j;
		
		// get the time values of each of the words in current state position
		for(i=0;i<this.nrows; i++)
		{			
			// check the current position of the state and check if all the words are within the given time threshold 
			if(Alignment.timeList.get(i) == null || Alignment.timeList.get(i).length==0)
			{
				timeArray[i] = -1;
				continue;
			}			

			if(this.currentPos[i] < Alignment.numWordsList[i])
			{
				timeArray[i] = Alignment.timeList.get(i)[currentPos[i]];
			}
			else
			{
				timeArray[i] = -1;
			}
		}
		
		// check the time differences
		for(i=0;i<this.nrows-1;i++)
		{
			if(timeArray[i]<0)
			{
				continue;
			}
			
			for(j=i+1;j<this.nrows;j++)
			{
				//
				if(timeArray[j]<0)
				{
					continue;
				}
				//
				if(Math.abs(timeArray[i]-timeArray[j]) > timeThreshold)
				{
					isValidTime = false;
					break;
				}
			}
		}
		// return the result of the check
		return isValidTime;
	}
	
	/*
	 * 
	 */
	public double getAlignmentCost()
	{		
		return this.cost;
	}
	
	/*
	 * The function checks whether the current alignment reached the goal
	 * The goal is reached when we cover all the input sequences. In other 
	 * words, all the current positions reached the last position of each of
	 * the input strings.
	 */
	public boolean isGoal()
	{
		boolean isGoalFlag = true;
		int i;
		
		// for each of the input sequences1
		for(i=0;i<this.nrows;i++)
		{
			// check if we are at the last position
			if(currentPos[i] < Alignment.numWordsList[i])
			{
				isGoalFlag = false;
				break;
			}
		}
		
		return isGoalFlag;
	}
	
	/**********************************************************************/

	/*
	 * Get the value of the heuristic function, weighted by the corresponded weight. 
	 */
	public double estimateCostFunction(double prevgCost)
	{
		//double cost = this.estimateGfunctionValue() + this.heuristicWeight * this.estimateHeuristicFunctionValue();
		
		this.gcost = this.estimateGfunctionValueFast(prevgCost);
		double cost = this.gcost + this.heuristicWeight * this.estimateHeuristicFunctionValue();
		
		return cost;
	}
	
		
	/*
	 * Estimate the cost of the path so far. Starting from the root to the current node.
	 * To estimate, first start from the initial position, and loop until the last node on the path.
	 * 
	 * At each position, we'll have a state that will indicate the gap/substitution for all the sequences.
	 * We'll add up over all these pairwise alignments to get Sum of Pairs cost.
	 */
	public double estimateGfunctionValueFast(double prevCost)
	{
		double g = 0.0;
		int i, j, k;
		
		// create the pos array
		int[] pos = new int[this.nrows];
		// set the position values to zero		
		int y = this.alignList.get(this.alignList.size()-1);
		int yprev = 0;
		
		if(this.alignList.size()==1)
		{
			double temp = Math.pow(2.0, (double)(this.nrows)) - 1.0;
			yprev = (int)(temp);
		}
		else
		{
			yprev = this.alignList.get(this.alignList.size()-2);
		}
		
		
		int []bits = this.bm.getBitsFromInteger(y, this.nrows);
		
		int []prevBits = this.bm.getBitsFromInteger(yprev, this.nrows);
		

		for(j=0;j<this.nrows-1;j++)
		{
			for(k=j+1;k<this.nrows;k++)
			{
				// get the associated word lists
				String[] wordlistj = Alignment.wordsList.get(j);
				String[] wordlistk = Alignment.wordsList.get(k);
				
				// if any one is gap, and the other is not
				if( (bits[j]==0 && bits[k]==1) || (bits[j]==1 && bits[k]==0))
				{
					if( (bits[j]==0 && prevBits[j]== 0) || (bits[k]==0 && prevBits[k]==0))
					{
						// gap continuation
						g += ConfigurableParameters.gapExtensionPenalty;						
					}
					else
					{
						g+= ConfigurableParameters.gapOpenPenalty;
					}
				}
				else if(bits[j]==1 && bits[k]==1)
				{
					//substitution
					if(this.isIncludeTime == false)
					{
						g+= getEditDistance(wordlistj[this.currentPos[j]-1], wordlistk[this.currentPos[k]-1]);
					}
					/*
					else
					{
						g+= getEditDistanceWithTimeDifference(wordlistj[this.currentPos[j]-1], wordlistk[this.currentPos[k]-1],timelistj[this.currentPos[j]-1], timelistk[this.currentPos[k]-1]);
					}*/
				}
				else
				{
					// do nothing if both are gaps. Assuming that s(_,_) = 0
				}					
								
			}
			
		}			
		
		return g+prevCost;		
	}
	
		
	/*
	 * Estimate the heuristic cost 
	 */
	public double estimateHeuristicFunctionValue()
	{
		double h = 0;		
		int i, j;		
			
		for(i=0;i<this.nrows-1;i++)
		{
			for(j=i+1;j<this.nrows;j++)
			{

				// get the strings
				int nn = Alignment.numWordsList[i];
				int mm = Alignment.numWordsList[j];
					
				int ii = nn-this.currentPos[i];
				int jj = mm-this.currentPos[j];

				h+= Alignment.listOfeditDistanceMatrices[i][j].E[ii][jj];
			}
			
		}
		return h;
	}

	
	public int nthOccurrence(String str, char c, int n) 
	{
		if(n<0)
			return 0;
		
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}


	
	
	/*
	 * Estimate word error rate
	 */
	public static double estimateWER(String str1, String str2)
	{
		
		if(str1!=null && !str1.trim().isEmpty() )
		{
			// do nothing
		}
		else
		{
			return str2.length();
		}
		
		if(str2!=null && !str2.trim().isEmpty() )
		{
			// do nothing
		}
		else
		{
			return str1.length();
		}
		
		int i, j, n, m;		
		double[][] E;
		//		
		double substitution_cost = 0;
		double temp_sub, temp_ins, temp_del;

		// first split the sentence into words
		String[] wordlist1 = str1.split("\\W");
		String[] wordlist2 = str2.split("\\W");

		// get the length of sentence parts
		n = wordlist1.length;
		m = wordlist2.length;	

		// initialize the edit distance matrix
		E = new double [n+1][m+1];
		E[0][0] = 0;
		
		
		for(i=1;i<=n;i++)
		{
			E[i][0] = i;			
		}
		for(j=1;j<=m;j++)
		{
			E[0][j] = j;			
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
				
				
				//substitution_cost = EditDistanceCalculator.getEditDistance(wordlist1[i-1],wordlist2[j-1]) * 2;
				
				
				//1. get the substitution cost
				temp_sub = E[i-1][j-1] + substitution_cost;
				//2. cost of insertion
				temp_ins = E[i-1][j] + 1;				
				//3. cost of deletion
				temp_del = E[i][j-1] + 1;
				
				if(temp_sub <= temp_ins && temp_sub <= temp_del)
				{
					E[i][j] = temp_sub;
				}
				else if(temp_ins <= temp_sub && temp_ins <= temp_del)
				{
					E[i][j] = temp_ins;
				}
				else if(temp_del <= temp_sub && temp_del <= temp_ins)
				{
					E[i][j] = temp_del;
				}
				
			}
		}
		
		return E[n][m];
	}
	
	
	
	
	/*
	 * The function estimates the pairwise edit distance between two strings.
	 * This is necessary to estimate the cost function for substitution of two words.
	 * 
	 * If two words are exactly the same, then the distance will be zero. 
	 */
	private double getEditDistance(String str1, String str2)
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
		
		if(percentage > 0.3)
		{
			retval = 0.5;
		}
		else
		{
			retval = 0;
		}

		return retval;
	}
	
	
	/*
	 * 
	 */
	private double getDLDistance(String str1, String str2)
	{
		int m = str1.length();
		int n = str2.length();
		
		//System.out.println(str1 + " "+str2);
		
		if(m==0 && n == 0)
			return 0;
		else if (m==0 || n==0)
			return 1;
		
		DamerauLevenshtein dl = new DamerauLevenshtein(str1, str2);
		
		double percentage = (double)(dl.getSimilarity())/(Math.min(m,n));

		double retval = percentage;
		
		if(percentage > 0.3)
			retval = 0.5;
		else
			retval = 0.0;
		
		return retval;
	}
	
	
	private double getEditDistanceWithTimeDifference(String str1, String str2, String time1, String time2)
	{
		
		//System.out.println("("+ str1+" "+ time1 + " )" + "("+ str2+" "+ time2 + " )");
		
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

		double deltaTime = 0;
		try
		{
			deltaTime = Math.abs ((Double.parseDouble(time1) - Double.parseDouble(time2))/1000.0)/20;
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
			

		if(percentage > 0.3)
		{
			retval = 0.5;
		}
		else
		{
			retval = 0;
		}

		retval += deltaTime;
		//*
		//retval = percentage;
		//*/
		
		return retval;
	}
	
	/*
	 * Print a given matrix
	 */
	private void printMatrix(double[] [] E, int n, int m)
	{
		int i,j;
		
		for(i=0;i<n;i++)
		{
			for(j=0;j<m;j++)
			{
				System.out.print(E[i][j]);
				System.out.print("\t");
			}
			System.out.println("");
		}
	}

	
	/*
	 * Print an alignment using all the strings
	 */
	public ArrayList<String> printAlignment()
	{
		int g = 0;
		int i, j, k;
		

		int pos=0;
		
		ArrayList<String> outputAlignment = new ArrayList<String>();
		
		for(i=0;i<this.nrows;i++)
		{
			pos = 0;
			
			String outputi = "";
			String[] wordlisti = Alignment.wordsList.get(i);
			
			for(j=0;j<this.ncols;j++)
			{
				int y = this.alignList.get(j);
				int []bits = this.bm.getBitsFromInteger(y, this.nrows);
				
				if(bits[i]==0)
				{
					System.out.print("_\t\t");
					outputi += "_" + " ";
				}				
				else
				{
					System.out.print(wordlisti[pos]+"\t\t");
					outputi += wordlisti[pos] + " ";
					pos++;
				}
			}
			System.out.println("");
			
			outputAlignment.add(outputi.trim());
		}
		
		return outputAlignment;
	}
	
	/*
	 * Get the combined string using Majority voting
	 */
	public String getCombinedStringMajorityVoting(int voteThreshold)
	{
		int i, j;		
		String combinedTranscription = "";

		//
		int y = 0;
		
		HashMap<String, Integer> votes;		
		
		String key;
		int []bits;
		int []currentPos = new int[this.nrows];
		
		// initialize the current positions to zero
		for(i=0;i<this.nrows;i++)
		{
			currentPos[i] = 0;
		}
		
		System.out.println("");
		
		for(j=0;j<this.ncols;j++)
		{
			
			votes = new HashMap<String,Integer>();
			
			y = this.alignList.get(j);
			bits = this.bm.getBitsFromInteger(y, this.nrows);
		
			for(i=0;i<this.nrows;i++)
			{
				
				String[] wordlisti = Alignment.wordsList.get(i);
			
				// take votes only for non-gap words. Ignore any gap words
				if(bits[i]!=0)
				{
					key = wordlisti[currentPos[i]];
					Integer count = votes.remove(key);
					
					if(count == null)
					{
						votes.put(key, new Integer(1));
					}
					else
					{
						votes.put(key, new Integer(count.intValue()+1));
					}
					currentPos[i]++;
				}				
			}
			// get the maximum voted string
			String majority_voted_word = getStringWithMaxVotes(votes, voteThreshold);
			System.out.print(majority_voted_word+" ");
			if(majority_voted_word!=null && !majority_voted_word.isEmpty())
			{
				combinedTranscription += majority_voted_word+ " ";
			}
			
			votes.clear();
			votes = null;
		}
		
		combinedTranscription.trim();
		System.out.println("");
		
		return combinedTranscription.trim();
	}
	

	/*
	 * Get the combined string using Majority voting
	 */
	public Sequence getCombinedSequenceMajorityVoting(int voteThreshold)
	{
		int i, j;		

		String combinedTranscription = "";
		String combinedTimeString = "";
		ArrayList<Integer> combinedWorkerIDList = new ArrayList<Integer>();

		//
		int y = 0;
		
		// define the hashmaps for storing votes, timestamps, and worker ids
		HashMap<String, Integer> votes;
		// stores the earliest timestamp for each word in the given position		
		HashMap<String, Long> timeStampsHash;
		// stores the index of the worker for the word and the smallest timestamp and word
		HashMap<String, Integer> workerIDsHash;
		
		
		String keyWord, keyTime;
		int []bits;
		int []currentPos = new int[this.nrows];
		
		// initialize the current positions to zero
		for(i=0;i<this.nrows;i++)
		{
			currentPos[i] = 0;
		}
		
		System.out.println("");
		
		for(j=0;j<this.ncols;j++)
		{
			// initialize the hashmaps
			votes = new HashMap<String,Integer>();
			
			timeStampsHash = new HashMap<String,Long>();
			workerIDsHash = new HashMap<String,Integer>();

			
			y = this.alignList.get(j);
			bits = this.bm.getBitsFromInteger(y, this.nrows);
		
			for(i=0;i<this.nrows;i++)
			{
				
				String[] wordlisti = Alignment.wordsList.get(i);
				long[] timelisti = Alignment.timeList.get(i);
			
				// take votes only for non-gap words. Ignore any gap words
				if(bits[i]!=0)
				{
					keyWord = wordlisti[currentPos[i]];
					Long timeStampLong = timelisti[currentPos[i]];
					keyTime = timeStampLong.toString();
					
					Integer count = votes.remove(keyWord);
					
					workerIDsHash.put(keyWord+" "+keyTime, new Integer(i));
					
					if(count == null)
					{
						votes.put(keyWord, new Integer(1));
						timeStampsHash.put(keyWord, timeStampLong);
					}
					else
					{
						votes.put(keyWord, new Integer(count.intValue()+1));
						if(timeStampLong < timeStampsHash.get(keyWord))
						{
							timeStampsHash.put(keyWord, timeStampLong);
						}
					}
					currentPos[i]++;
				}				
			}
			
			if(votes.isEmpty())
			{
				return null;
			}
			// get the maximum voted string
			String majority_voted_word = getStringWithMaxVotes(votes, voteThreshold);
			System.out.print(majority_voted_word+" ");
			
			if(majority_voted_word!=null && !majority_voted_word.isEmpty())
			{
				combinedTranscription += majority_voted_word+ " ";
				String timeStringTemp = timeStampsHash.get(majority_voted_word).toString(); 
				combinedTimeString +=  timeStringTemp + " ";
				combinedWorkerIDList.add(workerIDsHash.get(majority_voted_word + " " + timeStringTemp));
			}
			
			votes.clear();
			votes = null;
		}
		
		
		System.out.println("");
		
		return new Sequence(combinedTranscription.trim(), combinedTimeString.trim(), combinedWorkerIDList);
	}

	

	
	/*
	 * The function estimates the word that has majority of votes
	 */
	private String getStringWithMaxVotes(HashMap<String, Integer> maps, int voteThreshold)
	{
		int max = Integer.MIN_VALUE;
		String winnerString = null;
		
		// loop over all the keys
	    for(Object key: maps.keySet()) 
	    {
	        Integer tmp = maps.get(key);
	        
	        if(tmp.intValue() > max) 
	        {
	            max = tmp;
	            winnerString = (String)key;
	        }
	    }
	    
	  
	   if(voteThreshold>0)
	   {
		   System.out.print(" [");
		   for(Object key: maps.keySet()) 
		    {
		        Integer tmp = maps.get(key);
		        System.out.print("("+ key+":"+tmp.toString() + ")");
		        
		    }  
		   
		   System.out.print("] ");
	   }
	    
	   //System.out.println("");
	   if(max<=voteThreshold)
	    	return "";
	   
	    return winnerString;
	}
	
	
	
	
	
	private String getStringWithMaxLMProbability(HashMap<String, Double> maps, int y1, int y2, int y3)
	{
		double max = Double.MIN_VALUE;
		String winnerString = null;
		
		// loop over all the keys
	    for(Object key: maps.keySet()) 
	    {
	        Double tmp = maps.get(key);
	        
	        
	        System.out.println(tmp);
	        System.out.println(key);
	        if(tmp.doubleValue() > max) 
	        {
	            max = tmp;
	            winnerString = (String)key;
	        }
	    }
	    
	    if (winnerString == null || winnerString.isEmpty())
	    {
	    	System.out.println("Null string found");
	    	return "";
	    }
	    
	    String[] parts = winnerString.split(" ");
	   
	    return parts[1];
	    
	}
}
