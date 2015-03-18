/**
 * @author inaim
 * 
 * The class to estimate multiple sequence alignment.
 *  
 * I plan the class to contain many different implementation of MSA algorithms.
 * For the time being, we are going to start with the A* algorithm.
 * 
 */


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

/*
 * The class to perform multiple sequence alignment.
 * At this point, it uses the A* search for aligning the sequences.
 */
public class MSA {
	
	// The list of sequences and their corresponding time values
	public ArrayList<String[]> wordsList;
	public ArrayList<long[]> timeList;
	
	// The pre-computed matrix of edit distance matrices
	public EditDistanceMatrix[][] listOfeditDistanceMatrices;
	
	// the number of sequences for MSA
	public int K;
	
	// few constant values
	private int LEFT = 1;
	private int UP = 2;
	private int DIAG = 3;
	
	// number of successors from each node
	private int nSuccessors;

	// the priority list used by the A* search
	public PriorityQueue<Alignment> openlist;
	
	public HashMap<String, Double> closedlist;
	
	// the comparator for the priority queue
    Comparator<Alignment> comparator;
    
    public double beamSizeInSeconds;
    
    	
	/**************************************************/
	/*
	 * The constructor for MSA class
	 */
	public MSA(int numSeq)
	{
		this.wordsList = null;
		this.timeList = null;
		
		//this.gapOpenPenalty = gap_penalty;
		
		this.K = numSeq;
		
		this.comparator = new AlignmentComparator();		
		
		this.openlist =  new PriorityQueue<Alignment>(10, this.comparator);
		
		this.closedlist = new HashMap<String, Double>();
		
		this.nSuccessors = (int)(Math.pow(2, this.K));
	}
	
	/*
	 * The constructor function for MSA class. Gets all the string sequences as inputs
	 */
	public MSA(ArrayList<Sequence> seqList, int numSeq, double beamSizeInSeconds)
	{	
		// clear out the static variables in Alignment class 
		Alignment.wordsList = null;
		Alignment.timeList = null;
		Alignment.numWordsList = null;
		Alignment.listOfeditDistanceMatrices = null;
		
		
		this.beamSizeInSeconds = beamSizeInSeconds;
		this.wordsList = new ArrayList<String[]>();
		this.timeList = new ArrayList<long[]>();
		
		// iterate over the sequence list. Each list item has a string of words and corresponding time stamps.
		for(int i=0;i<seqList.size();i++)
		{
			Sequence seq_i = seqList.get(i);
			
			// get the parts separated by space character
			String[] strParts = seq_i.sequenceString.trim().split(" ");
			String[] timeParts = seq_i.timeString.trim().split(" ");

			// check if the sequence is non-empty
			if(seq_i.sequenceString == null || seq_i.sequenceString.isEmpty() || seq_i.sequenceString.equals(""))
			{
				// in case there is no words in the window for that worker
				// empty sequence
				this.wordsList.add(new String[0]);
				this.timeList.add(new long[0]);
			}
			else
			{
				// non-empty sequence
				// add them to the list
				this.wordsList.add(strParts);
				this.timeList.add(this.convertStringArrayToLongArray(timeParts));
			}
			
			// print for debugging purpose
			for(int l=0;l<strParts.length;l++)
			{
				System.out.print(strParts[l] + "(" + timeParts[l] + ")" );
			}
			System.out.println();
			
		}
		
		//this.gapOpenPenalty = gap_penalty;
		
		this.K = numSeq;

		
		this.comparator = new AlignmentComparator();	
		
		this.openlist =  new PriorityQueue<Alignment>(10, this.comparator);
		
		this.closedlist = new HashMap<String, Double>();
		
		this.nSuccessors = (int)(Math.pow(2, this.K));
		
		// set up the pre-computed edit distance matrices.
		this.listOfeditDistanceMatrices = new EditDistanceMatrix[this.K][this.K];
		this.setupEditDistanceMatrices();
		
	}
	
	/*
	 * The function to pre-compute the pairwise edit distances among the sequences
	 */
	public void setupEditDistanceMatrices()
	{
		int i, j;
		
		for(i=0;i<this.K-1;i++)
		{
			for(j=i+1;j<this.K;j++)
			{
				this.listOfeditDistanceMatrices[i][j] = EditDistanceCalculator.getPairwiseSenteceAlignmentCostReverseOrder(this.wordsList.get(i), this.wordsList.get(j), ConfigurableParameters.gapOpenPenalty, ConfigurableParameters.gapExtensionPenalty);
			}
		}
	}
	
	public String convertIntArrayToString(int array[])
	{
		String str = "";
		for(int i = 0;i< this.K;i++)
		{
			str += array[i];
		}
		
		return str;
	}
	
