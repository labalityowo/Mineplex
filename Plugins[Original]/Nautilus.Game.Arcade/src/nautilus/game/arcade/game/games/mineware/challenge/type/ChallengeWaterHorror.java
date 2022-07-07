package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on water, boats and tnt.
 */
public class ChallengeWaterHorror extends Challenge
{
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_SPAWN_HEIGHT = 6;
	private static final int MAP_HEIGHT = MAP_SPAWN_HEIGHT + 1;

	private static final int TNT_SPAWN_MAX = 3;
	private static final int TNT_SPAWN_MIN = 1;
	private static final int TNT_EXPLODE_AFTER = 2; // seconds

	private static final int BEDROCK_LEVEL = 0;
	private static final int SAND_LEVEL = 1;
	private static final int MAIN_LEVEL = 5;

	private static final int DROPSITE_HEIGHT = 7;

	private static final float TNT_PARTICLE_OFFSET = 0.2F;
	private static final int TNT_PARTICLE_AMOUNT = 2;
	private static final float TNT_INCOMING_SOUND_VOLUME = 0.5F;
	private static final float TNT_INCOMING_SOUND_PITCH = 1.0F;

	private static final float SPAWNER_FLAME_OFFSET = 0.3F;
	private static final float SPAWNER_FLAME_SPEED = 0.03F;
	private static final int SPAWNER_FLAME_AMOUNT = 2;

	private static final float SPAWNER_CLOUD_OFFSET = 0.1F;
	private static final int SPAWNER_CLOUD_AMOUNT = 2;

	private static final float TNT_SPAWN_SOUND_VOLUME = 0.5F;
	private static final float TNT_SPAWN_SOUND_PITCH = 1.3F;
	private static final int TNT_SPAWN_INTERVAL = 2; // seconds

	private static final double TNT_VELOCITY_POWER_MIN = 0.2;
	private static final double TNT_VELOCITY_HEIGHT = 0.4;

	private Map<Player, Boat> _boats = new HashMap<>();
	private Location _dropsite;
	private Location _base;

	public ChallengeWaterHorror(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Water Horror",
			"TNT is spawning from the water!",
			"Use your boat to dodge explosions.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		int size = getArenaSize();
		int platform = size - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				double absX = Math.abs(x);
				double absZ = Math.abs(z);

				if ((absX == platform || absZ == platform) && !(absX > platform || absZ > platform))
				{
					spawns.add(getCenter().add(x, MAP_SPAWN_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		int size = getArenaSize();

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					double absX = Math.abs(x);
					double absZ = Math.abs(z);

					// Bottom Layer

					if (y == BEDROCK_LEVEL)
					{
						setBlock(block, Material.BEDROCK);
					}

					// Ground Layer

					else if (y == SAND_LEVEL)
					{
						setBlock(block, Material.SAND);
					}
					else
					{
						if (y <= MAIN_LEVEL)
						{
							// Container

							if (absX == size || absZ == size)
							{
								if (y == MAIN_LEVEL)
								{
									setBlock(block, Material.GRASS);
								}
								else
								{
									setBlock(block, Material.DIRT);
								}
							}

							// Water

							else if (absX < size || absZ < size)
							{
								if (y == MAIN_LEVEL)
								{
									setBlock(block, Material.ICE);
								}
								else
								{
									setBlock(block, Material.WATER);
								}
							}
						}
						else
						{
							// Fences

							if (absX == size || absZ == size)
							{
								setBlock(block, Material.FENCE);
							}
						}
					}

					addBlock(block);
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.getArcadeManager().GetExplosion().SetLiquidDamage(false);

		_dropsite = getCenter().add(0, DROPSITE_HEIGHT, 0);
		_base = _dropsite.clone().subtract(0, DROPSITE_HEIGHT, 0);

		for (Player player : getPlayersAlive())
		{
			Boat boat = player.getWorld().spawn(player.getLocation(), Boat.class);
			boat.setPassenger(player);

			_boats.put(player, boat);
		}

		startTNTSpawnTask();
		removeIce();
	}

	@Override
	public void onEnd()
	{
		Host.getArcadeManager().GetExplosion().SetLiquidDamage(true);

		remove(EntityType.BOAT);
		remove(EntityType.PRIMED_TNT);

		_boats.clear();
	}

	@EventHandler
	public void onUpdateEntityTrail(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Entity entity : Host.WorldData.World.getEntities())
		{
			if (entity instanceof TNTPrimed)
			{
				if (entity.isValid() && !entity.isOnGround())
				{
					UtilParticle.PlayParticleToAll(ParticleType.FLAME, entity.getLocation(), TNT_PARTICLE_OFFSET, TNT_PARTICLE_OFFSET, TNT_PARTICLE_OFFSET, 0.0F, TNT_PARTICLE_AMOUNT, ViewDist.LONG);

					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							entity.getWorld().playSound(entity.getLocation(), Sound.ORB_PICKUP, TNT_INCOMING_SOUND_VOLUME, TNT_INCOMING_SOUND_PITCH);
						}
					}.runTaskLater(Host.getArcadeManager().getPlugin(), TICK_MULTIPLIER);
				}

			}
		}
	}

