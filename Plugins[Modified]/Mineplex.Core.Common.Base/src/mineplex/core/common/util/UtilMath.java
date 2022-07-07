package mineplex.core.common.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class UtilMath
{
	public static final double TAU = Math.PI * 2D;

	public static double trim(int degree, double d)
	{
		StringBuilder format = new StringBuilder("#.#");
		
		for (int i = 1; i < degree; i++)
			format.append("#");
			
		DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
		DecimalFormat twoDForm = new DecimalFormat(format.toString(), symb);
		return Double.valueOf(twoDForm.format(d));
	}
	
	public static Random random = new Random();
	
	public static int r(int i)
	{
		return random.nextInt(i);
	}
	
	public static int rRange(int min, int max)
	{
		return min + r(1 + max - min);
	}
	
	public static double offset2d(Entity a, Entity b)
	{
		return offset2d(a.getLocation().toVector(), b.getLocation().toVector());
	}
	
	public static double offset2d(Location a, Location b)
	{
		return offset2d(a.toVector(), b.toVector());
	}
	
	public static double offset2d(Vector a, Vector b)
	{
		a.setY(0);
		b.setY(0);
		return a.subtract(b).length();
	}
	
	public static double offset2dSquared(Entity a, Entity b)
	{
		return offset2dSquared(a.getLocation().toVector(), b.getLocation().toVector());
	}
	
	public static double offset2dSquared(Location a, Location b)
	{
		return offset2dSquared(a.toVector(), b.toVector());
	}
	
	public static double offset2dSquared(Vector a, Vector b)
	{
		a.setY(0);
		b.setY(0);
		return a.subtract(b).lengthSquared();
	}
	
	public static double offset(Entity a, Entity b)
	{
		return offset(a.getLocation().toVector(), b.getLocation().toVector());
	}
	
	public static double offset(Location a, Location b)
	{
		return offset(a.toVector(), b.toVector());
	}
	
	public static double offset(Vector a, Vector b)
	{
		return a.clone().subtract(b).length();
	}
	
	public static double offsetSquared(Entity a, Entity b)
	{
		return offsetSquared(a.getLocation(), b.getLocation());
	}
	
	public static double offsetSquared(Location a, Location b)
	{
		return offsetSquared(a.toVector(), b.toVector());
	}
	
	public static double offsetSquared(Vector a, Vector b)
	{
		return a.distanceSquared(b);
	}
	
	public static double rr(double d, boolean bidirectional)
	{
		if (bidirectional) return Math.random() * (2 * d) - d;
		
		return Math.random() * d;
	}
	
	public static <T> T randomElement(T[] array)
	{
		if (array.length == 0) return null;
		return array[random.nextInt(array.length)];
	}
	
	public static <T> T randomElement(List<T> list)
	{
		if (list.isEmpty()) return null;
		return list.get(random.nextInt(list.size()));
	}
	
	public static double clamp(double num, double min, double max)
	{
		return num < min ? min : (num > max ? max : num);
	}
	
	public static float clamp(float num, float min, float max)
	{
		return num < min ? min : (num > max ? max : num);
	}
	
	public static long clamp(long num, long min, long max)
	{
		return num < min ? min : (num > max ? max : num);
	}
	
	public static int clamp(int num, int min, int max)
	{
		return num < min ? min : (num > max ? max : num);
	}

	public static List<Integer> digits(int i) {
		List<Integer> digits = new ArrayList<Integer>();
		while(i > 0) {
			digits.add(i % 10);
			i /= 10;
		}
		return digits;
	}

	public static double random(double min, double max)
	{
		min = Math.abs(min);

		int rand = -random.nextInt((int)(min * 100));

		rand += random.nextInt((int)(max * 100));

		return ((double) rand) / 100.d;
	}

	public static <T> T getLast(List<T> list)
	{
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}
	
	public static <T> T getFirst(List<T> list)
	{
		return list.isEmpty() ? null : list.get(0);
	}
	
	public static <T> T getLast(NautArrayList<T> list)
	{
		return list.isEmpty() ? null : list.get(list.size() - 1);
	}
	
	public static <T> T getFirst(NautArrayList<T> list)
	{
		return list.isEmpty() ? null : list.get(0);
	}

	public static <N extends Number> N closest(List<N> values, N value)
	{
		int closestIndex = -1;
		
		int index = 0;
		for (N number : values)
		{
			if (closestIndex == -1 || (Math.abs(number.doubleValue() - value.doubleValue()) < Math.abs(values.get(closestIndex).doubleValue() - value.doubleValue())))
			{
				closestIndex = index;
			}
			
			index++;
		}
		
		return values.get(closestIndex);
	}

	public static boolean isOdd(int size)
	{
		return !isEven(size);
	}
	
	public static boolean isEven(int size)
	{
		return size % 2 == 0;
 	}

	public static byte[] getBits(int value)
	{
		byte[] bits = new byte[32];

		String bit = Long.toBinaryString(value);
		
		while (bit.length() < 32)
		{
			bit = "0" + bit;
		}
		
		int index = 0;
		for (char c : bit.toCharArray())
		{
			bits[index] = (byte) (c == '1' ? '1' : '0');
			
			index++;
		}
		
		return bits;
	}
	
	public static byte[] getBits(long value)
	{
		byte[] bits = new byte[64];
		
		String bit = Long.toBinaryString(value);
		
		while (bit.length() < 64)
		{
			bit = "0" + bit;
		}
		
		int index = 0;
		for (char c : bit.toCharArray())
		{
			bits[index] = (byte) (c == '1' ? '1' : '0');
			
			index++;
		}
		
		return bits;
	}
	
	public static byte[] getBits(byte value)
	{
		byte[] bits = new byte[8];
		
		String bit = Long.toBinaryString(value);
		
		while (bit.length() < 8)
		{
			bit = "0" + bit;
		}
		
		int index = 0;
		for (char c : bit.toCharArray())
		{
			bits[index] = (byte) (c == '1' ? '1' : '0');
			
			index++;
		}
		
		return bits;
	}
	
	public static byte[] getBits(short value)
	{
		byte[] bits = new byte[16];
		
		String bit = Long.toBinaryString(value);
		
		while (bit.length() < 16)
		{
			bit = "0" + bit;
		}
		
		int index = 0;
		for (char c : bit.toCharArray())
		{
			bits[index] = (byte) (c == '1' ? '1' : '0');
			
			index++;
		}
		
		return bits;
	}

	public static double getDecimalPoints(double n)
	{
		return n - ((int) ((int) n));
	}
	
	public static int getMax(int... ints)
	{
		if (ints.length < 1)
		{
			return -1;
		}
		int max = ints[0];
		
		for (int i = 1; i < ints.length; i++)
		{
			max = Math.max(max, ints[i]);
		}
		
		return max;
	}
	
	public static int getMin(int... ints)
	{
		if (ints.length < 1)
		{
			return -1;
		}
		int min = ints[0];
		
		for (int i = 1; i < ints.length; i++)
		{
			min = Math.min(min, ints[i]);
		}
		
		return min;
	}

	/**
	 * Creates an array of points, arranged in a circle normal to a vector.
	 *
	 * @param center The center of the circle.
	 * @param normal A vector normal to the circle.
	 * @param radius The radius of the circle.
	 * @param points How many points to make up the circle.
	 *
	 * @return An array of points of the form <code>double[point #][x=0, y=1, z=3]</code>.
	 */
	public static double[][] normalCircle(Location center, Vector normal, double radius, int points)
	{
		return normalCircle(center.toVector(), normal, radius, points);
	}

	/**
	 * Creates an array of points, arranged in a circle normal to a vector.
	 *
	 * @param center The center of the circle.
	 * @param normal A vector normal to the circle.
	 * @param radius The radius of the circle.
	 * @param points How many points to make up the circle.
	 *
	 * @return An array of points of the form <code>double[point #][x=0, y=1, z=3]</code>.
	 */
	public static double[][] normalCircle(Vector center, Vector normal, double radius, int points)
	{
		Vector n = normal.clone().normalize();
		Vector a = n.clone().add(new Vector(1, 1, 1)).crossProduct(n).normalize();
		Vector b = n.getCrossProduct(a).normalize();

		double[][] data = new double[points][3];

		double interval = TAU / points;
		double theta = 0;

		for (int i = 0; i < points; i++)
		{
			data[i][0] = center.getX() + (radius * ((Math.cos(theta) * a.getX()) + (Math.sin(theta) * b.getX())));
			data[i][1] = center.getY() + (radius * ((Math.cos(theta) * a.getY()) + (Math.sin(theta) * b.getY())));
			data[i][2] = center.getZ() + (radius * ((Math.cos(theta) * a.getZ()) + (Math.sin(theta) * b.getZ())));
			theta += interval;
		}

		return data;
	}

	/**
	 * Slightly randomize a location with a standard deviation of one.
	 *
	 * @param location The location to randomize.
	 *
	 * @return The original location, now gaussian-randomized.
	 */
	public static Location gauss(Location location)
	{
		return gauss(location, 1, 1, 1);
	}

	/**
	 * Slightly randomize a vector with a standard deviation of one.
	 *
	 * @param vector The location to randomize.
	 *
	 * @return The randomized vector, now gaussian-randomized.
	 */
	public static Vector gauss(Vector vector)
	{
		return gauss(vector, 1, 1, 1);
	}

	/**
	 * Slightly randomize a location with a standard deviation of one. <br>
	 *
	 * <b>This method only accepts positive values for all of its arguments.</b> <br>
	 *
	 * A good parameter set for small offsets is (loc, 10, 10, 10).
	 *
	 * @param location The location to randomize.
	 * @param x A granularity control for the x-axis, higher numbers = less randomness
	 * @param y A granularity control for the y-axis, higher numbers = less randomness
	 * @param z A granularity control for the z-axis, higher numbers = less randomness
	 *
	 * @return The original location, now gaussian-randomized
	 */
	public static Location gauss(Location location, double x, double y, double z)
	{
		return location.clone().add(
				x <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / x),
				y <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / y),
				z <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / z));
	}

	/**
	 * Slightly randomize a vector with a standard deviation of one. <br>
	 *
	 * <b>This method only accepts positive values for all of its arguments.</b> <br>
	 *
	 * A good parameter set for small offsets is (loc, 10, 10, 10).
	 *
	 * @param vector The location to randomize.
	 * @param x A granularity control for the x-axis, higher numbers = less randomness
	 * @param y A granularity control for the y-axis, higher numbers = less randomness
	 * @param z A granularity control for the z-axis, higher numbers = less randomness
	 *
	 * @return The randomized vector, now gaussian-randomized
	 */
	public static Vector gauss(Vector vector, double x, double y, double z)
	{
		return vector.clone().add(new Vector(
				x <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / x),
				y <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / y),
				z <= 0 ? 0 : (ThreadLocalRandom.current().nextGaussian() / z)));
	}
}
