package mineplex.game.clans.clans.worldevent.raid;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.api.EventState;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;
import mineplex.game.clans.spawn.Spawn;

public abstract class RaidWorldEvent extends WorldEvent
{
	private static int nextId = 0;
	
	protected RaidManager Manager;
	protected WorldData WorldData;
	
	protected final int Id;
	
	private List<Player> _players = new ArrayList<>();
	
	protected long _forceEnd = -1;
	
	private boolean _cleanup = false;
	
	public RaidWorldEvent(String name, WorldData data, RaidManager manager)
	{
		super(name, new Location(data.World, data.MinX + ((data.MaxX - data.MinX) / 2), data.MinY + ((data.MaxY - data.MinY) / 2), data.MinZ + ((data.MaxZ - data.MinZ) / 2)), UtilMath.getMax((data.MaxX - data.MinX) / 2, (data.MaxY - data.MinY) / 2, (data.MaxZ - data.MinZ) / 2), false, manager.getDisguiseManager(), manager.getProjectileManager(), manager.getDamageManager(), manager.getBlockRestore(), manager.getConditionManager());
		
		Id = nextId++;
		Manager = manager;
		WorldData = data;
		
		loadNecessaryChunks();
		customLoad();
	}
	
	protected abstract void customLoad();
	
	protected abstract void afterTeleportIn();
	
	private void loadNecessaryChunks()
	{
		int minX = WorldData.MinX >> 4;
		int minZ = WorldData.MinZ >> 4;
		int maxX = WorldData.MaxX >> 4;
		int maxZ = WorldData.MaxZ >> 4;

		for (int x = minX; x <= maxX; x++)
		{
			for (int z = minZ; z <= maxZ; z++)
			{
				WorldData.World.getChunkAt(x, z);
			}
		}
	}
	
	public mineplex.game.clans.clans.worldevent.raid.WorldData getWorldData()
	{
		return WorldData;
	}
	
	public List<Player> getPlayers()
	{
		List<Player> players = new ArrayList<>();
		players.addAll(_players);
		return players;
	}
	
	public int getId()
	{
		return Id;
	}
	
	@Override
	public void customStart()
	{
		_forceEnd = System.currentTimeMillis() + UtilTime.convert(90, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
		int spawnIndex = 0;
		List<Location> spawns = WorldData.SpawnLocs.get("Red");
		for (Player player : _players)
		{
			if (spawnIndex >= spawns.size())
			{
				spawnIndex = 0;
			}
			player.teleport(spawns.get(spawnIndex));
			spawnIndex++;
		}
		afterTeleportIn();
	}
	
	@Override
	public void customCleanup(boolean onDisable)
	{
		_cleanup = true;
		_players.forEach(player -> player.teleport(Spawn.getNorthSpawn()));
		_players.clear();
		getCreatures().forEach(creature ->
		{
			HandlerList.unregisterAll(creature);
			creature.getEntity().remove();
		});
		getCreatures().clear();
		if (onDisable)
		{
			WorldData.uninitialize();
		}
		else
		{
			UtilServer.runSyncLater(WorldData::uninitialize, 20 * 10);
		}
	}
	
	public void addPlayer(Player player)
	{
		_players.add(player);
	}
	
	public void setForceEnd(long end)
	{
		_forceEnd = end;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (getState() != EventState.LIVE)
		{
			return;
		}
		if (_players.isEmpty())
		{
			stop();
		}
		else
		{
			updateLastActive();
		}
		if (_forceEnd != -1 && System.currentTimeMillis() >= _forceEnd)
		{
			stop();
		}
	}
	
	@EventHandler
	public void onCreatureDeath(EventCreatureDeathEvent event)
	{
		if (!event.getCreature().getEvent().getName().equals(getName()))
		{
			return;
		}
		RaidWorldEvent worldEvent = (RaidWorldEvent) event.getCreature().getEvent();
		if (worldEvent.Id != Id)
		{
			return;
		}
		if (event.getCreature() instanceof RaidCreature)
		{
			((RaidCreature<?>)event.getCreature()).handleDeath(event.getCreature().getLastKnownLocation());
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		if (getState() == EventState.STOPPED || getState() == EventState.REMOVED || _cleanup)
		{
			return;
		}
		if (!event.getWorld().equals(WorldData.World))
		{
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void breakBlock(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		if (!block.getWorld().equals(WorldData.World))
		{
			return;
		}
		
		event.setCancelled(true);
		UtilPlayer.message(player, F.main(getName(), "You cannot build here!"));
	}
	
	@EventHandler
	public void placeBlock(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		if (!block.getWorld().equals(WorldData.World))
		{
			return;
		}
		
		event.setCancelled(true);
		UtilPlayer.message(player, F.main(getName(), "You cannot build here!"));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onQuit(PlayerQuitEvent event)
	{
		if (_cleanup)
		{
			return;
		}
		if (_players.remove(event.getPlayer()))
		{
			event.getPlayer().setHealth(0);
			event.getPlayer().spigot().respawn();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDie(PlayerDeathEvent event)
	{
		if (_cleanup)
		{
			return;
		}
		_players.remove(event.getEntity());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event)
	{
		if (_cleanup)
		{
			return;
		}
		if (!event.getTo().getWorld().equals(WorldData.World))
		{
			_players.remove(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onTpHome(ClansCommandExecutedEvent event)
	{
		if (!_players.contains(event.getPlayer()))
		{
			return;
		}
		if (event.getCommand().equalsIgnoreCase("tphome") || event.getCommand().equalsIgnoreCase("stuck"))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot teleport while in a raid!"));
			return;
		}
		if (event.getCommand().equalsIgnoreCase("claim") || event.getCommand().equalsIgnoreCase("unclaim") || event.getCommand().equalsIgnoreCase("unclaimall") || event.getCommand().equalsIgnoreCase("homeset"))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot manage your clan's territory while in a raid!"));
			return;
		}
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof RaidWorldEvent))
		{
			return false;
		}
		
		return ((RaidWorldEvent)object).getId() == getId();
	}
	
	@Override
	public int hashCode()
	{
		return Integer.hashCode(getId());
	}
}