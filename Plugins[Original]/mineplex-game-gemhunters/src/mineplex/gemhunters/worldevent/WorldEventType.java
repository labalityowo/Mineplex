package mineplex.gemhunters.worldevent;

public enum WorldEventType
{

	GIANT("Zombie Awakening", WorldEventPriority.TRIGGERED),
	BLZZARD("Hurricane", WorldEventPriority.GLOBAL),
	NETHER("Dark Portal", WorldEventPriority.TRIGGERED),
	WITHER("Wither Temple", WorldEventPriority.TRIGGERED),
	GWEN_MART("Gwen-Mart Mega Sale", WorldEventPriority.GLOBAL),
	UFO("UFO", WorldEventPriority.GLOBAL),

	;

	private String _name;
	private WorldEventPriority _priority;
	private long _last;
	
	WorldEventType(String name,  WorldEventPriority priority)
	{
		_name = name;
		_priority = priority;
		_last = 0;
	}

	public String getName()
	{
		return _name;
	}

	public WorldEventPriority getPriority()
	{
		return _priority;
	}

	public long getLast()
	{
		return _last;
	}
}
