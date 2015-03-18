import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 */

/**
 * @author inaim
 *
 */
public class DataReaderScribe {
	
	/*
	 * The function processes all the files in the given directory path.
	 * It reads the files, extracts the sequence of words and build an arraylist
	 * of strings, where each string is a transcription sequence of an audio file.
	 */
	public static ArrayList<Sequence> getTranscriptionSequencesByTimeInterval(String directory_path, int startTime, int endTime)
	{		
		
		ArrayList<Sequence> sequences = new ArrayList<Sequence>();
		
		String rawSequence = "";
		String singleSequence = "";
		String timeSequence = "";
		//
		String[] timeStringParts;
		int flag = 0;
		//
		int i, j;
		// Read all the files from the given directory		
		ArrayList<String> filenames = getFilenamesFromDirectory(directory_path);
		/*
		 * 
		 */
		for(i=0;i<filenames.size();i++)
		{

			// for each file, process
			try
			{
				// get the raw sequence, with all the tokens and separators
				rawSequence = getSingleTranscriptionFromFile(directory_path + filenames.get(i));
				// initialize the sequences
				singleSequence = "";
				timeSequence = "";
				String[] parts = rawSequence.split(":");
				
				flag = 0;
				
				for(j=1;j<parts.length; j+=2)
				{					

					timeStringParts = parts[j+1].split("\n");
					int time = Integer.parseInt(timeStringParts[0]);
					
					// filter out unwanted parts outside the given time window
					if(time <= startTime || time > endTime)
					{
						continue;
					}
					// filter out words at time 0
					if(time == 0)
					{
						continue;
					}					
					// avoid processing "uh" words
					if(parts[j].equals("uh"))
					{
						continue;
					}
					
					if(flag == 0)
					{
						// start of a string
						singleSequence = parts[j];	
						timeSequence = timeStringParts[0];
						
					}
					else
					{
						// middle of a string
						singleSequence += " " + parts[j];
						timeSequence += " " + timeStringParts[0];
					}
					
					// marked that passed the start of a sequence
					flag = 1;
				}				
			}
			catch(IOException e)
			{
				e.printStackTrace();				
			}
			
			
			/*
			// spell check? can be made as a user defined parameter
			System.out.println(singleSequence);
			singleSequence = SpellCheckerForCaptions.getSpellCheckedStringHunspell(singleSequence);
			System.out.println(singleSequence);
			*/
			
			// add the sequence to the list of all the transcription sequences
			sequences.add(new Sequence(singleSequence, timeSequence));			
		}
	
		return sequences;
	}
	
