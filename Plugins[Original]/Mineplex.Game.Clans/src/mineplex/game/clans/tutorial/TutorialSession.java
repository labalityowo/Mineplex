package mineplex.game.clans.tutorial;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import mineplex.core.hologram.Hologram;

public class TutorialSession
{
	private final long _startTime;
	private int _objectiveIndex;
	private TutorialRegion _region;
	private List<Hologram> _hologramList = new ArrayList<>();
	private Location _homeLocation;
	private int _colorTick;
	private int _textSeconds;
	private Location _mapTargetLocation;
	private Hologram _spawnHologram;
	private boolean _removedHologram;

	public TutorialSession()
	{
		_startTime = System.currentTimeMillis();
	}

	public List<Hologram> getHolograms()
	{
		return _hologramList;
	}

	public int getObjectiveIndex()
	{
		return _objectiveIndex;
	}

	public void setObjectiveIndex(int objectiveIndex)
	{
		_objectiveIndex = objectiveIndex;
	}

	public TutorialRegion getRegion()
	{
		return _region;
	}

	public void setRegion(TutorialRegion region)
	{
		_region = region;
	}

	public Location getHomeLocation()
	{
		return _homeLocation;
	}

	public void setHomeLocation(Location homeLocation)
	{
		_homeLocation = homeLocation;
	}

	public int incrementAndGetColorTick()
	{
		return ++_colorTick;
	}

	public void setTextSeconds(int seconds)
	{
		_textSeconds = seconds;
	}

	public int incrementAndGetTextSeconds()
	{
		_textSeconds++;
		return _textSeconds;
	}

	public void setMapTargetLocation(Location location)
	{
		_mapTargetLocation = location;
	}

	public Location getMapTargetLocation()
	{
		return _mapTargetLocation;
	}

	public long getStartTime()
	{
		return _startTime;
	}

	public long getElapsedTime()
	{
		return System.currentTimeMillis() - _startTime;
	}

	public boolean isRemovedHologram()
	{
		return _removedHologram;
	}

	public void setRemovedHologram(boolean removedHologram)
	{
		_removedHologram = removedHologram;
	}

	public void setSpawnHologram(Hologram spawnHologram)
	{
		_spawnHologram = spawnHologram;
	}

	public Hologram getSpawnHologram()
	{
		return _spawnHologram;
	}
}
