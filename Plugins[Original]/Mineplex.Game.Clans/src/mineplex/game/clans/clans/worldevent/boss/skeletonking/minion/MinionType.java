package mineplex.game.clans.clans.worldevent.boss.skeletonking.minion;

import org.bukkit.Location;

import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;

public enum MinionType
{
	WARRIOR(UndeadWarriorCreature.class),
	ARCHER(UndeadArcherCreature.class),
	WRAITH(WraithCreature.class);
	
	private Class<? extends EventCreature<?>> _code;
	
	private MinionType(Class<? extends EventCreature<?>> code)
	{
		_code = code;
	}
	
	public EventCreature<?> getNewInstance(WorldEvent event, Location spawn)
	{
		try
		{
			return _code.getConstructor(WorldEvent.class, Location.class).newInstance(event, spawn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}