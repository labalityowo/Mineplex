package mineplex.core.common.shape;

import java.awt.Color;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;

/**
 * Some simple wing shapes implementing {@link CosmeticShape} storing additional particle information
 */

public class ShapeWings extends ShapeGrid implements CosmeticShape
{
	public static final String[] ANGEL_WING_PATTERN = new String[]
			{
					"000$$0000000000$$000",
					"00$##$00000000$##$00",
					"0$####$000000$####$0",
					"$#####$000000$#####$",
					"$#####$000000$#####$",
					"$######$0000$######$",
					"$######$0000$######$",
					"$######$0000$######$",
					"$######$$$$$$######$",
					"$##################$",
					"0$################$0",
					"00$####$$$$$$####$00",
					"00$####$0000$####$00",
					"000$##$000000$##$000",
					"000$##$000000$##$000",
					"000$#$00000000$#$000",
					"00$#$0000000000$#$00",
					"00$#$0000000000$#$00",
					"000$000000000000$000",
			};

	public static final String[] SMALL_ANGEL_WING_PATTERN = new String[]
			{
					"00$0000$00",
					"0$#$00$#$0",
					"$##$00$##$",
					"$###$$###$",
					"$########$",
					"$########$",
					"$##$$$$##$",
					"0$#$00$#$0",
					"00$0000$00"
			};
	
	public static final String[] BUTTERFLY_WING_PATTERN = new String[]
			{
					"0$$000000000000000$$0",
					"$##$0000000000000$##$",
					"0$##$00000000000$##$0",
					"00$##$000000000$##$00",
					"00$###$0000000$###$00",
					"000$###$$000$$###$000",
					"0000$####$$$####$0000",
					"0000$###########$0000",
					"00000$#########$00000",
					"00000$#########$00000",
					"00000$###$$$###$00000",
					"0000$###$000$###$0000",
					"0000$##$00000$##$0000",
					"00000$$0000000$$00000"
			};

	public static final String[] BEE_WING_PATTERN = new String[]
			{
					"00$$$000$$$00",
					"0$###$0$###$0",
					"$$$$$$$$$$$$$",
					"$#####$#####$",
					"$$$$$$$$$$$$$",
					"0$#########$0",
					"00$$$$$$$$$00",
					"0$#########$0",
					"$$$$$$$$$$$$$",
					"$#####$#####$",
					"$$$$$$$$$$$$$",
					"0$###$0$###$0",
					"00$$$000$$$00"
			};

	public static final String[] SMALL_BUTTERFLY_WING_PATTERN = new String[]
			{
					"0$$00000000$$0",
					"$##$000000$##$",
					"0$##$0000$##$0",
					"00$##$$$$##$00",
					"000$######$000",
					"000$######$000",
					"00$###$$###$00",
					"000$#$00$#$000",
					"0000$0000$0000"
			};

	public static final String[] HEART_WING_PATTERN = new String[]
			{
					"00$00000000000000000$00",
					"0$%$000000000000000$%$0",
					"$%%%$$00$$000$$00$$%%%$",
					"$%%%%%$$##$0$##$$%%%%%$",
					"$%%%%%$####$####$%%%%%$",
					"0$%%%%$#########$%%%%$0",
					"00$%%%$#########$%%%$00",
					"000$%%$$#######$$%%$000",
					"0000$$00$#####$00$$0000",
					"000000000$###$000000000",
					"0000000000$#$0000000000",
					"00000000000$00000000000"
			};

	public static final String[] SMALL_HEART_WING_PATTERN = new String[]
			{
					"0$000000000$0",
					"$%$0$$0$$0$%$",
					"$%%$##$##$%%$",
					"0$%$00000$%$0",
					"00$0$###$0$00",
					"00000$#$00000",
					"000000$000000"
			};

	public static final String[] FOUR_LEAF_CLOVER = new String[]
			{
					"$$$$$$$$###$$$$$$$",
					"$$$$$$##***#$$$$$$",
					"$$$$##****%#$$$$$$",
					"$$$#******%#$###$$",
					"$$$#***%#%%##***#$",
					"$$$$#**%#%#*****#$",
					"$$$####*%%#*%%**#$",
					"$##***#*%#**##**##",
					"#***%%*###******%#",
					"#**%##*%###%%%%%%#",
					"#**#**%%#**######$",
					"$#****%#*****#$$$$",
					"$$#*%%%#******#$$$",
					"$$#####**%#****#$$",
					"$$$$$##**%#***%#$$",
					"$$$$$###****%%%#$$",
					"$$$$##$#%%%%%##$$$",
					"$$$##$$$#####$$$$$",
					"$###$$$$$$$$$$$$$$",
					"$##$$$$$$$$$$$$$$$"
			};

