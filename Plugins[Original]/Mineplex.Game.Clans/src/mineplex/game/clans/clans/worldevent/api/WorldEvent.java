 package mineplex.game.clans.clans.worldevent.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.blockrestore.BlockRestoreMap;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;

public abstract class WorldEvent implements Listener, ScoreboardElement
{
	// 30 Minutes
	private static final int ACTIVE_TIMEOUT = 1800000;
	private static final int LOADING_TIMEOUT = 300000;
	
	private DamageManager _damageManager;
	private ConditionManager _conditionManager;
	private DisguiseManager _disguiseManager;
	private ProjectileManager _projectileManager;
	
	private String _name;
	private EventState _state;
	private EventArena _arena;
	private Random _random;
	private long _timeStarted;
	private long _lastActive;
	private boolean _announceStart;
	
	// Creatures
	private List<EventCreature<?>> _creatures;
	// Block Restore
	private BlockRestoreMap _blockRestoreMap;
	private double _difficulty = 1;
	
	public WorldEvent(String name, Location centerLocation, double radius, boolean announceStart, DisguiseManager disguiseManager, ProjectileManager projectileManager, DamageManager damageManager, BlockRestore blockRestore, ConditionManager conditionManager)
	{
		_disguiseManager = disguiseManager;
		_projectileManager = projectileManager;
		_damageManager = damageManager;
		_conditionManager = conditionManager;
		
		_name = name;
		_state = EventState.LOADING;
		_arena = new EventArena(centerLocation, radius);
		_random = new Random();
		_announceStart = announceStart;
		
		_creatures = new ArrayList<>();
		_blockRestoreMap = blockRestore.createMap();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public EventArena getEventArena()
	{
		return _arena;
	}
	
	public EventState getState()
	{
		return _state;
	}
	
	protected void setState(EventState state)
	{
		_state = state;
	}
	
	public double getDifficulty()
	{
		return _difficulty;
	}
	
	public void setDifficulty(double difficulty)
	{
		_difficulty = difficulty;
	}
	
	public boolean allowsIcePrison()
	{
		return false;
	}
	
	public Location getCenterLocation()
	{
		return _arena.getCenterLocation();
	}
	
	public List<EventCreature<?>> getCreatures()
	{
		return _creatures;
	}
	
	public void registerCreature(EventCreature<?> creature)
	{
		UtilServer.RegisterEvents(creature);
		_creatures.add(creature);
	}
	
	public void removeCreature(EventCreature<?> creature)
	{
		Bukkit.getPluginManager().callEvent(new EventCreatureDeathEvent(creature));
		HandlerList.unregisterAll(creature);
		_creatures.remove(creature);
	}
	
	public void clearCreatures()
	{
		for (EventCreature<?> creature : _creatures)
		{
			creature.remove(false);
			HandlerList.unregisterAll(creature);
		}
		
		_creatures.clear();
	}
	
	public long getTimeRunning()
	{
		return System.currentTimeMillis() - _timeStarted;
	}
	
	public long getLastActive()
	{
		return _lastActive;
	}
	
	public void updateLastActive()
	{
		_lastActive = System.currentTimeMillis();
	}
	
	public boolean isInBounds(Location location, boolean flat)
	{
		return _arena.isInArena(location, flat);
	}
	
	protected Random getRandom()
	{
		return _random;
	}
	
	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}
	
	public ProjectileManager getProjectileManager()
	{
		return _projectileManager;
	}
	
	public DamageManager getDamageManager()
	{
		return _damageManager;
	}
	
	public ConditionManager getCondition()
	{
		return _conditionManager;
	}
	
	protected BlockRestoreMap getBlockRestoreMap()
	{
		return _blockRestoreMap;
	}
	
	public void setBlock(Block block, Material material)
	{
		setBlock(block, material, (byte) 0);
	}
	
	@SuppressWarnings("deprecation")
	public void setBlock(Block block, Material material, byte data)
	{
		setBlock(block, material.getId(), data);
	}
	
	public void setBlock(Block block, int id, byte data)
	{
		_blockRestoreMap.set(block, id, data);
	}
	
	public void restoreBlocks()
	{
		_blockRestoreMap.restore();
	}
	
	public final void start()
	{
		_timeStarted = System.currentTimeMillis();
		_lastActive = System.currentTimeMillis();
		if (_announceStart)
		{
			announceStart();
		}
		setState(EventState.LIVE);
		customStart();
		UtilServer.RegisterEvents(this);
	}
	
	public void announceStart()
	{
		UtilTextMiddle.display(C.cGreen + getName(), UtilWorld.locToStrClean(getCenterLocation()), 10, 100, 40);

		UtilServer.broadcast(F.main("Event", F.elem(getName()) + " has started at coordinates " + F.elem(UtilWorld.locToStrClean(getCenterLocation()))));
	}
	
	protected abstract void customStart();
	
	protected abstract void customTick();
	
	public final void cleanup(boolean onDisable)
	{
		clearCreatures();
		restoreBlocks();
		customCleanup(onDisable);
	}
	
	public abstract void customCleanup(boolean onDisable);
	
	public final void stop()
	{
		stop(false);
	}
	
	public final void stop(boolean onDisable)
	{
		cleanup(onDisable);
		if (onDisable)
		{
			setState(EventState.REMOVED);
		}
		else
		{
			setState(EventState.STOPPED);
		}
		customStop();
		HandlerList.unregisterAll(this);
	}
	
	protected abstract void customStop();
	
	public int getRandomRange(int min, int max)
	{
		return min + _random.nextInt(UtilMath.clamp(max - min, min, max));
	}
	
	public void announceMessage(String message)
	{
		UtilServer.broadcast(F.main("Event", F.elem(getName()) + ": " + message));
	}
	
	public void sendMessage(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Event", F.elem(getName()) + ": " + message));
	}
	
	@EventHandler
	public void tick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		customTick();
	}
	
	@EventHandler
	public void endInactive(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		long diff = System.currentTimeMillis() - getLastActive();
		if (diff > ACTIVE_TIMEOUT)
		{
			stop();
		}
	}
	
	@EventHandler
	public void prepareTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		if (getState() != EventState.LOADING)
		{
			return;
		}
		
		// Event was preparing for more than 5 minutes
		if (getTimeRunning() > LOADING_TIMEOUT)
		{
			stop();
		}
	}
	
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		return null;
	}
}