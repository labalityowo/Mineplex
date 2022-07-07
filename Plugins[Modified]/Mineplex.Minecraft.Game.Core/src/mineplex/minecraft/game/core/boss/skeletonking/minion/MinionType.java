package mineplex.minecraft.game.core.boss.skeletonking.minion;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Location;

import mineplex.minecraft.game.core.boss.EventCreature;
import mineplex.minecraft.game.core.boss.WorldEvent;

public enum MinionType
{
	WARRIOR(UndeadWarriorCreature.class),
	ARCHER(UndeadArcherCreature.class),
	WRAITH(WraithCreature.class);
	
	private Class<? extends EventCreature> _code;
	
	private MinionType(Class<? extends EventCreature> code)
	{
		_code = code;
	}
	
	public EventCreature getNewInstance(WorldEvent event, Location spawn)
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