	public static final String[] KINGS_CAPE = new String[]
			{
					"00000$00000",
					"0000$#$0000",
					"000$###$000",
					"000$###$000",
					"00$#####$00",
					"00$#####$00",
					"00$#####$00",
					"00$#####$00",
					"0$#######$0",
					"$#########$"
			};

	public static final String[] MAPLE_LEAF = new String[]
			{
					"000000000000000000000000000$000000000000000000000000000",
					"00000000000000000000000000$$$00000000000000000000000000",
					"0000000000000000000000000$$#$$0000000000000000000000000",
					"000000000000000000000000$$###$$000000000000000000000000",
					"00000000000000000000000$$#####$$00000000000000000000000",
					"0000000000000000$$$000$$#######$$000$$$0000000000000000",
					"0000000000000000$#$$$$$#########$$$$$#$0000000000000000",
					"0000000000000000$$###################$$0000000000000000",
					"00000000000000000$###################$00000000000000000",
					"00000000000$$0000$$#################$$0000$$00000000000",
					"0$$$000000$$$$$000$#################$000$$$$$000000$$$0",
					"00$$$$$$$$$###$$$0$$###############$$0$$$###$$$$$$$$$00",
					"00$$############$$$$###############$$$$############$$00",
					"000$$#############$$###############$$#############$$000",
					"0000$$###########################################$$0000",
					"00$$$#############################################$$$00",
					"$$$#################################################$$$",
					"00$$$$###########################################$$$$00",
					"00000$$$#######################################$$$00000",
					"00000000$$$$###############################$$$$00000000",
					"00000000000$$$###########################$$$00000000000",
					"0000000000000$$#########################$$0000000000000",
					"0000000000000$$#########################$$0000000000000",
					"0000000000000$##$$$$$$$$$$$#$$$$$$$$$$$##$0000000000000",
					"000000000000$$$$$000000000$#$000000000$$$$$000000000000",
					"00000000000000000000000000$#$00000000000000000000000000",
					"00000000000000000000000000$#$00000000000000000000000000",
					"00000000000000000000000000$#$00000000000000000000000000",
					"00000000000000000000000000$#$00000000000000000000000000",
					"00000000000000000000000000$$$00000000000000000000000000"
			};
	
	/**
	 * Default rotation to give the wings a little tilt when displayed on players for instance
	 */
	public static double DEFAULT_ROTATION = Math.PI/0.05;

	/**
	 * Doesn't have any rotation, so it doesn't go inside the player
	 */
	public static double NO_ROTATION = 0;

	private String _particle;
	private Vector _offsetData;
	private float _speed;
	private int _count;
	
	/**
	 * A simple non-edge wing shape using the default butterfly pattern {@link ShapeWings#BUTTERFLY_WING_PATTERN} 
	 * and x-rotation {@link #DEFAULT_ROTATION}. It also uses default redstone dust particle with offset of 0, speed of 0 and count 1
	 */
	public ShapeWings()
	{
		this(ParticleType.RED_DUST.particleName);
	}
	
	/**
	 * A simple non-edge wing shape using the default butterfly pattern {@link ShapeWings#BUTTERFLY_WING_PATTERN} 
	 * and x-rotation {@link #DEFAULT_ROTATION}
	 * @param particle The particle to display at each point in the wing shape. Using offset of 0, speed of 0 and count 1
	 */
	public ShapeWings(String particle)
	{
		this(particle, null, 0, 1);
	}
	
	/**
	 * A simple non-edge wing shape using the default butterfly pattern {@link ShapeWings#BUTTERFLY_WING_PATTERN} 
	 * and x-rotation {@link #DEFAULT_ROTATION}
	 * @param particle The particle to display at each point in the wing shape
	 * @param offsetData Particle data
	 * @param speed Particle speed
	 * @param count Particle count
	 */
	public ShapeWings(String particle, Vector offsetData, float speed, int count)
	{
		this(particle, offsetData, speed, count, false);
	}
	
	/**
	 * A simple wing shape using the default butterfly pattern {@link ShapeWings#BUTTERFLY_WING_PATTERN} 
	 * and x-rotation {@link #DEFAULT_ROTATION}
	 * @param particle The particle to display at each point in the wing shape
	 * @param offsetData Particle data
	 * @param speed Particle speed
	 * @param count Particle count
	 * @param edge If this is the edge of the wings or not
	 */
	public ShapeWings(String particle, Vector offsetData, float speed, int count, boolean edge)
	{
		this(particle, offsetData, speed, count, edge, DEFAULT_ROTATION);
	}
	
