/**
 * 
 */

/**
 * @author inaim
 * The file contiains the tunable parameters for the algorithm.
 * The default values are also set in the class.
 */
public class ConfigurableParameters {
	
	//
	public static double gapOpenPenalty = 0.125;
	public static double gapExtensionPenalty = 0.05;

	// set the chunk size parameter
	public static int chunkSize = 20;
	
	// the weight parameter for heuristic function
	public static double weight = 3.0;
	
	// beam size parameter	
	public static double beamSize = 60;
	// the entire time window to be processed by the given run
	public static int startTime = 0;
	public static int endTime = 360;
	
	
	// must be specified to a folder that contains all the paths
	public static String dataDirectoryPath = "~/MSA/data";
	
	// do you want to use Language model? or not?
	public static boolean isLM = false;

	//language model file, needs to be specified by users
	public static String lmFilePath = "./dummy.lm";
	
	public static boolean checkForRepeatedStates = false;
}