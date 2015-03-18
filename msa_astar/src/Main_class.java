import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * 
 */

/**
 * @author inaim
 *
 */
public class Main_class {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		try
		{
			humanEvaluationExperiment();
			numberOfWorkersSimulation("/home/vax7/u6/inaim/MSA/data/e6p11/", 2.5, 60, false, 0, 360, 20);
			numberOfWorkersSimulation("/home/vax7/u6/inaim/MSA/data/e6p22/", 2.5, 60, false, 0, 360, 20);
			numberOfWorkersSimulation("/home/vax7/u6/inaim/MSA/data/sch1p11/", 2.5, 60, false, 0, 360, 20);
			numberOfWorkersSimulation("/home/vax7/u6/inaim/MSA/data/sch1p22/", 2.5, 60, false, 0, 360, 20);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		*/
		
		parseInputParameters(args);
		double wer = testScriptOnScribeDataByTime();
		
				
	}
	
	/**
	 * Parse the input arguments
	 * @param args
	 */
	public static void parseInputParameters(String[] args)
	{
		
		for(int i=0;i<args.length;i++)
		{
			System.out.println(args[i].substring(0, 2));
			if(args[i].substring(0, 2).equals("-c"))
			{
				ConfigurableParameters.chunkSize = Integer.parseInt(args[i].substring(2));
				
			}
			else if(args[i].substring(0, 2).equals("-d"))
			{
				ConfigurableParameters.dataDirectoryPath = args[i].substring(2);
				if(!ConfigurableParameters.dataDirectoryPath.endsWith("/"))
					ConfigurableParameters.dataDirectoryPath += "/";
			}
			else if(args[i].substring(0, 2).equals("-b"))
			{
				ConfigurableParameters.beamSize = Double.parseDouble(args[i].substring(2));
			}
			else if(args[i].substring(0, 2).equals("-s"))
			{
				ConfigurableParameters.startTime = Integer.parseInt(args[i].substring(2));
			}
			else if(args[i].substring(0, 2).equals("-e"))
			{
				ConfigurableParameters.endTime = Integer.parseInt(args[i].substring(2));
			}
			else if(args[i].substring(0, 2).equals("-w"))
			{
				ConfigurableParameters.weight = Double.parseDouble(args[i].substring(2));
			}
			else if(args[i].substring(0, 2).equals("-r"))
			{
				if(args[i].substring(2).equals("1"))
				{
					ConfigurableParameters.checkForRepeatedStates = true;
				}
				else if(args[i].substring(2).equals("0"))
				{
					ConfigurableParameters.checkForRepeatedStates = false;
				}
				
			}
			else if(args[i].substring(0, 2).equals("-go"))
			{
				ConfigurableParameters.gapOpenPenalty = Double.parseDouble(args[i].substring(3));
			}
			else if(args[i].substring(0, 2).equals("-ge"))
			{
				ConfigurableParameters.gapExtensionPenalty = Double.parseDouble(args[i].substring(3));
			}
			
			
		}
		
		System.out.println("Directory: " + ConfigurableParameters.dataDirectoryPath);
		System.out.println("ChunkSize: " + ConfigurableParameters.chunkSize);
		System.out.println("Heuristic Weight: " + ConfigurableParameters.weight);
		System.out.println("Beamsize: " + ConfigurableParameters.beamSize);
		System.out.println("GapOpen: " + ConfigurableParameters.gapOpenPenalty);
		System.out.println("GapExtension: " + ConfigurableParameters.gapExtensionPenalty);
		//System.out.println();
		//System.exit(0);
	}
	
