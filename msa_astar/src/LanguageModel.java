import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * 
 */

/**
 * The class is designed to incorporate different language models in to the MSA system. For the time being, I am writing simple class.
 * I would like to modify it using class inheritance and abstract class architecture for multiple Language Models (Good-Turing, KN, Neural Net, etc.)
 * 
 * @author inaim
 *
 */

public class LanguageModel 
{
	// need some hashtable to store all the necessary LM probabilities
	private HashMap<String, Double> logprob_table;
	private HashMap<String, Double> backoff_table;
	public double LOGZERO = -99.0;
	public int Ngram = 3;
	
	/*
	 * The Good-Turing LM class constructor
	 */
	public LanguageModel(String filePath)
	{
		// load the LM using the filePath
		this.logprob_table = new HashMap<String,Double>();
		this.backoff_table = new HashMap<String,Double>();
		
		// start reading the file
		readLanguageModelFromFile(filePath);
	}

	/*
	 * get the trigram probability using Good-Turing model
	 */
	public double getGoodTuringLMProbabilityTrigram(String trigram)
	{
		Double logprob, backoffprob;
		String[] trigramParts;
		
		logprob = this.logprob_table.get(trigram);
		
		if(logprob != null)
		{
			return Math.exp(logprob.doubleValue());
		}
		else
		{
			trigramParts = trigram.split(" ");
			logprob = this.logprob_table.get(trigramParts[0] + " " + trigramParts[1]);

			if(trigramParts.length >= 3)
			{
				backoffprob = this.backoff_table.get(trigramParts[1] + " " + trigramParts[2]);
			}
			else
			{
				backoffprob = null;
			}

			if(logprob == null || backoffprob == null)
			{
				return Math.exp(this.LOGZERO);
			}
			else
			{
				return Math.exp(logprob.doubleValue() + backoffprob.doubleValue());
			}
		}
		
	}

	/*
	 * get the trigram probability using Good-Turing model
	 */
	public double getGoodTuringLMProbabilityBigram(String bigram)
	{
		Double logprob, backoffprob;
		String[] bigramParts;
		
		logprob = this.logprob_table.get(bigram);
		
		if(logprob != null)
		{
			return Math.exp(logprob.doubleValue());
		}
		else
		{
			bigramParts = bigram.split(" ");
			logprob = this.logprob_table.get(bigramParts[0]);

			if(bigramParts.length >= 2)
			{
				backoffprob = this.backoff_table.get(bigramParts[1]);
			}
			else
			{
				backoffprob = null;
			}

			if(logprob == null || backoffprob == null)
			{
				return Math.exp(this.LOGZERO);
			}
			else
			{
				return Math.exp(logprob.doubleValue() + backoffprob.doubleValue());
			}
		}
		
	}
	
	
	/*
	 * get the trigram probability using Good-Turing model
	 */
	public double getGoodTuringLMProbabilityUnigram(String unigram)
	{
		Double logprob;
		
		logprob = this.logprob_table.get(unigram);
		
		if(logprob != null)
		{
			return Math.exp(logprob.doubleValue());
		}
		else
		{
			return Math.exp(this.LOGZERO);
		}
		
	}
	
	/*
	 * 
	 */
	private void readLanguageModelFromFile(String filepath)
	{
		BufferedReader br = null;
		String sequenceString = "";
		
		int linecount = 0;
		// open and read the file
		try 
		{
			br = new BufferedReader(new FileReader(filepath));

			StringBuilder sb = new StringBuilder();	       
			String line = br.readLine();

			while (line != null) 
			{
				linecount++;
				
				String[] parts = line.split("\t"); 
				
				if(parts.length <2)
				{
					line = br.readLine();
					continue;
				}
				else
				{
					// insert entries to the logprob and backoff table 
					line = br.readLine();	 
					this.logprob_table.put(parts[1], Double.parseDouble(parts[0]));
					
					if(parts.length > 2)
					{
						this.backoff_table.put(parts[1], Double.parseDouble(parts[2]));
					}

				}	        
			}
		} 
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// close the file
		try
		{
			if(br!=null)
			{
				br.close();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}
}
