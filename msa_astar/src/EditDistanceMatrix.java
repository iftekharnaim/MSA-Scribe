/**
 * 
 */

/**
 * @author inaim
 *
 */
public class EditDistanceMatrix {
	public double E[][];
	public int n, m;
	
	public EditDistanceMatrix(int nn, int mm) {
		// initialize the array
		this.E = new double[n][m];
	}
	
	public EditDistanceMatrix(double[][] Ein)
	{
		this.E = Ein;
	}

}
