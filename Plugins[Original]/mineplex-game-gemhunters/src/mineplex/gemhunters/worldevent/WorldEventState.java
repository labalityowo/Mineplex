package mineplex.gemhunters.worldevent;

public enum WorldEventState
{

	WARMUP,
	LIVE,
	COMPLETE;
	
	public String getName()
	{
		return name().charAt(0) + name().toLowerCase().substring(1);
	}
	
}
