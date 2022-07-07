package mineplex.game.clans.spawn;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.classcombat.item.event.WebTossEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Spawn extends MiniPlugin
{
	public static final int SPAWN_RADIUS = 32;
	public static final int SHOP_RADIUS = 48;
	public static final long COMBAT_TAG_DURATION = 15000;
	public static final Location ORIGIN = new Location(getSpawnWorld(), 0, 0, 0);
	
	private static Spawn _instance;
	public static Spawn getInstance() { return _instance; }
	
	private WeightSet<Location> _spawns;
	private WeightSet<Location> _shops;
	private ClansManager _clansManager;
	
	private long _songEastLast = 0;
	private long _songWestLast = 0;
	
	private long _songEastLength = 345000; //Blocks
	private long _songWestLength = 185000; //Chirp
	 
	public Spawn(JavaPlugin plugin, ClansManager clansManager) 
	{
		super("Clan Spawn Zones", plugin);
		
		_instance = this;
		_spawns = new WeightSet<Location>(getNorthSpawn(), getSouthSpawn());
		_shops = new WeightSet<Location>(getEastTown(), getWestTown());
		_clansManager = clansManager;

		getSpawnWorld().setGameRuleValue("doDaylightCycle", "true");
	}
	
	/**
	 * Cancel most fire-spread mechanics to prevent mass destruction and uncontrollable fires.
	 * @param event
	 */
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if (event.getCause() != IgniteCause.FLINT_AND_STEEL)
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Prevent liquids from flowing into Safe Zone areas.
	 * @param event
	 */
	@EventHandler
	public void onWaterFlow(BlockFromToEvent event)
	{
		Block block = event.getToBlock();
		
		if (block.isLiquid() && isSafe(block.getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Prevent players from tossing Webs into spawn or from spawn.
	 * @param event
	 */
	@EventHandler
	public void onWebToss(WebTossEvent event)
	{
		if (isSafe(event.getLocation()))
		{
			event.setCancelled(true);
		}
		else if (event.getThrower() instanceof Player)
		{
			Player thrower = (Player) event.getThrower();
			
			if (isInSpawn(thrower))
			{
				event.setCancelled(true);
				attemptNotify(thrower, "You cannot throw webs while in a safe zone!");
			}
		}
	}
	
	/**
	 * Prevent Spawn blocks from burning
	 * @param event
	 */
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		if (isSafe(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Prevent {@link ItemFrame}s from being broken inside Spawn.
	 * @param event
	 */
	@EventHandler
	public void onItemFrameDestroyed(HangingBreakByEntityEvent event)
	{
		if (event.getEntity() instanceof ItemFrame)
		{
			if (isSafe(event.getEntity().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onItemFrameDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof ItemFrame)
		{
			if (isSafe(event.GetDamageeEntity().getLocation()))
			{
				event.SetCancelled("Item Frame Cancel");
			}
		}
	}
	
	@EventHandler
	public void onItemFrameDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof ItemFrame)
		{
			if (isSafe(event.getEntity().getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		for (Player cur : UtilServer.getPlayers())
		{
			if (isSafe(cur.getLocation()))
			{
				long lastDamager = _clansManager.getCombatManager().getLog(cur).GetLastCombatEngaged();
				long duration = System.currentTimeMillis() - lastDamager;
				
				if (!UtilTime.elapsed(lastDamager, COMBAT_TAG_DURATION))
				{
					String message = ChatColor.RED + "Unsafe for "
									+ ChatColor.YELLOW + F.time(UtilTime.convertString(COMBAT_TAG_DURATION - duration, 1, TimeUnit.FIT));
					
					UtilTextMiddle.display(null, message, 0, 20, 0, cur);
					playUnsafeParticles(cur);
				}
				else if (!UtilTime.elapsed(lastDamager, COMBAT_TAG_DURATION + 600))
				{
					UtilTextMiddle.display(null, ChatColor.GREEN + "Safe!", 0, 60, 20, cur);
				}
			}
		}
	}

	@EventHandler
	public void ignoreVelocity(PlayerVelocityEvent event)
	{
		if (_clansManager.getClanUtility().isSafe(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onSkill(SkillTriggerEvent event)
	{
		if (!isSafe(event.GetPlayer().getLocation()))
		{
			_clansManager.getCombatManager().getLog(event.GetPlayer()).SetLastCombatEngaged(System.currentTimeMillis());
		}
	}
	
	private void playUnsafeParticles(Player player)
	{
		UtilParticle.PlayParticle(UtilParticle.ParticleType.CRIT, player.getEyeLocation().add(0, 0.75d, 0), 0, 0, 0, 0.2f, 35, UtilParticle.ViewDist.NORMAL);
	}
	
	/**
	 * Prevent players from targetting spawn protected players using skills
	 * @param event
	 */
	@EventHandler
	public void onSkillTriggered(SkillTriggerEvent event)
	{
		if (event.GetTargets() == null) return;
		
		for (Entity entity : event.GetTargets())
		{
			if (isInSpawn(entity))
			{
				event.SetCancelled(true);
			}
		} 
	}
	
	/**
	 * Prevent players from activating skills while in Spawn
	 * @param event
	 */
	@EventHandler
	public void onSkillTriggeredInSpawn(SkillTriggerEvent event)
	{
		Player player = event.GetPlayer();
		
		if (isInSpawn(player))
		{
			UtilPlayer.message(event.GetPlayer(), F.main("Safe Zone", "You cannot use " + F.skill(event.GetSkillName()) + " in " + F.elem("Safe Zone") + "."));
			event.SetCancelled(true);
		}
	}

	/**
	 * Ensure players respawn in Spawn locations.
	 * @param event
	 */
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		event.setRespawnLocation(getSpawnLocation());
		_clansManager.getCombatManager().getLog(event.getPlayer()).SetLastCombatEngaged(System.currentTimeMillis() - Spawn.COMBAT_TAG_DURATION);
	}
	
	/*
	@EventHandler
	public void onPlayerFirstJoin(PlayerJoinEvent event)
	{
		if (!event.getPlayer().hasPlayedBefore())	// First time playing on server, teleport to a spawn
		{
			teleport(event.getPlayer(), getSpawnLocation(), 2);	// Teleport player to spawn after 2-tick delay to prevent on-join bug
		}
	}
	*/
	
	/**
	 * Prevent creatures from spawning inside Spawn
	 * @param event
	 */
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && isSafe(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Despawns any monsters within a spawn zone every two seconds.
	 * @param event
	 */
	@EventHandler
	public void onUpdateTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC) return;
		
		for (LivingEntity entity : getSpawnWorld().getLivingEntities())
		{
			if (entity.hasMetadata("CombatLogNPC"))
			{
				continue;
			}
			if (entity instanceof Monster)
			{
				Monster monster = (Monster) entity;
				
				if (isInSpawn(monster))
				{
					monster.setHealth(0);
				}
			}
		}

		String mobsEnabled = getSpawnWorld().getGameRuleValue("doMobSpawning");

		if (mobsEnabled.equals("true"))
		{
			if (getSpawnWorld().getTime() < 12000)
			{
				getSpawnWorld().setGameRuleValue("doMobSpawning", "false");
			}
		}
		else
		{
			if (getSpawnWorld().getTime() > 12000)
			{
				getSpawnWorld().setGameRuleValue("doMobSpawning", "true");
			}
		}
	}
	
	/**
	 * Prevent entities from dying and dropping items inside spawn.
	 * @param event
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Monster)
		{
			Monster monster = (Monster) event.getEntity();
			
			if (isInSpawn(monster))
			{
//				int size = event.getDrops().size();
				event.getDrops().clear();
			}
		}
	}
	
	/**
	 * Prevent creatures from targetting players who are in Spawn
	 * @param event
	 */
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event)
	{
		if (event.getTarget() != null && isSafe(event.getTarget().getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	/**
	 * Prevent players from breaking blocks in spawn
	 * @param event
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (isSafe(event.getBlock().getLocation()) || isInSpawn(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Prevent players from atacking others while in spawn
	 * @param event
	 */
	@EventHandler
	public void onPlayerAttack(CustomDamageEvent event)
	{
		Player victim = event.GetDamageePlayer();
		Player attacker = event.GetDamagerPlayer(true);	// Get (potentially ranged) attacker
		
		// wat
		if (victim == null)
		{
			return;
		}
		
		if (attacker == null)
		{
			return;
		}
		
		if (!isCombatTagged(victim))
		{
			if (isInSpawn(victim))
			{
				event.SetCancelled("Safe Zone");
				attemptNotify(attacker, "You cannot attack players who are in a safe zone!");
				return;
			}
			else if (isInSpawn(attacker) && !isCombatTagged(attacker))
			{
				event.SetCancelled("Safe Zone");
				attemptNotify(attacker, "You cannot attack untagged players while in a safe zone!");
				return;
			}	
		}
		
		System.out.println(event.GetCancellers());
	}
	
	public Location getSpawnLocation()
	{
		Location spawn = _spawns.generateRandom().clone();
		spawn.setWorld(getSpawnWorld());
		return spawn;
	}
	
	public Set<Location> getSpawnLocations()
	{
		return _spawns.elements();
	}
	
	public Set<Location> getShopLocations()
	{
		return _shops.elements();
	}
	
	public boolean isSafe(Location location)
	{
		return _clansManager.getClanUtility().isSafe(location);	// Check to see if location is in a SafeZone chunk
	}
	
	private static boolean isInRadius(Location origin, Location location, int radius)
	{
		int xOffset = Math.abs(location.getBlockX() - origin.getBlockX());
		int zOffset = Math.abs(location.getBlockZ() - origin.getBlockZ());
		
		return xOffset <= radius && zOffset <= radius;
	}
	
	// this is basically just isSafe();
	public boolean isInSpawn(Entity entity)
	{
		return entity != null && isSafe(entity.getLocation());
	}
	
	public static World getSpawnWorld()
	{
		return Bukkit.getWorld("world");
	}

	public static Location getWestTown()
	{
		return new Location(getSpawnWorld(), -440.91, 68, 23.08);
	}

	public static Location getWestTownCenter()
	{
		return new Location(getSpawnWorld(), -425, 69, 8);
	}

	public static Location getEastTown()
	{
		return new Location(getSpawnWorld(), 440.91, 64, -23.08);
	}

	public static Location getEastTownCenter()
	{
		return new Location(getSpawnWorld(), 425, 65, -8);
	}

	public static Location getNorthSpawn()
	{
		return new Location(getSpawnWorld(), 8.5, 206, -393.5);
	}

	public static Location getSouthSpawn()
	{
		return new Location(getSpawnWorld(), 8.5, 200, 390.5);
	}

	public boolean isCombatTagged(Player player)
	{
		return !UtilTime.elapsed(_clansManager.getCombatManager().getLog(player).GetLastCombatEngaged(), Spawn.COMBAT_TAG_DURATION);
	}

	public void teleport(final Player player, final Location location, int delay)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
		{
			player.teleport(location);
		}, delay);
	}

	/**
	 * Attempts to send a {@code message} to {@code entity} if they are a {@link Player}.
	 * @param entity
	 * @param message
	 */
	private static void attemptNotify(Entity entity, String message)
	{
		if (entity instanceof Player)
		{
			Player player = (Player) entity;
			UtilPlayer.message(player, F.main("Clans", message));
		}
	}
	
	@EventHandler
	public void playDatMusicALLDAYLONG(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		if (UtilTime.elapsed(_songEastLast, _songEastLength))
		{
			getNorthSpawn().getWorld().playEffect(getNorthSpawn(), Effect.RECORD_PLAY, 2258);	//Blocks
			
			_songEastLast = System.currentTimeMillis();
		}
		
		if (UtilTime.elapsed(_songWestLast, _songWestLength))
		{
			getSouthSpawn().getWorld().playEffect(getSouthSpawn(), Effect.RECORD_PLAY, 2259);	//Chirp
			
			_songWestLast = System.currentTimeMillis();
		}
	}

	public ClansManager getClansManager()
	{
		return _clansManager;
	}
}
