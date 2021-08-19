package nautilus.game.arcade.game.games.uhc.components;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import mineplex.core.common.util.UtilBlock;

import nautilus.game.arcade.game.games.uhc.UHC;

public class UHCBorder
{

	private static final int WARNING_TIME = 60;

	// Allow some blocks of lee-way for the Y border
	private static final int VARIATION_OF_Y_BORDER = 8;
	
	// Time in seconds for the world Y border to reach it's goal
	private static final int TIME_FOR_COMPLETION_OF_Y_BORDER = UHC.DEATHMATCH_TIME_SECONDS;
	
	private UHC _host;
	private int _startingSize;

	private WorldBorder _worldBorder;
	private double _yMin;
	private double _yMax;
	private double _yMinSpeed;
	private double _yMaxSpeed;
	private double _yGoal;

	public UHCBorder(UHC host, int startingSize)
	{
		_host = host;
		_startingSize = startingSize;
	}

	public void prepare()
	{
		World world = _host.WorldData.World;
		
		_worldBorder = world.getWorldBorder();

		_worldBorder.setCenter(0, 0);
		_worldBorder.setWarningTime(WARNING_TIME);

		setSize(_startingSize, 0);
				
		_yMin = 0;
		_yMax = world.getMaxHeight();
				
		// Get the highest non-air block at 0,0
		_yGoal = (int) (UtilBlock.getHighest(world, new Location(world, 0, 0, 0)).getLocation().getY());
		
		_yMinSpeed = (_yGoal - _yMin) / TIME_FOR_COMPLETION_OF_Y_BORDER;
		_yMaxSpeed = (_yMax - _yGoal) / TIME_FOR_COMPLETION_OF_Y_BORDER;
	}

	public void setSize(double size, long seconds)
	{
		_worldBorder.setSize(size * 2, seconds);
	}

	public void stop()
	{
		_worldBorder.setSize(_worldBorder.getSize());
		_worldBorder.setSize(5000);
	}

	public double getSize()
	{
		if (_worldBorder == null)
		{
			return _startingSize;
		}

		return _worldBorder.getSize();
	}

	public double getMaxCords()
	{
		return getSize() / 2;
	}

	public void advanceYBorder()
	{
		if (_yMax - _yGoal < VARIATION_OF_Y_BORDER)
		{
			return;
		}
		
		_yMin += _yMinSpeed;
		_yMax -= _yMaxSpeed;
	}
	
	public double getYMin()
	{
		return _yMin;
	}
	
	public double getYMax()
	{
		return _yMax;
	}

}
