package mineplex.gemhunters.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.gemhunters.death.event.PlayerCustomRespawnEvent;
import mineplex.gemhunters.playerstatus.PlayerStatusModule;
import mineplex.gemhunters.playerstatus.PlayerStatusType;
import mineplex.gemhunters.safezone.SafezoneModule;
import mineplex.gemhunters.spawn.command.HubCommand;
import mineplex.gemhunters.spawn.event.PlayerTeleportIntoMapEvent;
import mineplex.gemhunters.util.ColouredTextAnimation;
import mineplex.gemhunters.util.SimpleNPC;
import mineplex.gemhunters.world.WorldDataModule;

@ReflectivelyCreateMiniPlugin
public class SpawnModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		HUB_COMMAND,
	}

	public static final int WORLD_BORDER_RADIUS = 1024;
	private static final int MAX_SPAWNING_Y = 73;
	private static final int MIN_PLAYER_DISTANCE_SQUARED = 6400;
	
	private final PlayerStatusModule _status;
	private final SafezoneModule _safezone;
	private final WorldDataModule _worldData;

	private Location _spawn;
	private Location _center;
	private boolean _npcsSpawned;

	private SpawnModule()
	{
		super("Spawn");

		_status = require(PlayerStatusModule.class);
		_safezone = require(SafezoneModule.class);
		_worldData = require(WorldDataModule.class);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.PLAYER.setPermission(Perm.HUB_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new HubCommand(this));
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		if (_spawn == null || _center == null)
		{
			_spawn = _worldData.getCustomLocation("PLAYER_SPAWN").get(0);
			_center = new Location(_worldData.World, 0, 64, 0);
			_worldData.World.setSpawnLocation(_spawn.getBlockX(), _spawn.getBlockY(), _spawn.getBlockZ());
		}

		if (_npcsSpawned)
		{
			return;
		}

		WorldBorder border = _spawn.getWorld().getWorldBorder();

		border.setCenter(_spawn);
		border.setSize(WORLD_BORDER_RADIUS * 2);

		_npcsSpawned = true;

		{
			Location location = _worldData.getCustomLocation("TELEPORT_NPC").get(0);

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _spawn)));

			new SimpleNPC(_plugin, location, Villager.class, C.cDRed + "! " + C.cRedB + "Enter The World" + C.cDRed + " !", clicker -> {

				Location toTeleport = getRandomLocation();

				if (toTeleport == null)
				{
					clicker.sendMessage(F.main(_moduleName, "A suitable teleport location could not be found. Please try again in a few seconds."));
					return;
				}

				PlayerTeleportIntoMapEvent teleportEvent = new PlayerTeleportIntoMapEvent(clicker, toTeleport);
				
				UtilServer.CallEvent(teleportEvent);
				
				if (teleportEvent.isCancelled())
				{
					clicker.sendMessage(F.main(_moduleName, "Something went wrong there, sorry. Please try again in a few seconds."));
					return;
				}
				
				clicker.teleport(toTeleport);
				clicker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 9));
				clicker.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 4 * 20, 9));
				
				ColouredTextAnimation animation = new ColouredTextAnimation("GEM HUNTERS", C.cGoldB + "M ", C.cGoldB + " M", new String[] { C.cDGreenB, C.cGreenB, C.cWhiteB });

				runSyncTimer(new BukkitRunnable()
				{

					@Override
					public void run()
					{
						if (animation.displayAsTitle(clicker))
						{
							cancel();
						}
					}
				}, 10, 4);
			});
		}
		{
			Location location = _worldData.getCustomLocation("RETURN_TO_HUB").get(0);

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _spawn)));

			new SimpleNPC(_plugin, location, Villager.class, C.cGoldB + "Return To Hub", clicker -> {

				Portal.getInstance().sendPlayerToGenericServer(clicker, GenericServer.HUB, Intent.PLAYER_REQUEST);

			});
		}
		{
			Location location = _worldData.getCustomLocation("TUTORIAL").get(0);

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _spawn)));

			new SimpleNPC(_plugin, location, Villager.class, C.cGoldB + "Gem Hunters Tutorial", null);
		}
		{
			for (Location location : _worldData.getCustomLocation("RANDOM_TELEPORT"))
			{
				new SimpleNPC(_plugin, location, Villager.class, C.cYellowB + "Random Teleport", clicker ->
				{

					if (_status.Get(clicker).getStatusType() == PlayerStatusType.COMBAT || !Recharge.Instance.usable(clicker, "Cash Out"))
					{
						clicker.sendMessage(F.main(_moduleName, "You can not do this right now."));
						return;
					}

					Location toTeleport = getRandomLocation();

					if (toTeleport == null)
					{
						clicker.sendMessage(F.main(_moduleName, "A suitable teleport location could not be found. Please try again in a few seconds."));
						return;
					}

					clicker.teleport(toTeleport);
				});
			}
		}
	}
	
	@EventHandler
	public void clearExp(PlayerCustomRespawnEvent event)
	{
		Player player = event.getPlayer();
		
		player.setLevel(0);
		player.setExp(0);
	}
	

	public void teleportToSpawn(Player player)
	{
		player.teleport(_spawn);
	}

	public boolean isSuitable(Block block)
	{
		Block up = block.getRelative(BlockFace.UP);
		Block down = block.getRelative(BlockFace.DOWN);

		if (block.getType() != Material.AIR || down.getType() == Material.AIR || down.getType() == Material.LEAVES || up.getType() == Material.LEAVES || UtilBlock.liquid(down) || UtilBlock.liquid(up) || UtilBlock.liquid(block) || _safezone.isInSafeZone(block.getLocation()) || block.getLocation().getBlockY() > MAX_SPAWNING_Y)
		{
			return false;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (_safezone.isInSafeZone(player.getLocation()))
			{
				continue;
			}
			
			if (UtilMath.offsetSquared(player.getLocation(), block.getLocation()) < MIN_PLAYER_DISTANCE_SQUARED)
			{
				return false;
			}
		}

		return true;
	}

	public Location getRandomLocation()
	{
		int attempts = 0;
		double range = WORLD_BORDER_RADIUS * 0.5;

		while (attempts < 100)
		{
			Location possible = UtilBlock.getHighest(_worldData.World, UtilAlg.getRandomLocation(_center, range)).getLocation();

			if (isSuitable(possible.getBlock()))
			{
				return possible.add(0, 1, 0);
			}

			attempts++;
		}

		return null;
	}

	public Location getCenter()
	{
		return _center;
	}

}
