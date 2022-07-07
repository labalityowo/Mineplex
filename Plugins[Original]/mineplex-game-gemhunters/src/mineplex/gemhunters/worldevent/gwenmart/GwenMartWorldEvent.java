package mineplex.gemhunters.worldevent.gwenmart;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.Managers;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;

public class GwenMartWorldEvent extends WorldEvent
{
	
	private static final double START_CHANCE = 0.01;
	private static final long WARMUP_TIME = TimeUnit.MINUTES.toMillis(3);
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(8);
	
	private final BlockRestore _restore;
	
	private final Location _average;
	private List<Location> _door;
	private boolean _colour;
	
	public GwenMartWorldEvent()
	{
		super(WorldEventType.GWEN_MART);
		
		_restore = Managers.require(BlockRestore.class);
		_average = UtilAlg.getAverageLocation(_worldData.getCustomLocation("GWEN_MART"));
		_door = _worldData.getCustomLocation(String.valueOf(Material.EMERALD_BLOCK.getId()));
		
		_worldEvent.runSyncLater(() -> {
			
			for (Location location : _door)
			{
				location.getBlock().setType(Material.WOOD);
			}
			
		}, 20);
	}
	
	@EventHandler
	public void trigger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWEST)
		{
			return;
		}
		
		if (Math.random() < START_CHANCE)
		{
			setEventState(WorldEventState.LIVE);
		}
	}
	
	@EventHandler
	public void updateOpening(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		_colour = !_colour;
		
		if (UtilTime.elapsed(_start, WARMUP_TIME) && isWarmup())
		{
			for (Location location : _door)
			{
				_restore.add(location.getBlock(), 0, (byte) 0, Long.MAX_VALUE);
			}
			
			for (Location location : _worldData.getCustomLocation("GWEN_MART_CHEST"))
			{
				location.getBlock().setType(Material.CHEST);
				_loot.addSpawnedChest(location, getRandomChestKey());
			}
			
			setEventState(WorldEventState.LIVE);
		}
		else if (isLive())
		{
			UtilTextBottom.display((_colour ? C.cDAquaB : C.cWhiteB) + "GWEN MART IS CLOSING IN " + UtilTime.MakeStr(_start + MAX_TIME - System.currentTimeMillis()), UtilServer.getPlayers());
		}
		else if (isWarmup())
		{
			UtilTextBottom.display((_colour ? C.cDAquaB : C.cWhiteB) + "GWEN MART IS OPENING IN " + UtilTime.MakeStr(_start + WARMUP_TIME - System.currentTimeMillis()), UtilServer.getPlayers());
		}
	}
	
	@Override
	public void onStart()
	{
	}

	@Override
	public boolean checkToEnd()
	{
		return UtilTime.elapsed(_start, MAX_TIME);
	}

	@Override
	public void onEnd()
	{
		Location teleportTo = _worldData.getCustomLocation("GWEN_MART_TP").get(0);
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (isInGwenMart(player.getLocation()))
			{
				player.leaveVehicle();
				player.teleport(teleportTo);
			}
		}
		
		for (Location location : _door)
		{
			_restore.restore(location.getBlock());
		}
	}

	@Override
	public Location[] getEventLocations()
	{
		return new Location[] { _average };
	}
	
	@Override
	public double getProgress()
	{
		return (double) (_start + MAX_TIME - System.currentTimeMillis()) / (double) MAX_TIME;
	}
	
	private boolean isInGwenMart(Location location)
	{
		List<Location> locations = _worldData.getCustomLocation("GWEN_MART");
		
		return UtilAlg.inBoundingBox(location, locations.get(0), locations.get(1));
	}
	
	private String getRandomChestKey()
	{
		double random = Math.random();
		
		if (random > 0.6)
		{
			return "ORANGE";
		}
		else if (random > 0.1)
		{
			return "PINK";
		}
		
		return "GREEN";
	}

}
