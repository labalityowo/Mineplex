package mineplex.game.clans.clans.worldevent.raid;

import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;

public enum RaidType
{
	CHARLES_WITHERTON("Charles Witherton", WitherRaid.class);
	
	private String _raidName;
	private Class<? extends RaidWorldEvent> _clazz;
	
	private RaidType(String raidName, Class<? extends RaidWorldEvent> clazz)
	{
		_raidName = raidName;
		_clazz = clazz;
	}
	
	public String getRaidName()
	{
		return _raidName;
	}
	
	public Class<? extends RaidWorldEvent> getClazz()
	{
		return _clazz;
	}
}