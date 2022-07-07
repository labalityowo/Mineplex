package nautilus.game.arcade.game.games.minecraftleague.data;

import org.bukkit.Location;

public class MapZone
{
	private Location _loc;
	private int[] _rgb;
	private boolean _valid;
	
	public MapZone(Location center, int[] rgb)
	{
		_loc = center;
		_rgb = rgb;
		_valid = true;
	}
	
	public boolean isValid()
	{
		return _valid;
	}
	
	public boolean isInRadius(int x, int z)
	{
		int diffX = Math.max(x, _loc.getBlockX()) - Math.min(x, _loc.getBlockX());
		int diffZ = Math.max(z, _loc.getBlockZ()) - Math.min(z, _loc.getBlockZ());
		
		if (diffX <= 5)
			if (diffZ <= 5)
				return true;
		
		return false;
	}
	
	/*public boolean isBase(int x, int z)
	{
		if (_loc.getBlockX() == x)
			if (_loc.getBlockZ() == z)
				return true;
		
		return false;
	}*/
	
	/*public Location getBase()
	{
		return _loc;
	}*/
	
	public int getRed()
	{
		return _rgb[0];
	}
	
	public int getGreen()
	{
		return _rgb[1];
	}
	
	public int getBlue()
	{
		return _rgb[2];
	}

	public void setValid(boolean valid)
	{
		_valid = valid;
	}
	
	public void setCenter(Location center)
	{
		_loc = center;
	}
	
	/*public void update()
	{
		int found = 0;
		for (Block block : UtilBlock.getInSquare(_loc.getBlock(), 10))
		{
			if (block.getType() == _ore)
				found++;
		}
		
		_valid = found >= 1;
	}*/
}
