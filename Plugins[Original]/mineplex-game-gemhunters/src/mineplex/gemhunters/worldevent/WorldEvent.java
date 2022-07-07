package mineplex.gemhunters.worldevent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.world.WorldDataModule;
import mineplex.minecraft.game.core.damage.DamageManager;

public abstract class WorldEvent implements Listener
{

	private final WorldEventType _eventType;
	
	private WorldEventState _eventState;
	
	protected final DamageManager _damage;
	protected final LootModule _loot;
	protected final WorldDataModule _worldData;
	protected final WorldEventModule _worldEvent;
	
	protected final Set<LivingEntity> _entities;
	
	protected long _start;
	private long _complete;
	
	public WorldEvent(WorldEventType eventType)
	{
		_eventType = eventType;
		_eventState = null;
		
		_damage = Managers.get(DamageManager.class);
		_loot = Managers.get(LootModule.class);
		_worldData = Managers.get(WorldDataModule.class);
		_worldEvent = Managers.get(WorldEventModule.class);
		
		_entities = new HashSet<>();
		
		_worldEvent.registerEvents(this);
	}
		
	public abstract void onStart();
	
	public abstract boolean checkToEnd();
	
	public abstract void onEnd();
	
	public abstract Location[] getEventLocations();
	
	public abstract double getProgress();
	
	private final void start()
	{
		if (isLive())
		{
			return;
		}
		
		UtilTextMiddle.display(C.cRed + _eventType.getName(), C.cGray + "World Event is starting!", 20, 60, 20);
		UtilServer.broadcast(F.main(_worldEvent.getName(), "The " + F.elem(_eventType.getName()) + " world event is starting!"));
		
		_start = System.currentTimeMillis();
		onStart();
	}
	
	private final void end()
	{
		_complete = System.currentTimeMillis();
		
		UtilServer.broadcast(F.main(_worldEvent.getName(), "The " + F.elem(_eventType.getName()) + " world event is over!"));
		
		for (LivingEntity entity : _entities)
		{
			entity.remove();
		}
		
		_entities.clear();
		
		onEnd();
	}
	
	@EventHandler
	public void updateEntities(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		Iterator<LivingEntity> iterator = _entities.iterator();
		
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			
			if (entity.isDead() || !entity.isValid())
			{
				entity.remove();
				iterator.remove();
			}
		}
	}
	
	public void addEntity(LivingEntity entity)
	{
		_entities.add(entity);
	}
	
	public WorldEventType getEventType()
	{
		return _eventType;
	}
	
	public void setEventState(WorldEventState eventState)
	{
		_eventState = eventState;
		
		if (eventState == null)
		{
			return;
		}
		
		switch (eventState)
		{
		case WARMUP:
			start();
			break;
		case COMPLETE:
			end();
			break;
		default:
			break;
		}
	}
	
	public boolean isEnabled()
	{
		return _eventState != null;
	}
	
	public boolean isWarmup()
	{
		return _eventState == WorldEventState.WARMUP;
	}
	
	public boolean isLive()
	{
		return _eventState == WorldEventState.LIVE;
	}
	
	public boolean isInProgress()
	{
		return isWarmup() || isLive();
	}
	
	public WorldEventState getEventState()
	{
		return _eventState;
	}
	
	public long getCompleteTime()
	{
		return _complete;
	}
}