	/*
	 * This function will process several scribe files, extract multiple sequences, and will align them
	 */
	public static double testScriptOnScribeDataByTime()
	{
		
		int i;
		// initialize variables
		ArrayList<Sequence> sequences;
		// the output strings
		String output_no_threshold = "";
		String output_with_threshold = "";
		String output_with_lm = "";
		String output_with_newlines = "";
		String output_with_threshold_newline = "";

		
		// number of words in ground truth caption
		int nGrounds = 0;

		// initialize the language model
		LanguageModel lm = null;
		//
		if(ConfigurableParameters.isLM == true)
		{
			// get the pretrained language model
			lm = new LanguageModel(ConfigurableParameters.lmFilePath);
		}
		
		// the ground truth caption
		String ground = "";
		String answerGraphicalModel = "";
		String answerMuscle = "";
		
		/******************************************************************************/
		// read the ground truth caption from file
		try
		{
			// extract the ground truth caption
			ground = DataReaderScribe.readGroundTruthCaptionByTimeInterval(ConfigurableParameters.dataDirectoryPath + "ground/ground.bas", ConfigurableParameters.startTime-5, ConfigurableParameters.endTime);			
			String[] parts  = ground.split(" ");
			nGrounds = parts.length;
			
			// get the output from graphical model for the sake of comparison			
			answerGraphicalModel = DataReaderScribe.getGraphicalModelOutputInGivenTimeInterval(ConfigurableParameters.dataDirectoryPath + "graphical_model/answer.txt", ConfigurableParameters.startTime, ConfigurableParameters.endTime);
			
			answerMuscle = DataReaderScribe.getMuscleOutput(ConfigurableParameters.dataDirectoryPath + "muscle/merged.txt");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		/*****************************************************************************/

		/*
		 *  Perform Alignment using A* algorithm 
		 */
		// extract the chunks and align them
		long startingTimeAlignment = 0;
		long estimatedTime = 0;// = System.currentTimeMillis();

		for(i=(ConfigurableParameters.startTime)*1000; (i+ConfigurableParameters.chunkSize*1000) <= ConfigurableParameters.endTime*1000;i+= ConfigurableParameters.chunkSize*1000)
		{
			// get the chunks
			sequences = DataReaderScribe.getTranscriptionSequencesByTimeInterval(ConfigurableParameters.dataDirectoryPath, i, i+(ConfigurableParameters.chunkSize *1000));
			//double gapPenalty = 0.125; 
			//ConfigurableParameters.gapOpenPenalty = gapPenalty;

			// 
			startingTimeAlignment = System.currentTimeMillis();
			// create the MSA object
			MSA myMSA = new MSA(sequences, sequences.size(), ConfigurableParameters.beamSize);
			
			// call the A* alignment program
			Alignment alignOut = myMSA.getOptimalAlignment_Astar(ConfigurableParameters.weight, Double.MAX_VALUE);

			if(alignOut==null)
			{
				continue;
			}
			
			alignOut.printAlignment();
			/*
			 * get the majority vote (without threshold)
			 */
			// get the output values via majority voting (without any threshold)
			output_no_threshold +=alignOut.getCombinedStringMajorityVoting(0) + " ";
			// get the outputs with newline as separator. This will be helpful to investigate the errors at boundary
			output_with_newlines += alignOut.getCombinedStringMajorityVoting(0) + "\n* ";
			
			/*
			 * get the output (with threshold)
			 */
			output_with_threshold += alignOut.getCombinedStringMajorityVoting(1) + " ";
			output_with_threshold_newline += alignOut.getCombinedStringMajorityVoting(1) + "\n* ";
			
			
			estimatedTime += System.currentTimeMillis() - startingTimeAlignment;
			
			Sequence outputSeq = alignOut.getCombinedSequenceMajorityVoting(1);
			
			alignOut = null;
			myMSA = null;
		}
		
		System.out.println("");System.out.println("");
		System.out.println("---------------------------------");
		System.out.println("Output without threshold:");
		output_no_threshold = removeDuplicateWords(output_no_threshold);
		System.out.println(output_no_threshold);
		
		System.out.println("");System.out.println("");
		System.out.println("---------------------------------");
		System.out.println("Output with threshold:");
		output_with_threshold = removeDuplicateWords(output_with_threshold);
		System.out.println(output_with_threshold);

		System.out.println("");System.out.println("");
		System.out.println("---------------------------------");
		output_no_threshold = removeDuplicateWords(output_no_threshold);
		System.out.println("Output without threshold:");
		System.out.println(output_with_newlines);
		
		System.out.println("");System.out.println("");
		System.out.println("---------------------------------");
		System.out.println("Output with threshold:");
		System.out.println(output_with_threshold_newline);
		System.out.println("Ground Truth: -----------------");		
		System.out.println(ground);

		
		/*************************************************************************************/
		/*
		 * Measure word error rate
		 */
		double WER1 = Alignment.estimateWER(output_no_threshold, ground)/ (double)(nGrounds);		
		double WER2 = Alignment.estimateWER(output_with_threshold, ground)/ (double)(nGrounds);
		double WERGraph = Alignment.estimateWER(answerGraphicalModel, ground)/ (double)(nGrounds);
		double WERMuscle = Alignment.estimateWER(answerMuscle, ground)/ (double)(nGrounds);
		/**************************************************************************************/
		/*
		 * Measure BLEU
		 */		
		// for no threshold case
		BleuMeasurer bm = new BleuMeasurer();
		String[] refTokens = ground.split(" ");
		String[] candTokens = output_no_threshold.split(" ");		
		bm.addSentence(refTokens, candTokens);
		double bleu_nothreshold = bm.bleu();

		// bleu with threshold
		BleuMeasurer bm2 = new BleuMeasurer();
		String[] candTokens2 = output_with_threshold.split(" ");		
		bm2.addSentence(refTokens, candTokens2);
		double bleu_threshold = bm2.bleu();
		
		
		// compare for graphical model version
		BleuMeasurer bmGraph = new BleuMeasurer();
		String[] candTokensGraph = answerGraphicalModel.split(" ");// new String[1]; candTokens[0] = process_output;		
		bmGraph.addSentence(refTokens, candTokensGraph);
		double bleu_graph = bmGraph.bleu();
		
		// compare for graphical model version
		BleuMeasurer bmMuscle = new BleuMeasurer();
		String[] candTokensMuscle = answerMuscle.split(" ");// new String[1]; candTokens[0] = process_output;		
		bmMuscle.addSentence(refTokens, candTokensMuscle);
		double bleu_muscle = bmMuscle.bleu();

		/********************************************************************************/
		System.out.print("");System.out.print("");
		System.out.println(/************************************************/);
		System.out.println("The graphical model output:");
		System.out.println(answerGraphicalModel);
		System.out.println("");
		System.out.println("The final output with threshold:");
		System.out.println(output_with_threshold);
		System.out.println("");
		System.out.println("The final output without threshold:");
		System.out.println(output_no_threshold);


		System.out.println(/************************************************/);		
		System.out.println("Chunk size: " + ConfigurableParameters.chunkSize);
		System.out.println("Heuristic Weight: " + ConfigurableParameters.weight);
		System.out.println("BLEU scores:------------------");
		System.out.println("BLEU without threshold:" + bleu_nothreshold);
		System.out.println("BLEU with threshold:" + bleu_threshold);
		System.out.println("BLEU with Graph:" + bleu_graph);
		System.out.println("BLEU with Muscle:" + bleu_muscle);
		System.out.println(/************************************************/);	
		System.out.println("Fmeasure scores:------------------");
		System.out.println("Fmeasure without threshold:" + getFmeasure(ground, output_no_threshold));
		System.out.println("Fmeasure with threshold:" + getFmeasure(ground, output_with_threshold));
		System.out.println("Fmeasure with Graph:" + getFmeasure(ground, answerGraphicalModel));
		System.out.println("Fmeasure with Muscle:" + getFmeasure(ground, answerMuscle));
				
		System.out.println("WER scores:------------------");
		System.out.println("WER without threshold:" + WER1);
		System.out.println("WER with threshold:" + WER2);
		System.out.println("WER with Graph:" + WERGraph);
		System.out.println("WER with Muscle:" + WERMuscle);

		
		System.out.println("Estimated Time:" + estimatedTime);
		
		
		// for the language model case
		if(ConfigurableParameters.isLM)
		{
			String process_output_lm = removeDuplicateWords(output_with_lm);
			System.out.println(process_output_lm);
		
		
			// get the blue score for the majority voted string
			//
			BleuMeasurer bm1 = new BleuMeasurer();
			String[] candTokens1 = process_output_lm.split(" ");// new String[1]; candTokens[0] = process_output;		
			bm1.addSentence(refTokens, candTokens1);
			System.out.println("BLUE with LM: " + bm1.bleu());
			double WER4 = Alignment.estimateWER(process_output_lm, ground)/(double)(nGrounds);
			System.out.println("WER with LM: " + WER4);
		}
		
		
		System.gc();
		
		 try 
		 {
		        Thread.sleep(2000);		 
		 } 
		 catch (InterruptedException x) 
		 {
		        Thread.currentThread().interrupt();
		 }
		//System.out.println(output_with_newlines);
		
		return 0;
		
	}
	

	/*
	 * The function removes repeated words from the final alignment
	 */
	public static String removeDuplicateWords(String str)
	{
		
		if(str == null)
		{
			return null;
		}
		else if(str.trim().isEmpty())
		{
			return null;
		}
		else if(str.trim().equals(""))
		{
			return str;
		}
		
		String[] words = str.split(" ");
		String output = words[0] + " ";
		
		for(int i=1;i<words.length;)
		{
			if( words[i].equals(words[i-1]) )
			{
				i++;
				continue;
			}
			

			if(i < words.length -2)
			{
				if(i>1 && words[i].equals(words[i-2]) && words[i+1].equals(words[i-1]) )
				{
					i+= 2;
					continue;
				}
			}
			
			//System.out.println(i + " "+ words.length);
			
			if(i< words.length -3)
			{
				if(i>2 && words[i].equals(words[i-3]) && words[i+1].equals(words[i-2]) && words[i+2].equals(words[i-1]))
				{
					i+= 3;
					continue;
				}
				
			}


			output += words[i] + " ";
			i++;				
			
		}
		
		return output.trim();
	}
	
	/*
	 *	Estimate the word error rate for the human workers 	
	 */
	public static void estimateWERForHumanWorkers(String directory_path)
	{
		ArrayList<Sequence> sequences;
		// initialize the groud truth string
		String ground = "";
		int nGrounds = 0;
		
		double avg = 0;
		
		// get the ground truth from file
		try
		{
			ground = DataReaderScribe.readGroundTruthCaption(directory_path + "ground/ground.bas");
			String[] parts  = ground.split(" ");
			nGrounds = parts.length;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		// read all the transcriptions starting from offset = 0, and upto offset = 300000. This will return all the five minitue transcripts
		sequences = DataReaderScribe.getTranscriptionSequencesByTimeInterval(directory_path, 0, 300000);
		
		// for each of the transcript sequences, print the word error rate
		for(int i = 0;i<sequences.size();i++)			
		{
			double WER1 = Alignment.estimateWER(sequences.get(i).sequenceString, ground)/ (double)(nGrounds);
			System.out.println("WER: "+ WER1);
			avg += WER1;
		}
		
		System.out.println("AVG: " + avg/sequences.size());

	}
	
	
	 static void numberOfWorkersSimulation(String directory_path, double heuristic_weight, int chunksizeInSeconds, boolean useLM, int startTimeInSec, int endTimeInSec, double beamsizeInSeconds)
	{
		
		int i, j;
		long startTime = 0;
		long estimatedTime = 0;// = System.currentTimeMillis();
		double gapPenalty = 0.125; 
		ConfigurableParameters.gapOpenPenalty = gapPenalty;

		// the output strings
		String output_no_threshold = "";
		String output_with_threshold = "";
		String output_with_lm = "";
		
		String ground = "";
		
		try
		{
			ground = DataReaderScribe.readGroundTruthCaptionByTimeInterval(directory_path + "ground/ground.bas", startTimeInSec-5, endTimeInSec);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		String[] parts  = ground.split(" ");
		int nGrounds = parts.length;

		double [][]werlist = new double[10][20];
		long [][]timelist = new long[10][20];
		
		for(int k = 2;k<11;k++)
		{
		
			for(int repeat = 0;repeat<20;repeat++)
			{
				ArrayList<Sequence> sequences;
		
				int[] inclusionFlags = getInclusionFlags(k);
				
				// the output strings
				output_no_threshold = "";
				output_with_threshold = "";
				
				estimatedTime = 0;
				for(i=(startTimeInSec)*1000; (i+chunksizeInSeconds*1000) <= endTimeInSec*1000;i+=chunksizeInSeconds*1000)
				{
					// get the chunks
					sequences = DataReaderScribe.getRandomlySelectedTranscriptionSequencesByTimeInterval(directory_path, i, i+(chunksizeInSeconds)*1000, inclusionFlags);
					startTime = System.currentTimeMillis();
					// create the MSA object
					MSA myMSA = new MSA(sequences, sequences.size(), beamsizeInSeconds);			
					// call the A* alignment program
					Alignment alignOut = myMSA.getOptimalAlignment_Astar(heuristic_weight, Double.MAX_VALUE);
		
					if(alignOut==null)
					{
						continue;
					}
					
					output_with_threshold += alignOut.getCombinedStringMajorityVoting(1) + " ";
					
					estimatedTime += System.currentTimeMillis() - startTime;
					
					alignOut = null;
					myMSA = null;
					
				}
				
				System.out.println("Output with threshold:");
				output_with_threshold = removeDuplicateWords(output_with_threshold);
				double WER = Alignment.estimateWER(output_with_threshold, ground)/ (double)(nGrounds);
				
				System.out.println("WER = " + WER);
				werlist[k-2][repeat] = WER;
				timelist[k-2][repeat] = estimatedTime;
			}
		}
		
		double avgWER, avgTime;
		
		
		for(int k=2;k<11;k++)
		{
			avgWER = 0.0;
			avgTime = 0.0;
			
			for(i=0;i<20;i++)
			{
				avgWER += werlist[k-2][i];
				
				avgTime += (double)(timelist[k-2][i]);
			}
			
			avgWER = avgWER/20.0;
			avgTime = avgTime/20.0;
			System.out.println(k + "," + avgWER + "," + avgTime);
		}
			
	}
	
	
	static int[] getInclusionFlags(int k)
	{
		Vector<Integer> itemsVector = new Vector<Integer>();
		 
		int nn = 10;
		int i;
		int inclusionFlags[] = new int[nn];
		
		for(i=0;i<nn;i++)
		{
			itemsVector.add(new Integer(i));
			inclusionFlags[i] = 0;
		}
		
		Collections.shuffle(itemsVector);
		
		for(i=0;i<k;i++)
		{
			inclusionFlags[itemsVector.get(i).intValue()] = 1;
		}

		return inclusionFlags;
	}
	/*public static void mySimpleToyTest()
	{

		// simple six sequence alignment
		int numSeq = 6;
		ArrayList<String> strings = new ArrayList<String>(); 
		ArrayList<String> times = new ArrayList<String>();
		
		// create the arraylist containing strings
		String str1 = new String("Hello my friend how you doing?");
		String str2 = new String("Hellow how are doing?");
		String str3 = new String("Hello my are you doing?");
		String str4 = new String("Hello how are you doing?");
		String str5 = new String("Hellow my friend doing?");
		String str6 = new String("Hello are you doing?");
		// add the strings to the arraylist
		strings.add(str1);
		strings.add(str2);
		strings.add(str3);
		strings.add(str4);
		strings.add(str5);
		strings.add(str6);
		
		

		// create an object of myMSA class
		double gapPenalty = 2;
		MSA myMSA = new MSA(strings, numSeq, gapPenalty);
				
		int ed = myMSA.getEditDistance(str1, str2);
		System.out.println(ed);		
		myMSA.performPairwiseSenteceAlignment(str1, str2);
		

		// call the A* alignment program
		Alignment A = myMSA.getOptimalAlignment_Astar(100.0, Double.MAX_VALUE);
		// print the output alignment
		A.printAlignment();
	}*/
	
	
	
	
	public static double getFmeasure(String reference, String candidate)
	{
		double fmeasure = 0;
		
		// for no threshold case
		BleuMeasurer bm = new BleuMeasurer();
		String[] refTokens = reference.split(" ");
		String[] candTokens = candidate.split(" ");		
		bm.addSentence(refTokens, candTokens);	
		
		return bm.fmeasure();	
		
	}
	

	public static double getFmeasureBLEU(String reference, String candidate)
	{
		double fmeasure = 0;
		
		// for no threshold case
		BleuMeasurer bm = new BleuMeasurer();
		String[] refTokens = reference.split(" ");
		String[] candTokens = candidate.split(" ");		
		bm.addSentence(refTokens, candTokens);	
		
		return bm.fmeasureBLEU();	
		
	}

	
	public static void humanEvaluationExperiment() throws IOException
	{

		ArrayList<String> groundTruthList = new ArrayList<String>(); 
		ArrayList<String> transcripits = new ArrayList<String>();
		
		// read the data
		BufferedReader br = null;
		String sequenceString = "";
		
		try 
		{
			br = new BufferedReader(new FileReader("/u/inaim/MSA/rochci/transcription/msa/human_evaluation_docs/human_evaluation_final.txt"));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

			int linecount = 0;
			int index = 0;
       
			while (line != null) 
			{
				linecount++;
				
				System.out.println(line);
				
				index = (linecount-1)%5;
				
				if(index==0)
				{
					groundTruthList.add(line.trim());
				}
				else 
				{
					transcripits.add(line.trim());
				}
				
				line = br.readLine();	        
			}
		} 
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally 
		{
			if(br!=null)
			{
				br.close();
			}
		}
		///////////////////////////////////////////////////////////////
		// NOW PERFORM THE EVALUATION TASK
		BleuMeasurer bm = new BleuMeasurer();
		
		for(int i=0;i<4;i++)
		{
			System.out.println(i);
			String ground = groundTruthList.get(i);
			String[] refTokens = ground.split(" ");
			int nGrounds = refTokens.length;
			for(int j=0;j<4;j++)
			{
				String caption = transcripits.get(i*4 + j);
				String[] candTokens = caption.split(" ");		

				// get wer
				double WER = Alignment.estimateWER(caption, ground)/ (double)(nGrounds);
				
				/*
				  WordSequenceAligner werEval = new WordSequenceAligner();
				  WordSequenceAligner.WERAlignment a = werEval.align(refTokens, candTokens);
				  System.out.println(a);
				*/
				//System.out.println("WER =" + WER);
				System.out.print(WER+",");
				// get bleu
				bm.addSentence(refTokens, candTokens);
				double bleu = bm.bleu();
				//System.out.println("BLEU =" + bleu);
				System.out.print(bleu+",");
				
				/*
				double dice = bm.DICE();
				System.out.print(dice + ",");
				*/
				bm.reset();
				//System.out.println("Fmeasure:" + getFmeasure(ground, caption));
				System.out.println(getFmeasure(ground, caption));
				
			}

		}

	}
	
}


