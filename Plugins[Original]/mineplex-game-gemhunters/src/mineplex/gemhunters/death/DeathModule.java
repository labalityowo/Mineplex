package mineplex.gemhunters.death;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Sets;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.stats.StatsManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import mineplex.gemhunters.death.event.PlayerCustomRespawnEvent;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.playerstatus.PlayerStatusType;
import mineplex.gemhunters.spawn.SpawnModule;

/**
 * This module handles anything to do with a players death
 */
@ReflectivelyCreateMiniPlugin
public class DeathModule extends MiniPlugin
{

	// Don't need to be dropped to avoid duplication.
	private static final Set<Material> DISALLOWED_DROPS = Sets.newHashSet(Material.EMERALD, Material.MAP, Material.STAINED_GLASS_PANE);
	private static final int DEATH_ANIMATION_TIME = 7000;
	private static final int DEATH_ANIMATION_COUNTDOWN = 2000;

	private final PlayerStatusModule _playerStatus;
	private final StatsManager _stats;
	private final SpawnModule _spawn;

	private final Map<UUID, Long> _toRemove;

	private DeathModule()
	{
		super("Death");

		_playerStatus = require(PlayerStatusModule.class);
		_stats = require(StatsManager.class);
		_spawn = require(SpawnModule.class);

		_toRemove = new HashMap<>();
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		// Stop the player dieing
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setExhaustion(0);

		// Record the stats
		Player killer = player.getKiller();

		if (killer != null)
		{
			_stats.incrementStat(killer, "Gem Hunters.Kills", 1);
		}

		startAnimation(player);
		_toRemove.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (DISALLOWED_DROPS.contains(event.getEntity().getItemStack().getType()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void updateAnimations(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Iterator<UUID> iterator = _toRemove.keySet().iterator();

		while (iterator.hasNext())
		{
			UUID key = iterator.next();
			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				iterator.remove();
				continue;
			}

			long start = _toRemove.get(key);
			long end = start + DEATH_ANIMATION_TIME + 1000;

			if (UtilTime.elapsed(start, DEATH_ANIMATION_TIME))
			{
				stopAnimation(player);
				iterator.remove();
				continue;
			}
			else if (UtilTime.elapsed(start, DEATH_ANIMATION_COUNTDOWN))
			{
				UtilTextMiddle.display(C.cRedB + "YOU DIED", String.valueOf((int) (end - System.currentTimeMillis()) / 1000), 0, 20, 0, player);
			}
		}
	}

	public void startAnimation(Player player)
	{
		UtilTextMiddle.display(C.cRedB + "YOU DIED", "Respawning shortly", 0, 60, 0, player);
		((CraftPlayer) player).getHandle().spectating = true;
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setGameMode(GameMode.CREATIVE);
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		for (Player other : Bukkit.getOnlinePlayers())
		{
			vm.hidePlayer(other, player, "GH Respawning");
		}
		
		_playerStatus.setStatus(player, PlayerStatusType.DANGER, true);
	}

	public void stopAnimation(Player player)
	{
		UtilTextMiddle.display(C.cGreenB + "RESPAWNED", "", 0, 20, 20, player);
		((CraftPlayer) player).getHandle().spectating = false;
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setGameMode(GameMode.SURVIVAL);
		_spawn.teleportToSpawn(player);
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		for (Player other : Bukkit.getOnlinePlayers())
		{
			vm.showPlayer(other, player, "GH Respawning");
		}
		
		PlayerCustomRespawnEvent event = new PlayerCustomRespawnEvent(player);

		UtilServer.CallEvent(event);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (isRespawning(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void itemPickup(PlayerPickupItemEvent event)
	{
		if (isRespawning(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (isRespawning(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (isRespawning(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void inventory(InventoryClickEvent event)
	{
		if (isRespawning(event.getWhoClicked()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void entityDamage(EntityDamageEvent event)
	{		
		if (event instanceof EntityDamageByEntityEvent)
		{
			if (isRespawning(((EntityDamageByEntityEvent) event).getDamager()))
			{
				event.setCancelled(true);
			}
		}
		
		if (isRespawning(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_toRemove.remove(event.getPlayer().getUniqueId());
	}
	
	public boolean isRespawning(Entity player)
	{
		return _toRemove.containsKey(player.getUniqueId());
	}
}