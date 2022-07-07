package mineplex.core.mission;

import org.bukkit.event.Listener;

public class MissionTracker implements Listener
{

	protected final MissionManager _manager;
	protected final MissionTrackerType _trackerType;

	public MissionTracker(MissionManager manager, MissionTrackerType trackerType)
	{
		_manager = manager;
		_trackerType = trackerType;
	}

	public void cleanup()
	{}
}
