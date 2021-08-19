package nautilus.game.pvp.worldevent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import me.chiss.Core.Modules.BlockRegenerateSet;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public abstract class EventBase implements Listener
{
	public enum EventState
	{
		PREPARE,
		LIVE,
		END
	}

	public EventManager Manager;

	private String _eventName;
	private EventState _state;

	private long _eventStart = 0;
	private long _eventExpire = 0;
	
	private long _lastAnnounce = 0;

	private ArrayList<EventMob> _creatures = new ArrayList<EventMob>();
	private HashMap<Block, Entry<Integer, Byte>> _blocks = new HashMap<Block, Entry<Integer, Byte>>();
	private BlockRegenerateSet _blockSet;
	
	private HashSet<Entity> _arrows = new HashSet<Entity>();
	
	private int _idleTicks = 0;
	
	public EventBase(EventManager manager, String eventName, double eventExpireHours)
	{
		_eventName = eventName;
		
		_eventStart = System.currentTimeMillis();
		SetExpire(eventExpireHours);
		
		_lastAnnounce = System.currentTimeMillis();

		Manager = manager;
		
		_blockSet = Manager.BlockRegenerate().CreateSet(10);
		
		_state = EventState.PREPARE;
	}

	public void TriggerStart()
	{
		System.out.println("Preparing World Event: " + GetEventName());
		Start();
		
		Manager.RecordStart(this);
	}

	public void TriggerStop()
	{
		Clean();
		Stop();
		
		Manager.RecordStop(this);
	}

	public void Clean()
	{
		SetState(EventState.END);
		
		//Remove Creatures
		HashSet<EventMob> remove = new HashSet<EventMob>();

		for (EventMob cur : _creatures)
			remove.add(cur);

		_creatures.clear();

		//Mobs
		for (EventMob cur : remove)
			cur.Remove();
	
		//Arrows
		for (Entity cur : _arrows)
			cur.remove();

		//Remove Blocks
		_blockSet.Start();

		_blocks.clear();
	}

	public abstract void Start();
	public abstract void Stop();

	@EventHandler
	public void Prepare(UpdateEvent event)
	{
		if (_state != EventState.PREPARE)
			return;

		if (event.getType() != UpdateType.TICK)
			return;
		
		_idleTicks++;
		if (_idleTicks > 600)
		{
			_idleTicks = 0;
			System.out.println("Failed to prepare " + GetEventName() + ".");
			TriggerStop();
			return;
		}	

		PrepareCustom();
	}

	public abstract void PrepareCustom();
	
	public void CreatureRegister(EventMob creature) 
	{
		_creatures.add(creature);
		UtilServer.getServer().getPluginManager().registerEvents(creature, Manager.Plugin());
	}

	public void CreatureDeregister(EventMob creature) 
	{
		_creatures.remove(creature);
		HandlerList.unregisterAll(creature);
	}

	public ArrayList<EventMob> GetCreatures()
	{
		return _creatures;
	}

	public void AddBlock(Block block, int id, byte data)
	{
		if (!_blocks.containsKey(block))
			_blocks.put(block, new AbstractMap.SimpleEntry<Integer, Byte>(block.getTypeId(), block.getData()));	
		
		_blockSet.AddBlock(block.getLocation(), block.getTypeId(), block.getData());
		block.setTypeIdAndData(id, data, true);
	}

	public boolean RemoveBlock(Block block)
	{
		Entry<Integer, Byte> entry = _blocks.remove(block);
		if (entry == null)		return false;

		block.setTypeIdAndData(entry.getKey(), entry.getValue(), true);
		return true;
	}

	public HashMap<Block, Entry<Integer, Byte>> GetBlocks()
	{
		return _blocks;
	}

	public String GetEventName() 
	{
		return _eventName;
	}

	public EventState GetState() {
		return _state;
	}

	public void SetState(EventState _state) {
		this._state = _state;
	}
	
	public void SetEventName(String newName)
	{
		_eventName = newName;
	}
	
	public void ResetIdleTicks()
	{
		_idleTicks = 0;
	}
	
	@EventHandler
	public void AnnounceUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (!UtilTime.elapsed(_lastAnnounce, 120000))
			return;
		
		_lastAnnounce = System.currentTimeMillis();
		
		AnnounceDuring();
	}
	
	public abstract void AnnounceStart();
	public abstract void AnnounceDuring();
	public abstract void AnnounceEnd();
	public abstract void AnnounceExpire();
	
	@EventHandler
	public void ExpireUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (!CanExpire())
			return;
		
		if (System.currentTimeMillis() - _eventStart < _eventExpire)
			return;
		
		TriggerStop();
		AnnounceExpire();
	}
	
	public void SetExpire(double eventExpireHours)
	{
		_eventExpire = (long) (eventExpireHours * 3600000);
	}
	
	public abstract boolean CanExpire();

	public void AddArrow(Arrow arrow) 
	{
		_arrows.add(arrow);
	}
	
	@EventHandler
	public void ArrowClean(ProjectileHitEvent event)
	{
		if (_arrows.remove(event.getEntity()))
		{			
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void ArrowClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (_arrows.isEmpty())
			return;
		
		for (Iterator<Entity> i = _arrows.iterator(); i.hasNext();) 
		{
			Entity cur = i.next();
			
			if (cur.isDead() || !cur.isValid() || cur.getTicksLived() > 60 || cur.getTicksLived() == 0)
			{
				i.remove();
				cur.remove();
			}
		}	
	}
}