	@EventHandler
	public void onUpdateSpawnerParticle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!isChallengeValid())
			return;

		UtilParticle.PlayParticleToAll(ParticleType.FLAME, _dropsite, SPAWNER_FLAME_OFFSET, SPAWNER_FLAME_OFFSET, SPAWNER_FLAME_OFFSET, SPAWNER_FLAME_SPEED, SPAWNER_FLAME_AMOUNT, ViewDist.LONG);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, _base, SPAWNER_CLOUD_OFFSET, SPAWNER_CLOUD_OFFSET, SPAWNER_CLOUD_OFFSET, 0.0F, SPAWNER_CLOUD_AMOUNT, ViewDist.LONG);
	}

	@EventHandler
	public void onEntityDismount(EntityDismountEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof Boat)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getVehicle() instanceof Boat)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onExplosion(ExplosionEvent event)
	{
		if (!isChallengeValid())
			return;

		event.GetBlocks().clear();
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getBlock().getType() == Material.ICE)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getEntity();

		if (!isPlayerValid(player))
			return;

		player.eject();

		Boat boat = _boats.get(player);

		boat.remove();
		_boats.remove(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		player.eject();

		Boat boat = _boats.get(player);

		boat.remove();
		_boats.remove(player);
	}

	private void startTNTSpawnTask()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isChallengeValid())
				{
					cancel();
					return;
				}

				ArrayList<Player> players = getPlayersAlive();
				int times = UtilMath.r(TNT_SPAWN_MAX) + TNT_SPAWN_MIN;

				if (times > players.size())
					times = players.size();

				for (int i = 0; i < times; i++)
				{
					Player target = UtilMath.randomElement(players);
					createExplosive(target, _dropsite);
				}

				_dropsite.getWorld().playSound(_dropsite, Sound.ZOMBIE_WOODBREAK, TNT_SPAWN_SOUND_VOLUME, TNT_SPAWN_SOUND_PITCH);
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), TNT_SPAWN_INTERVAL * TICK_MULTIPLIER, TNT_SPAWN_INTERVAL * TICK_MULTIPLIER);
	}

	private void createExplosive(Player player, Location dropsite)
	{
		Location target = player.getLocation();

		TNTPrimed explosive = dropsite.getWorld().spawn(dropsite, TNTPrimed.class);
		explosive.setFuseTicks(TNT_EXPLODE_AFTER * TICK_MULTIPLIER);

		double str = UtilAlg.calculateVelocity(dropsite.toVector(), target.toVector(), TNT_VELOCITY_HEIGHT).length() + TNT_VELOCITY_POWER_MIN;

		if (((dropsite.getX() - target.getX()) * (dropsite.getX() - target.getX()) + (dropsite.getY() - target.getY()) * (dropsite.getY() - target.getY())) < 16)
		{
			str = 0;
		}

		UtilAction.velocity(
			explosive,
			UtilAlg.getTrajectory2d(dropsite, target),
			str,
			true,
			0,
			TNT_VELOCITY_HEIGHT,
			TNT_VELOCITY_HEIGHT + 1,
			false);
	}

	private void removeIce()
	{
		int size = getArenaSize();

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, MAIN_LEVEL, z);

				if (block.getType() == Material.ICE)
				{
					setBlock(block, Material.WATER);
				}
			}
		}
	}
}
