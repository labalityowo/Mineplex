package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on falling anvil blocks.
 */
public class ChallengeAnvilDance extends Challenge
{
	private static final int MAP_HEIGHT = 1;
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int SMOOTH_BRICK_DATA_RANGE = 3;

	private static final int WAVE_HEIGHT = 13;
	private static final int WAVE_HEIGHT_RANDOM = 3;
	private static final int ANVIL_SPAWN_START_DELAY = 1;
	private static final int ANVIL_DATA_RANGE = 3;
	private static final float ANVIL_HITBOX_GROW = 0.7F;

	private static final int DANCE_TIMER = 10;
	private static final int DANCE_STOP = DANCE_TIMER / 2;
	private static final int DANCE_DELAY_START = 5;
	private static final float DANCE_SOUND_PITCH = 1.3F;

	private static final double DANCING_ANVIL_RANDOM_VELOCITY_X = 0.2;
	private static final double DANCING_ANVIL_RANDOM_VELOCITY_Y = 2.0;
	private static final double DANCING_ANVIL_RANDOM_VELOCITY_Z = 0.2;

	private static final int FIREWORK_HEIGHT = 8;
	private static final int FIREWORK_HEIGHT_INCREMENT = 4;
	private static final int FIREWORK_COLOR_DATA_RANGE = 255;
	private static final int FIREWORK_LAUNCH_LIMIT = 10;
	private static final int FIREWORK_LAUNCH_TICK_TIMER = 5;

	private int _arenaStartSize;
	private boolean _paused;
	private Set<Block> _landedAnvils = new HashSet<>();
	private Set<FallingBlock> _fallingAnvils = new HashSet<>();
	private BukkitTask _fireworkTask;

	public ChallengeAnvilDance(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Anvil Dance",
			"It's raining anvils!",
			"Dodge them to stay alive.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (Location location : circle(getCenter(), size, 1, true, false, 0))
		{
			spawns.add(location.add(0, MAP_HEIGHT, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		_arenaStartSize = getArenaSize();

		for (Location location : circle(getCenter(), _arenaStartSize, 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.SMOOTH_BRICK);

			if (UtilMath.random.nextBoolean())
			{
				setData(block, (byte) (UtilMath.r(SMOOTH_BRICK_DATA_RANGE)));
			}

			addBlock(location.getBlock());
		}
	}

	@Override
	public void onStart()
	{
		checkInvalidFallingBlocksTask();
		startFallingTask();
		startDancingTask();
	}

	@Override
	public void onEnd()
	{
		if (_fireworkTask != null)
		{
			_fireworkTask.cancel();
			_fireworkTask = null;
		}

		_paused = false;

		for (Block block : _landedAnvils)
		{
			resetBlock(block);
		}

		remove(EntityType.FALLING_BLOCK);
		remove(EntityType.DROPPED_ITEM);

		_landedAnvils.clear();
		_fallingAnvils.clear();
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if (!isChallengeValid())
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityChangeBlockEvent(final EntityChangeBlockEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof FallingBlock)
		{
			Block block = event.getBlock();

			if (!_landedAnvils.contains(block))
			{
				_fallingAnvils.remove(event.getEntity());
				_landedAnvils.add(block);
			}
		}
	}

	private void checkInvalidFallingBlocksTask()
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

				Iterator<FallingBlock> blocks = _fallingAnvils.iterator();

				while (blocks.hasNext())
				{
					FallingBlock block = blocks.next();

					if (!block.isValid())
					{
						blocks.remove();
					}
				}
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, 1L);
	}

	private void startFallingTask()
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

				if (!_paused)
				{
					createAnvil();
				}
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), ANVIL_SPAWN_START_DELAY * TICK_MULTIPLIER, 1L);
	}

	private void startDancingTask()
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

				_paused = true;

				playFireworksTask();

				for (Player player : getPlayersAlive())
				{
					player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1.0F, DANCE_SOUND_PITCH);
				}

				Iterator<Block> anvils = _landedAnvils.iterator();

				while (anvils.hasNext())
				{
					Block anvil = anvils.next();

					if (UtilMath.random.nextBoolean())
					{
						createDancingAnvil(anvil);
					}

					resetBlock(anvil);
					anvils.remove();
				}

				for (FallingBlock block : _fallingAnvils)
				{
					createDancingAnvil(block);
				}

				startUnpauseTask();
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), DANCE_DELAY_START * TICK_MULTIPLIER, DANCE_TIMER * TICK_MULTIPLIER);
	}

	@SuppressWarnings("deprecation")
	private void createAnvil()
	{
		Location center = getCenter().add(0, UtilMath.r(WAVE_HEIGHT_RANDOM) + WAVE_HEIGHT, 0);
		List<Location> locations = circle(center, _arenaStartSize, 1, false, false, 0);

		Location random = locations.get(UtilMath.r(locations.size()));

		World world = random.getWorld();
		FallingBlock block = world.spawnFallingBlock(random, Material.ANVIL, (byte) UtilMath.r(ANVIL_DATA_RANGE));
		block.setDropItem(false);

		_fallingAnvils.add(block);
		Host.Manager.GetProjectile().AddThrow(block, null, Host, -1, true, false, false, true, ANVIL_HITBOX_GROW);
	}

	@SuppressWarnings("deprecation")
	private void createDancingAnvil(Block block)
	{
		World world = block.getWorld();

		FallingBlock dancingBlock = world.spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
		dancingBlock.setDropItem(false);
		dancingBlock.setVelocity(getRandomVelocity());

		_fallingAnvils.add(dancingBlock);
		Host.Manager.GetProjectile().AddThrow(dancingBlock, null, Host, -1, true, false, false, true, ANVIL_HITBOX_GROW);
	}

	private void createDancingAnvil(FallingBlock block)
	{
		block.setVelocity(getRandomVelocity());
	}

	private void startUnpauseTask()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				_paused = false;
			}
		}.runTaskLater(Host.getArcadeManager().getPlugin(), DANCE_STOP * TICK_MULTIPLIER);
	}

	private void playFireworksTask()
	{
		_fireworkTask = new BukkitRunnable()
		{
			int height = FIREWORK_HEIGHT;
			int times = 0;

			@Override
			public void run()
			{
				if (!isChallengeValid())
				{
					cancel();
					return;
				}

				times++;

				if (times <= FIREWORK_LAUNCH_LIMIT)
				{
					Location spawn = getCenter().add(0, height, 0);
					UtilFirework.playFirework(spawn, Type.BALL_LARGE, Color.fromBGR(UtilMath.r(FIREWORK_COLOR_DATA_RANGE), UtilMath.r(FIREWORK_COLOR_DATA_RANGE), UtilMath.r(FIREWORK_COLOR_DATA_RANGE)), false, true);
				}

				height += FIREWORK_HEIGHT_INCREMENT;
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, FIREWORK_LAUNCH_TICK_TIMER);
	}

	private Vector getRandomVelocity()
	{
		return new Vector(
			UtilMath.rr(DANCING_ANVIL_RANDOM_VELOCITY_X, true),
			UtilMath.rr(DANCING_ANVIL_RANDOM_VELOCITY_Y, false) + 1,
			UtilMath.rr(DANCING_ANVIL_RANDOM_VELOCITY_Z, true));
	}

	@Override
	public void onCollide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target instanceof Player && data.getThrown() instanceof FallingBlock)
		{
			Player player = (Player) target;

			if (!isPlayerValid(player))
				return;

			player.damage(player.getHealth());
		}
	}
}
