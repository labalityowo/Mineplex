package nautilus.game.pvp.worldevent;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public abstract class EventMobBoss extends EventMob
{
	protected ArrayList<EventMobMinion> _minions = new ArrayList<EventMobMinion>();
	protected int _minionsMax = 0;
	protected int _minionTargetLimit = 1;
	
	protected int _state = 0;
	
	protected Location _location;
	
	public EventMobBoss(EventBase event, Location location, String name, boolean useName, int health, EntityType type) 
	{
		super(event, location, name, useName, health, type);
		
		_location = location;
	}
	
	public void MinionRegister(EventMobMinion minion) 
	{
		_minions.add(minion);
	}

	public void MinionDeregister(EventMobMinion minion) 
	{
		_minions.remove(minion);
	}	

	public ArrayList<EventMobMinion> GetMinions() 
	{
		return _minions;
	}
	
	public boolean MinionTarget(LivingEntity ent)
	{
		int i = 0;

		for (EventMobMinion cur : _minions)
			if (cur.GetTarget() != null && cur.GetTarget().equals(ent))
				i++;

		return i < _minionTargetLimit;
	}
	
	public int GetState()
	{
		return _state;
	}
	
	public abstract void DistanceAction();
}
