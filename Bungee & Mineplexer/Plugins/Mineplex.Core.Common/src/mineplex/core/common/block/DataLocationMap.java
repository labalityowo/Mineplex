package mineplex.core.common.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;

public class DataLocationMap
{

	private final EnumMap<DyeColor, List<Location>> _goldDataMap, _ironDataMap, _spongeDataMap;

	public DataLocationMap()
	{
		_goldDataMap = new EnumMap<>(DyeColor.class);
		_ironDataMap = new EnumMap<>(DyeColor.class);
		_spongeDataMap = new EnumMap<>(DyeColor.class);
	}

	public List<Location> getGoldLocations(DyeColor color)
	{
		List<Location> list = _goldDataMap.get(color);
		return list == null ? Collections.emptyList() : list;
	}

	public void addGoldLocation(DyeColor color, Location location)
	{
		if (_goldDataMap.containsKey(color))
		{
			_goldDataMap.get(color).add(location);
		}
		else
		{
			List<Location> list = new ArrayList<>();
			list.add(location);
			_goldDataMap.put(color, list);
		}
	}

	public List<Location> getIronLocations(DyeColor color)
	{
		List<Location> list = _ironDataMap.get(color);
		return list == null ? Collections.emptyList() : list;
	}

	public void addIronLocation(DyeColor color, Location location)
	{
		if (_ironDataMap.containsKey(color))
		{
			_ironDataMap.get(color).add(location);
		}
		else
		{
			List<Location> list = new ArrayList<>();
			list.add(location);
			_ironDataMap.put(color, list);
		}
	}

	public void addSpongeLocation(DyeColor color, Location location)
	{
		if (_spongeDataMap.containsKey(color))
		{
			_spongeDataMap.get(color).add(location);
		}
		else
		{
			List<Location> list = new ArrayList<>();
			list.add(location);
			_spongeDataMap.put(color, list);
		}
	}

	public List<Location> getSpongeLocations(DyeColor color)
	{
		List<Location> list = _spongeDataMap.get(color);
		return list == null ? Collections.emptyList() : list;
	}
}