	/**
	 * A simple wing shape using the default butterfly pattern {@link ShapeWings#BUTTERFLY_WING_PATTERN}
	 * @param particle The particle to display at each point in the wing shape
	 * @param offsetData Particle data
	 * @param speed Particle speed
	 * @param count Particle count
	 * @param edge If this is the edge of the wings or not
	 * @param xRotation Rotation on the x axis
	 */
	public ShapeWings(String particle, Vector offsetData, float speed, int count, boolean edge, double xRotation)
	{
		this(particle, offsetData, speed, count, edge, xRotation, BUTTERFLY_WING_PATTERN);
	}

	/**
	 * A simple wing shape
	 * @param particle The particle to display at each point in the wing shape
	 * @param offsetData Particle data
	 * @param speed Particle speed
	 * @param count Particle count
	 * @param edge If this is the edge of the wings or not
	 * @param xRotation Rotation on the x axis
	 * @param pattern Pattern to use as wing shape
	 */
	public ShapeWings(String particle, Vector offsetData, float speed, int count, boolean edge, double xRotation, String... pattern)
	{
		super(0.15, edge? '$' : '#',
				pattern
				);
		
		_particle = particle;
		_offsetData = offsetData;
		_speed = speed;
		_count = count;
		
		rotateOnXAxis(xRotation);
	}

	public ShapeWings(String particle, Vector offsetData, float speed, int count, char c, double xRotation, String... pattern)
	{
		super(0.15, c, pattern);
		_particle = particle;
		_offsetData = offsetData;
		_speed = speed;
		_count = count;
		rotateOnXAxis(xRotation);
	}

	/**
	 * Displays the wing
	 * @param location The location to display the visual at
	 */
	@Override
	public void display(Location location)
	{
		Shape clone = clone();
		clone.rotateOnYAxis(Math.toRadians(location.getYaw()));
		for (Vector v : clone.getPoints())
		{
			Location particleLocation = location.clone().add(v);
			displayParticle(particleLocation);
		}
	}

	/**
	 * Displays the wing for the given player
	 * @param location The location to display the visual at
	 * @param player The player
	 */
	public void display(Location location, Player player)
	{
		Shape clone = clone();
		clone.rotateOnYAxis(Math.toRadians(location.getYaw()));
		for (Vector v : clone.getPoints())
		{
			Location particleLocation = location.clone().add(v);
			displayParticle(particleLocation, player);
		}
	}

	/**
	 * Displays the colored wing
	 * @param location The location to display the visual at
	 * @param color The color of the particles
	 */
	public void displayColored(Location location, Color color)
	{
		Shape clone = clone();
		clone.rotateOnYAxis(Math.toRadians(location.getYaw()));
		for (Vector v : clone.getPoints())
		{
			Location particleLocation = location.clone().add(v);
			displayColoredParticle(particleLocation, color);
		}
	}

	/**
	 * Displays the colored wing for the given player
	 * @param location The location to display the visual at
	 * @param color The color of the particles
	 * @param player The player
	 */
	public void displayColored(Location location, Color color, Player player)
	{
		Shape clone = clone();
		clone.rotateOnYAxis(Math.toRadians(location.getYaw()));
		for (Vector v : clone.getPoints())
		{
			Location particleLocation = location.clone().add(v);
			displayColoredParticle(particleLocation, color, player);
		}
	}

	/**
	 * Display a single particle of the type provided to this shape at the given location.
	 * @param location The location
	 */
	public void displayParticle(Location location)
	{
		UtilParticle.PlayParticleToAll(_particle, location, _offsetData, _speed, _count, ViewDist.NORMAL);
	}

	/**
	 * Display a single particle of the type provided to this shape at the given location for the given player
	 * @param location The location
	 * @param player The player
	 */
	public void displayParticle(Location location, Player player)
	{
		UtilParticle.PlayParticle(_particle, location, (float) _offsetData.getX(), (float) _offsetData.getY(), (float) _offsetData.getZ(), _speed, _count, ViewDist.NORMAL, player);
	}

	/**
	 * Display a single colored particle of the type provided to this shape at the given location.
	 * @param location The location
	 * @param color The color
	 */
	public void displayColoredParticle(Location location, Color color)
	{
		ColoredParticle coloredParticle = new ColoredParticle(ParticleType.RED_DUST, new DustSpellColor(color), location);
		coloredParticle.display(ViewDist.NORMAL);
	}

	/**
	 * Displays a single colored particle of the type provided to this shape at the given location for the given player
	 * @param location The location
	 * @param color The color
	 * @param player The player
	 */
	public void displayColoredParticle(Location location, Color color, Player player)
	{
		ColoredParticle coloredParticle = new ColoredParticle(ParticleType.RED_DUST, new DustSpellColor(color), location);
		coloredParticle.display(ViewDist.NORMAL, player);
	}

}
