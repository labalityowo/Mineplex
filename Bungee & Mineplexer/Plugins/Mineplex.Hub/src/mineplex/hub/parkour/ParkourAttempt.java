package mineplex.hub.parkour;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

public class ParkourAttempt
{

	private final ParkourData _data;
	private final long _start;
	private long _end;
	private final Set<Location> _checkpoints;

	public ParkourAttempt(ParkourData data)
	{
		_data = data;
		_start = System.currentTimeMillis();
		_checkpoints = new HashSet<>();
	}

	public ParkourData getData()
	{
		return _data;
	}

	public long getStart()
	{
		return _start;
	}

	public void setEnd()
	{
		_end = System.currentTimeMillis();
	}

	public long getDuration()
	{
		return _end - _start;
	}

	public Set<Location> getCheckpoints()
	{
		return _checkpoints;
	}
}
