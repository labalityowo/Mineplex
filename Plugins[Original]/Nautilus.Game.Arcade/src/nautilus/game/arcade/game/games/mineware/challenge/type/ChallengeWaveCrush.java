package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilMath;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based on waves of falling wool blocks that can damage players.
 */
public class ChallengeWaveCrush extends Challenge implements NumberTracker
{
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 1;
	private static final int MAP_SPAWN_HEIGHT = MAP_HEIGHT + 1;
	private static final int MAP_SPAWN_X = -15;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_PLATFORM_X_START = -16;
	private static final int MAP_PLATFORM_X_STOP = 13;

	private static final int FIRST_WAVE_DELAY = 60; // ticks
	private static final int NEXT_WAVE_DELAY = 30; // ticks
	private static final int WAVE_LOCATION_MULTIPLIER = 2;
	private static final int WAVE_LENGTH_MAX = 5;
	private static final float WAVE_BLOCK_HITBOX_GROW = 0.7F;
	private static final int WAVE_BLOCK_VELOCITY_Y = 10;
	private static final int WAVE_BLOCK_SPAWN_DELAY = 2; // ticks
	private static final int WAVE_DELAY_DECREMENT_CRITERIA = 3;
	private static final int COLOR_BLOCK_LENGTH = 2;
	private static final byte[] COLORS = { 0, 5, 4, 1, 6, 14, 11, 12, 10, 7 };

	private int _modifiedNextWaveDelay;
	private int _wavesPassed;
	private int _colorIndex;
	private int _colorCounter;
	private Map<Player, Integer> _survivedWaves = new HashMap<>();
	private int _arenaStartSize;

	public ChallengeWaveCrush(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Wave Crush",
			"Waves of blocks are coming towards you!",
			"Avoid getting hit by them.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int z = -size; z <= size; z++)
		{
			if (z % SPAWN_COORDINATE_MULTIPLE == 0)
			{
				spawns.add(getCenter().add(MAP_SPAWN_X, MAP_SPAWN_HEIGHT, z));
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		_arenaStartSize = getArenaSize();

		for (int x = MAP_PLATFORM_X_START; x <= MAP_PLATFORM_X_STOP; x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.BEDROCK);
					}
					else
					{
						setBlock(block, Material.WOOL, getColor());
					}

					addBlock(block);
				}
			}

			_colorCounter++;
		}
	}

	@Override
	public void onStart()
	{
		_modifiedNextWaveDelay = NEXT_WAVE_DELAY;

		initializeWaveTracker();
		startWavesTask();
	}

	@Override
	public void onEnd()
	{
		remove(EntityType.FALLING_BLOCK);
		removeExtraBlocks();

		_wavesPassed = 0;
		_survivedWaves.clear();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_survivedWaves.remove(event.getPlayer());
	}

	@Override
	public void onCollide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target instanceof Player && data.getThrown() instanceof FallingBlock)
		{
			Player player = (Player) target;

			if (isPlayerValid(player))
			{
				Host.Manager.GetDamage().NewDamageEvent(target, null, null, DamageCause.PROJECTILE, player.getHealth(), false, false, false, "Falling Block", "Wave Crush");
				subtractWaveCount(player);
			}
		}
	}

	private byte getColor()
	{
		if (_colorCounter > COLOR_BLOCK_LENGTH)
		{
			_colorCounter = 0;
			_colorIndex++;

			if (_colorIndex >= COLORS.length)
				_colorIndex = 0;
		}

		return COLORS[_colorIndex];
	}

	private void initializeWaveTracker()
	{
		for (Player player : getPlayersAlive())
		{
			_survivedWaves.put(player, 0);
		}
	}

	private void subtractWaveCount(Player player)
	{
		if (Data.isLost(player) && _survivedWaves.get(player) > 0)
		{
			_survivedWaves.put(player, _survivedWaves.get(player) - 1);
		}
	}

	private void startWavesTask()
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

				_wavesPassed++;

				startWave();
				decreaseWaveDelay();
				increaseSurvivedWaves();
			}
		}.runTaskTimer(Host.Manager.getPlugin(), FIRST_WAVE_DELAY, _modifiedNextWaveDelay);
	}

	private Block getWaveStartBlock()
	{
		Block block = getCenter().getBlock().getRelative(MAP_PLATFORM_X_STOP, 1, UtilMath.r(getArenaSize() * WAVE_LOCATION_MULTIPLIER) - getArenaSize());

		while (block.isEmpty())
		{
			block = getCenter().getBlock().getRelative(MAP_PLATFORM_X_STOP, 1, UtilMath.r(getArenaSize() * WAVE_LOCATION_MULTIPLIER) - getArenaSize());
		}

		return block;
	}

	private void startWave()
	{
		Block startBlock = getWaveStartBlock();

		for (int i = 0; i <= WAVE_LENGTH_MAX; i++)
		{
			createWaveBlock(startBlock.getLocation().clone().add(0, 0, i).getBlock());
		}
	}

	private void createWaveBlock(final Block block)
	{
		new BukkitRunnable()
		{
			Block currentBlock = block;

			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				if (!isChallengeValid() || currentBlock.isEmpty())
				{
					cancel();
					return;
				}

				Location spawn = currentBlock.getLocation().clone().add(0, 1, 0);
				FallingBlock waveBlock = getCenter().getWorld().spawnFallingBlock(spawn, currentBlock.getType(), currentBlock.getData());
				Host.Manager.GetProjectile().AddThrow(waveBlock, null, Host, -1, true, false, true, true, WAVE_BLOCK_HITBOX_GROW);
				waveBlock.setVelocity(new Vector(0, WAVE_BLOCK_VELOCITY_Y, 0).normalize());

				resetBlock(currentBlock);
				currentBlock = getCenter().getWorld().getBlockAt(currentBlock.getX() - 1, currentBlock.getY(), currentBlock.getZ());
			}
		}.runTaskTimer(Host.Manager.getPlugin(), 0, WAVE_BLOCK_SPAWN_DELAY);
	}

	private void decreaseWaveDelay()
	{
		if (_wavesPassed % WAVE_DELAY_DECREMENT_CRITERIA == 0 && _modifiedNextWaveDelay > 0)
		{
			_modifiedNextWaveDelay--;
		}
	}

	private void increaseSurvivedWaves()
	{
		for (Player player : _survivedWaves.keySet())
		{
			if (isPlayerValid(player))
			{
				_survivedWaves.put(player, _survivedWaves.get(player) + 1);
			}
		}
	}

	private void removeExtraBlocks()
	{
		for (int x = MAP_PLATFORM_X_START; x <= MAP_PLATFORM_X_STOP; x++)
		{
			for (int z = -_arenaStartSize; z <= _arenaStartSize; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, MAP_SPAWN_HEIGHT, z);

				if (!block.isEmpty())
				{
					resetBlock(block);
				}
			}
		}
	}

	@Override
	public Number getData(Player player)
	{
		return _survivedWaves.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _survivedWaves.containsKey(player);
	}
}
