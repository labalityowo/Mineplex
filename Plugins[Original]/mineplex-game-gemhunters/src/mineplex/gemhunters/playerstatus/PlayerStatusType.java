package mineplex.gemhunters.playerstatus;

public enum PlayerStatusType
{

	DANGER,
	SAFE,
	COLD,
	WARM,
	COMBAT;
	
	public String getName()
	{
		return name().charAt(0) + name().substring(1).toLowerCase();
	}
	
	public boolean hasPriority(PlayerStatusType other)
	{
		return ordinal() > other.ordinal();
	}
	
}
