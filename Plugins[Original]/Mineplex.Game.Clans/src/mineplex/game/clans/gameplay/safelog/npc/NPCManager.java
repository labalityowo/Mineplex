package mineplex.game.clans.gameplay.safelog.npc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.MiniPlugin;
import mineplex.core.hologram.HologramManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.spawn.Spawn;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class NPCManager extends MiniPlugin
{
	public static final long COMBAT_LOG_DURATION = 30000;
	
	private static NPCManager _instance;
	
	public static NPCManager getInstance()
	{
		return _instance;
	}
	
	private ClansManager _clansManager;
	private Set<CombatLogNPC> _logoutNpcs;
	private Set<Integer> _toKillIds;
	
	private HologramManager _hologramManager;
	
	public NPCManager(ClansManager plugin, HologramManager hologramManager)
	{
		super("NPC Manager", plugin.getPlugin());
		
		_instance = this;
		_logoutNpcs = new HashSet<>();
		_toKillIds = new HashSet<>();
		_clansManager = plugin;
		_hologramManager = hologramManager;
	}
	
	@Override
	public void disable()
	{
		log("Killing logout npcs");
		
		// Despawn/kill all combat log NPCs on server shutdown
		for (CombatLogNPC npc : _logoutNpcs)
		{
			npc.despawn();
		}
		_logoutNpcs.clear();
	}
	
	public void spawnLogoutNpc(Player player)
	{
		if (!hasLogoutNpc(player))
		{
			CombatLogNPC npc = new CombatLogNPC(player, _clansManager.getDisguiseManager(), _hologramManager, player.getGameMode().equals(GameMode.CREATIVE), _clansManager.UserDataDir);
			npc.spawn();
			_logoutNpcs.add(npc);
			log(String.format("Spawned combat log NPC for %s!", player.getName()));
		}
	}
	
	@EventHandler
	public void killNpcs(PlayerJoinEvent event)
	{
		for (LivingEntity entity : Spawn.getSpawnWorld().getLivingEntities())
		{
			if (entity.hasMetadata("CombatLogNPC") && ((FixedMetadataValue) entity.getMetadata("CombatLogNPC").get(0)).asString().equals(event.getPlayer().getUniqueId().toString()))
			{
				entity.remove();
			}
		}
	}
	
	public void despawnLogoutNpc(Player player)
	{
		CombatLogNPC npc = getLogoutNpc(player);
		
		if (npc != null)
		{
			_toKillIds.add(npc.getEntityId());
			npc.despawn();
			_logoutNpcs.remove(npc);
			log(String.format("Despawned combat log NPC for %s!", player.getName()));
		}
	}
	
	public boolean hasLogoutNpc(Player player)
	{
		return getLogoutNpc(player) != null;
	}
	
	public CombatLogNPC getLogoutNpc(Player player)
	{
		for (CombatLogNPC logoutNpc : _logoutNpcs)
		{
			if (logoutNpc.matchesPlayer(player))
			{
				return logoutNpc;
			}
		}
		
		return null;
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		for (Entity entity : event.getChunk().getEntities())
		{
			for (CombatLogNPC npc : _logoutNpcs)
			{
				if (entity.getEntityId() == npc.getEntityId())
				{
					event.setCancelled(true);
					
					break;
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event)
	{
		CombatLogNPC logoutNpc = getLogoutNpc(event.getEntity());
		
		if (logoutNpc != null)
		{
			logoutNpc.onDeath(logoutNpc.getLastDamager());
			event.getDrops().clear(); // Clear the entity's item drops. Manually
										// drops combat log items earlier
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamaged(CustomDamageEvent event)
	{
		CombatLogNPC logoutNpc = getLogoutNpc(event.GetDamageeEntity());
		
		if (logoutNpc != null && event.GetDamagerEntity(true) != null)
		{
			if (logoutNpc.wasCreative())
			{
				event.SetCancelled("Cannot hurt creative player");
				return;
			}
			
			if (event.GetDamagerPlayer(true) != null)
			{
				ClanInfo clan = _clansManager.getClan(event.GetDamagerPlayer(true));
				
				if (clan != null && clan.isMember(logoutNpc.getPlayerInfo().getUniqueId()))
				{
					event.SetCancelled("Cannot hurt clan member.");
					return;
				}
				
				event.GetDamagerPlayer(true).playSound(event.GetDamagerPlayer(true).getLocation(), Sound.HURT_FLESH, 1, 1);
			}
			
			logoutNpc.setLastDamager(((CraftLivingEntity) event.GetDamagerEntity(true)));
			event.SetKnockback(false);
		}
	}
	
	@EventHandler
	public void onEntityIgnite(EntityCombustEvent event)
	{
		if (isLogoutNpc(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTER)
		{
			for (CombatLogNPC npc : _logoutNpcs)
			{
				npc.update();
			}
		}
		
		if (event.getType() == UpdateType.SEC)
		{
			Iterator<CombatLogNPC> iterator = _logoutNpcs.iterator();
			
			while (iterator.hasNext())
			{
				CombatLogNPC npc = iterator.next();
				
				if (Bukkit.getPlayer(npc.getPlayerInfo().getPlayerName()) != null)
				{
					System.out.println("{NPCMANAGER} ORIGINAL PLAYER ALIVE AND DESPAWNING");
					npc.despawn();
					iterator.remove();
				}
				
				if (!npc.isAlive())
				{
					System.out.println("{NPCMANAGER} NOT ALIVE AND REMOVING");
					npc.remove();
					iterator.remove();
				}
				else if (npc.getAliveDuation() > COMBAT_LOG_DURATION)
				{
					System.out.println("{NPCMANAGER} DESPAWNING");
					npc.despawn();
					iterator.remove();
				}
			}
		}
	}
	
	private boolean isLogoutNpc(Entity entity)
	{
		return getLogoutNpc(entity) != null;
	}
	
	private CombatLogNPC getLogoutNpc(Entity entity)
	{
		return getLogoutNpc(entity.getEntityId());
	}
	
	private CombatLogNPC getLogoutNpc(int entityId)
	{
		for (CombatLogNPC npc : _logoutNpcs)
		{
			if (npc.getEntityId() == entityId)
			{
				return npc;
			}
		}
		
		return null;
	}
}