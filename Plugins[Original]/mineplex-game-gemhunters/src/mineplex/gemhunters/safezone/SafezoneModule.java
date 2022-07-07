package mineplex.gemhunters.safezone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mineplex.core.recharge.Recharge;
import mineplex.gemhunters.death.event.QuitNPCSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.playerstatus.PlayerStatus;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.playerstatus.PlayerStatusType;
import mineplex.gemhunters.world.WorldDataModule;

@ReflectivelyCreateMiniPlugin
public class SafezoneModule extends MiniPlugin
{

	public static final String SAFEZONE_DATA_PREFIX = "SAFEZONE";
	public static final String SAFEZONE_DATA_IGNORE = "IGNORE";

	private final PlayerStatusModule _playerStatus;
	private final WorldDataModule _worldData;

	private final Map<UUID, String> _currentSafezone;

	private SafezoneModule()
	{
		super("Safezone");

		_playerStatus = require(PlayerStatusModule.class);
		_worldData = require(WorldDataModule.class);

		_currentSafezone = new HashMap<>();
	}

	@EventHandler
	public void updateSafeZone(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			UUID key = player.getUniqueId();
			String oldSafezone = _currentSafezone.get(key);
			boolean isInOldSafezone = oldSafezone != null;
			String newSafezone = getSafezone(player.getLocation());
			boolean isInNewSafezone = newSafezone != null;

			// null -> not null
			if (!isInOldSafezone && isInNewSafezone)
			{
				if (!newSafezone.contains(SAFEZONE_DATA_IGNORE))
				{
					UtilTextMiddle.display("", C.cYellow + "Entering " + newSafezone, 10, 40, 10, player);
				}
				_currentSafezone.put(key, newSafezone);
			}
			// not null -> null
			else if (isInOldSafezone && !isInNewSafezone)
			{
				if (!oldSafezone.contains(SAFEZONE_DATA_IGNORE))
				{
					UtilTextMiddle.display("", C.cYellow + "Leaving " + oldSafezone, 10, 40, 10, player);
				}
				_playerStatus.setStatus(player, PlayerStatusType.DANGER, true);
				_currentSafezone.remove(key);
			}
		}
	}

	@EventHandler
	public void updatePlayerStatus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!isInSafeZone(player.getLocation()))
			{
				continue;
			}

			PlayerStatus current = _playerStatus.Get(player);

			if (current.getStatusType() == PlayerStatusType.DANGER)
			{
				if (getSafezone(player.getLocation()).contains(SAFEZONE_DATA_IGNORE))
				{
					_playerStatus.setStatus(player, PlayerStatusType.SAFE, true);
				}
				else if (Recharge.Instance.usable(player, "Cash Out"))
				{
					_playerStatus.setStatus(player, PlayerStatusType.SAFE);
				}
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_currentSafezone.remove(player.getUniqueId());
	}

	@EventHandler
	public void quitNpcSpawn(QuitNPCSpawnEvent event)
	{
		String safezone = getSafezone(event.getPlayer().getLocation());

		if (safezone != null && safezone.contains(SAFEZONE_DATA_IGNORE))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (isInSafeZone(player) && _playerStatus.Get(player).getStatusType() != PlayerStatusType.COMBAT)
		{
			event.setCancelled(true);
		}

		if (!Recharge.Instance.usable(player, "Cash Out"))
		{
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void entityAttack(EntityDamageByEntityEvent event)
	{
		// Handle people shooting arrows at people outside a safezone
		if (event.getDamager() instanceof Projectile)
		{
			Projectile projectile = (Projectile) event.getDamager();

			if (projectile.getShooter() instanceof LivingEntity)
			{
				LivingEntity entity = (LivingEntity) projectile.getShooter();

				if (isInSafeZone(entity.getLocation()))
				{
					event.setCancelled(true);
					return;
				}
			}
		}

		if (!(event.getDamager() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getDamager();

		if (isInSafeZone(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void hungerChange(FoodLevelChangeEvent event)
	{
		if (isInSafeZone(event.getEntity()) && event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();

			if (player.getFoodLevel() < event.getFoodLevel())
			{
				return;
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void flintAndSteelInteract(PlayerInteractEvent event)
	{
		if (event.getItem() == null || event.getItem().getType() != Material.FLINT_AND_STEEL || event.getClickedBlock() == null || !isInSafeZone(event.getClickedBlock().getLocation()))
		{
			return;
		}

		event.setCancelled(true);
	}

	public boolean isInSafeZone(HumanEntity player)
	{
		return isInSafeZone(player.getLocation()) && _playerStatus.Get((Player) player).getStatusType() != PlayerStatusType.COMBAT;
	}
	
	public boolean isInSafeZone(Location location)
	{
		return getSafezone(location) != null;
	}

	public boolean isInSafeZone(Location location, String safezone)
	{
		if (safezone == null)
		{
			return false;
		}
		
		List<Location> bounds = _worldData.getCustomLocation(SAFEZONE_DATA_PREFIX + " " + safezone);

		if (bounds == null || bounds.size() != 2)
		{
			log("Error regarding safezone bounds for region " + safezone + " there are " + bounds.size() + " points instead of 2. Ignoring this safezone!");
			return false;
		}

		return UtilAlg.inBoundingBox(location, bounds.get(0), bounds.get(1));
	}

	public String getSafezone(Location location)
	{
		Map<String, List<Location>> customLocations = _worldData.getAllCustomLocations();

		for (String key : customLocations.keySet())
		{
			if (!key.startsWith(SAFEZONE_DATA_PREFIX))
			{
				continue;
			}

			List<Location> bounds = customLocations.get(key);

			if (bounds.size() != 2)
			{
				log("Error regarding safezone bounds for region " + key + " there are " + bounds.size() + " points instead of 2. Ignoring this safezone!");
				continue;
			}

			if (UtilAlg.inBoundingBox(location, bounds.get(0), bounds.get(1)))
			{
				String name = "";
				String[] split = key.split(" ");

				for (int i = 1; i < split.length; i++)
				{
					name += split[i] + " ";
				}

				return name.trim();
			}
		}

		return null;
	}
}
