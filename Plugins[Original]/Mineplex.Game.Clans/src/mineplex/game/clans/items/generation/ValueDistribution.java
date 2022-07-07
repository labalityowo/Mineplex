package mineplex.game.clans.items.generation;

import java.util.Random;

/**
 * Handles the random generation of attribute values in specified value range
 * and probability distribution.
 * @author MrTwiggy
 *
 */
public class ValueDistribution
{
	private static Random random = new Random();	// Used for RNG of value generation
	
	private double _min;			// Minimum value range available for distribution
	private double _max;			// Maximum value range available for distribution
	private double _lambdaScaler;	// Scales exponential probability distribution to skew range values
	
	/**
	 * Class constructor for distribution of range [min, max]
	 * @param min - the minimum value for generation range
	 * @param max - the maximum value for generation range
	 */
	public ValueDistribution(double min, double max)
	{
		_min = min;
		_max = max;
	}
	
	/**
	 * @return randomly generated value conforming to the range and value distribution.
	 */
	public double generateValue()
	{
		double roll = random.nextDouble() * random.nextDouble();
		
		double delta = getRange() * roll;
		return _min + delta;
	}
	
	/**
	 * @return randomly generated distribution value, rounding to nearest integer.
	 */
	public int generateIntValue()
	{
		return (int) Math.round(generateValue());
	}
	
	/**
	 * @return the value range associated with this distribution.
	 */
	public double getRange()
	{
		return _max - _min;
	}
}