	/*
	 * The function that estimates the optimal alignment
	 */
	public Alignment getOptimalAlignment_Astar(double heuristic_weight, double upper_bound)
	{
		long sameCount = 0; 
		
		int i;
		int numberOfQueueRemoval = 0;

		long startTime = System.currentTimeMillis();
		
		/*
		 * Perform initialization
		 * 1. create all the initial state nodes.
		 * 2. add to the priority queue openlist 
		 */
		for(i=1;i<this.nSuccessors;i++)
		{
			// create a new alignment object
			Alignment a = new Alignment(this.wordsList, this.timeList, this.K, i, heuristic_weight, upper_bound, this.listOfeditDistanceMatrices);

			// add the newly created alignment object in the priority queue
			if(a.cost!=-1)
			{
				this.openlist.add(a);
			}
		}
		
		
		/*
		 * Iteration
		 * 
		 * continue iteration until the openlist becomes empty
		 */		
		while(!this.openlist.isEmpty())
		{
			// get the node with the smallest cost
			Alignment currentNode = this.openlist.remove();
			
			numberOfQueueRemoval++;
			
			// insert it into the closed list
			if(ConfigurableParameters.checkForRepeatedStates == true)
			{
				this.closedlist.put(currentNode.currentPosString, new Double(currentNode.gcost));
			}
			
			System.out.print(".");
			
			if(currentNode.isGoal())
			{
				// reached the optimum alignment, so return.				
				System.out.println("Number of Queue Removal: " + numberOfQueueRemoval);
				
				long estimatedTime = System.currentTimeMillis() - startTime;
				
				System.out.println("");
				System.out.println("Time spent in alignment:" + estimatedTime + " ms");
				System.out.println("Same Count = " + sameCount);
				System.out.println("Closed list size = " + this.closedlist.size());
				
				this.openlist.clear();
				this.closedlist.clear();
				
				return currentNode;
			}
			
			// add all the successors of the current node. Push them on the priority queue
			for(i=1;i<this.nSuccessors;i++)
			{
				// create the successor alignment object
				Alignment a = currentNode.getSuccessor(i);
				
				// check if a is a valid node. If not, skip
				if(a!=null && a.cost <= a.upperBound)
				{
					
					//System.out.println(this.beamSizeInSeconds);
					// check if the current node satisfies the time constraint
					if(!a.isValidAlignmentInTermsOfTime(this.beamSizeInSeconds*1000))
					{
						//System.out.println("Time invalid alignment");
						continue;
					}

					// check if this state was visited before
					if(ConfigurableParameters.checkForRepeatedStates == true)
					{
						// remove from hashmap
						Double tempG = this.closedlist.remove(a.currentPosString);
						
						if(tempG!=null)
						{
							// found a better way to reach the state. so insert the new value						
							if(tempG.doubleValue() > a.gcost)
							{
								this.closedlist.put(a.currentPosString, new Double(a.gcost));
							}
							else
							{
								// the new way is worse than before. So skip
								this.closedlist.put(a.currentPosString, tempG);
								sameCount++;
								continue;
							}
						}
					}
					this.openlist.add(a);
				}
				
			}
		}

		// could not reach goal and search ended possibly due to some error
		return null;
	}
	
	
	/*
	 * Get the weighted edit distance between two strings. 
	 */
	public int getWeightedEditDistance(String str1, String str2)
	{
		int weightedEditDistance = 0;		
		
		return weightedEditDistance;
	}
	
	
	/*
	 * Print a given matrix (double)
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
	 * Print a given matrix (int)
	 */
	private void printMatrix(int[] [] E, int n, int m)
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
	 * The function estimates the pairwise edit distance between two strings.
	 * This is necessary to estimate the cost function for substitution of two words.
	 * 
	 * If two words are exactly the same, then the distance will be zero. 
	 */
	public int getEditDistance(String str1, String str2)
	{
		int n, m, i, j;
		int[][] E;
		// get the lengths of both the strings
		n = str1.length();
		m = str2.length();
		// initialize the array
		E = new int [n+1][m+1];

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
		
		return E[n][m];	
		
	}
	
	
	/*
	 * Print the pairwise aligned string, using the alignment backtracking matrix B
	 * 
	 *  
	 */
	public void printPairwiseAlignedString(int[][] B, String[] wordlist1, String[] wordlist2)
	{
		int n, m, i, j;
		// get the length of sentence parts
		n = wordlist1.length;
		m = wordlist2.length;
		
		
		i = n;
		j = m;
		
		/*
		 * Loop over the B matrix to print the aligned output string
		 */
		while(true)
		{
			if(i==0 && j==0)
				break;
			
			// substitution
			if(B[i][j] == this.DIAG)
			{
				i--;
				j--;
				
				System.out.print( "(" + wordlist1[i] + "," + wordlist2[j] + ") ");
			}
			// move left
			else if(B[i][j] == this.LEFT)
			{				
				j--;
				System.out.print( " " + wordlist2[j]);
			}
			// move right
			else
			{
				i--;
				System.out.print( " " + wordlist1[i]);
			}
			
		}
	}
	
	
	/*
	 * Convert an array of strings (for time) to an array of long
	 */
	private long[] convertStringArrayToLongArray(String[] stringArray)
	{
		
		long[] longArray = new long[stringArray.length];
		
		System.out.println(stringArray.length);
		
		/*
		 * Iterate the list
		 */
		for(int i=0;i<stringArray.length;i++)
		{
			longArray[i] = Long.parseLong(stringArray[i]);
		}
		
		return longArray;
	}
}
