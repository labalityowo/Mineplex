package mineplex.game.nano.game.games.minekart;

import java.util.Arrays;

import org.bukkit.Location;

import mineplex.core.common.util.UtilAlg;

public class KartCheckpoint
{

	private final Location _cornerA, _cornerB;
	private final int _index;
	private final boolean _key;

	KartCheckpoint(Location cornerA, Location cornerB, int index, boolean key)
	{
		_cornerA = cornerA;
		_cornerB = cornerB;
		_index = index;
		_key = key;
	}

	public boolean isInBox(Location location)
	{
		return UtilAlg.inBoundingBox(location, _cornerA, _cornerB);
	}

	public Location getCenter()
	{
		return UtilAlg.getAverageLocation(Arrays.asList(_cornerA, _cornerB));
	}

	public int getIndex()
	{
		return _index;
	}

	public boolean isKey()
	{
		return _key;
	}

}