	/*
	 * Read a single file and return the raw transcript script (with all the separators, time stamps, not tokenized)
	 */
	public static String getSingleTranscriptionFromFile(String filepath) throws IOException
	{
		BufferedReader br = null;
		String sequenceString = "";
		try 
		{
			br = new BufferedReader(new FileReader(filepath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

			int linecount = 0;
       
			while (line != null) 
			{
				linecount++;				
				
				sb.append(line);	           
				sb.append("\n");
           
				line = br.readLine();	        
			}
       
			sequenceString = sb.toString();
   
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

		// return the raw sequence string read from the file
		return sequenceString;
	}
	
	
	
	
	/*
	 * Get the list of all the text files in a directory
	 */
	public static ArrayList<String> getFilenamesFromDirectory(String directoryPath)
	{
		// define the array list for containing file names		
		ArrayList<String> filenames = new ArrayList<String>();		
		File folder = new File(directoryPath);
		File[] listOfFiles = folder.listFiles(); 
		 
		// iterate over all the files, and add their names to the arraylist
		for (int i = 0; i < listOfFiles.length; i++)
		{
			if (listOfFiles[i].isFile()) 
			{			
				filenames.add(listOfFiles[i].getName());
				//System.out.println(listOfFiles[i].getName());
			}
		} 
		return filenames;
	}
	
	
	/*
	 * Read the ground truth caption from the given file
	 */
	public static String readGroundTruthCaption(String filepath) throws IOException
	{
		BufferedReader br = null;
		String trueCaption = "";
		String []parts;
		try 
		{
			br = new BufferedReader(new FileReader(filepath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

       
			while (line != null) 
			{
				parts = line.split(":");
				
				sb.append(parts[0]+ " ");
           
				line = br.readLine();	        
			}
       
			trueCaption += sb.toString();
   
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
		
			
		return trueCaption.trim();
		
	}

	
	/*
	 * Read the ground truth caption from the given file within a time interval
	 */
	public static String readGroundTruthCaptionByTimeInterval(String filepath, int startTimeInSecond, int endTimeInSecond) throws IOException
	{
		BufferedReader br = null;
		String trueCaption = "";
		String []parts;
		try 
		{
			br = new BufferedReader(new FileReader(filepath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

			int timestamp = 0;
			
			while (line != null) 
			{
				parts = line.split(":");
				
				timestamp = Integer.parseInt(parts[1]);
				
				// append only if the time stamp is within the given time window
				if(timestamp > startTimeInSecond*1000 && timestamp<endTimeInSecond*1000)
				{
					if(parts[0].equals(new String("ive")))
					{
						parts[0] = "i have";
					}
					
					
					sb.append(parts[0]+ " ");
				}				
           
				line = br.readLine();
				
			}
       
			trueCaption += sb.toString();
   
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
		
			
		return trueCaption.trim();
	}

	
	/*
	 * get the output of Graphical Model based system
	 */
	public static String getGraphicalModelOutput(String answerFilePath) throws IOException
	{
		BufferedReader br = null;
		String answer = "";
		String []parts;

		try 
		{
			br = new BufferedReader(new FileReader(answerFilePath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();
       
			while (line != null) 
			{
				parts = line.split(":");
				
				sb.append(parts[1]+ " ");
           
				line = br.readLine();	        
			}
       
			answer += sb.toString();
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
			
		return answer.trim();
	}
	
	/*
	 * get the output of Graphical Model based system, within a given time interval.
	 */
	public static String getGraphicalModelOutputInGivenTimeInterval(String answerFilePath, int startTimeInSecond, int endTimeInSecond) throws IOException
	{
		BufferedReader br = null;
		String answer = "";
		String []parts;
		
		try 
		{
			br = new BufferedReader(new FileReader(answerFilePath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

			int timestamp = 0;
			
			while (line != null) 
			{
				parts = line.split(":");
				
				timestamp = Integer.parseInt(parts[2]);
				
				// append only if the time stamp is within the given time window
				if(timestamp > startTimeInSecond*1000 && timestamp<endTimeInSecond*1000)
				{
					sb.append(parts[1]+ " ");
				}				
				
				line = br.readLine();	        
			}
       
			answer += sb.toString();
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
			
		return answer.trim();
	}
	
	/*
	 * get the output of Graphical Model based system
	 */
	public static String getMuscleOutput(String answerFilePath) throws IOException
	{
		BufferedReader br = null;
		String answer = "";
		
		try 
		{
			br = new BufferedReader(new FileReader(answerFilePath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();
			
			while (line != null) 
			{
				sb.append(line);
				line = br.readLine();	        
			}
			answer += sb.toString();
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
		return answer.trim();
	}
	
	
	
	/**********************************************************************************/
	/*
	 * The function processes all the files in the given directory path.
	 * It reads the files, extracts the sequence of words and build an arraylist
	 * of strings, where each string is a transcription sequence of an audio file.
	 */
	public static ArrayList<Sequence> getRandomlySelectedTranscriptionSequencesByTimeInterval(String directory_path, int startTime, int endTime, int[] inclusionFlags )
	{		
		
		ArrayList<Sequence> sequences = new ArrayList<Sequence>();
		
		String rawSequence = "";
		String singleSequence = "";
		String timeSequence = "";
		//
		String[] timeStringParts;
		int flag = 0;
		//
		int i, j;
		// Read all the files from the given directory		
		ArrayList<String> filenames = getFilenamesFromDirectory(directory_path);
		
		/*
		 * 
		 */
		for(i=0;i<filenames.size();i++)
		{

			if (inclusionFlags[i]== 0)
				continue;
			
			
			// for each file, process
			try
			{
				// get the raw sequence, with all the tokens and separators
				rawSequence = getSingleTranscriptionFromFile(directory_path + filenames.get(i));
				// initialize the sequences
				singleSequence = "";
				timeSequence = "";
				String[] parts = rawSequence.split(":");
				
				flag = 0;
				
				for(j=1;j<parts.length; j+=2)
				{					

					timeStringParts = parts[j+1].split("\n");
					int time = Integer.parseInt(timeStringParts[0]);
					
					// filter out unwanted parts outside the given time window
					if(time <= startTime || time > endTime)
					{
						continue;
					}
					// filter out words at time 0
					if(time == 0)
					{
						continue;
					}					
					// avoid processing "uh" words
					if(parts[j].equals("uh"))
					{
						continue;
					}
					
					if(flag == 0)
					{
						// start of a string
						singleSequence = parts[j];	
						timeSequence = timeStringParts[0];
						
					}
					else
					{
						// middle of a string
						singleSequence += " " + parts[j];
						timeSequence += " " + timeStringParts[0];
					}
					
					// marked that passed the start of a sequence
					flag = 1;
				}				
			}
			catch(IOException e)
			{
				e.printStackTrace();				
			}
			
			
			/*
			// spell check? can be made as a user defined parameter
			System.out.println(singleSequence);
			singleSequence = SpellCheckerForCaptions.getSpellCheckedStringHunspell(singleSequence);
			System.out.println(singleSequence);
			*/
			
			// add the sequence to the list of all the transcription sequences
			sequences.add(new Sequence(singleSequence, timeSequence));			
		}
	
		return sequences;
	}
	
}
