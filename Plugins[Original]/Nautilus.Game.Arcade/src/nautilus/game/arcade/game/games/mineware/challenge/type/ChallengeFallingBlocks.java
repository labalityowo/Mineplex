package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based on falling blocks.
 */
public class ChallengeFallingBlocks extends Challenge implements NumberTracker
{
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 1;
	private static final byte STONE_DATA = 5;

	private static final double SPAWN_CHANCE = 0.2;
	private static final double SPAWN_CHANCE_INCREMENT = 0.05;
	private static final double SPAWN_HEIGHT = 13.0;
	private static final int NEXT_WAVE = 5;
	private static final float WAVE_SOUND_VOLUME = 1.0F;
	private static final float WAVE_SOUND_PITCH = 1.5F;
	private static final double SPAWN_CHANCE_MAX = 100;

	private static final int PLAYER_CAMP_MAX_HEIGHT = 3;
	private static final byte ADDITIONAL_BLOCK_DATA = 2;
	private static final float BLOCK_HITBOX_GROW = 0.6F;

	private static final Material[] MATERIALS = {
		Material.GRASS,
		Material.DIRT,
		Material.STONE,
		Material.LOG,
		Material.WOOD,
		Material.COBBLESTONE,
		Material.GRAVEL,
		Material.COAL_ORE,
		Material.IRON_ORE,
		Material.HAY_BLOCK,
		Material.JUKEBOX,
		Material.SMOOTH_BRICK,
		Material.EMERALD_ORE,
		Material.FURNACE };

	private static final Material[] FLOOR = { Material.GRASS, Material.DIRT, Material.STONE, Material.COBBLESTONE };
	private static final Sound[] SOUNDS = { Sound.DIG_GRASS, Sound.DIG_GRAVEL, Sound.DIG_SAND, Sound.DIG_SNOW, Sound.DIG_STONE, Sound.DIG_WOOD, Sound.DIG_WOOL };

	private int _modifiedNextWave;
	private double _modifiedSpawnChance;
	private int _wavesCompleted;
	private Set<Block> _remaining = new HashSet<>();
	private int _arenaStartSize;
	private Map<Player, Integer> _waveTracker = new HashMap<>();
	private boolean _trackedWave = false;

	public ChallengeFallingBlocks(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Falling Blocks",
			"Blocks are falling from the sky!",
			"Try to avoid getting hit.");

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
			Material material = UtilMath.randomElement(FLOOR);
			Block block = location.getBlock();
			setBlock(block, material);

			if (material == Material.STONE)
			{
				if (UtilMath.random.nextBoolean())
				{
					setData(block, STONE_DATA);
				}
			}

			addBlock(location.getBlock());
		}
	}

	@Override
	public void onStart()
	{
		_modifiedNextWave = NEXT_WAVE;
		_modifiedSpawnChance = SPAWN_CHANCE;

		initializeWaveTracker();
		startWavesTask();
		startWaveTimerTask();
	}

	@Override
	public void onEnd()
	{
		_wavesCompleted = 0;
		_trackedWave = false;
		_arenaStartSize = 0;

		for (Block block : _remaining)
		{
			resetBlock(block);
		}

		_remaining.clear();

		for (Entity entity : Host.WorldData.World.getEntities())
		{
			if (entity instanceof FallingBlock)
			{
				entity.remove();
			}
		}

		_waveTracker.clear();
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityChangeBlockEvent(final EntityChangeBlockEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getEntity() instanceof FallingBlock)
		{
			final Block block = event.getBlock();
			_remaining.add(block);

			if (!_trackedWave)
			{
				_trackedWave = true;

				for (Player player : getPlayersAlive())
				{
					_waveTracker.put(player, _waveTracker.get(player) + 1);
				}
			}

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

					UtilParticle.PlayParticle(ParticleType.BLOCK_CRACK.getParticle(block.getType(), 0), block.getLocation(), 0.3F, 0.3F, 0.3F, 0.0F, 3, ViewDist.LONG, UtilServer.getPlayers());

					resetBlock(block);
					_remaining.remove(block);
				}
			}.runTaskLater(Host.getArcadeManager().getPlugin(), 40L);
		}
	}

	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getNewState().getType() == Material.GRASS)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_waveTracker.remove(event.getPlayer());
	}

	private void initializeWaveTracker()
	{
		for (Player player : getPlayersAlive())
		{
			_waveTracker.put(player, 0);
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

				createWave();
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), NEXT_WAVE * TICK_MULTIPLIER, NEXT_WAVE * TICK_MULTIPLIER);
	}

	private void startWaveTimerTask()
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

				if (_modifiedNextWave == 0)
				{
					_modifiedNextWave = NEXT_WAVE;
				}

				int wave = _wavesCompleted + 1;
				String time = C.cWhite + C.Bold + _modifiedNextWave;

				if (wave > 1)
				{
					UtilTextBottom.display(C.cYellow + C.Bold + "Next Wave: " + time, UtilServer.getPlayers());
				}
				else
				{
					UtilTextBottom.display(C.cYellow + C.Bold + "First Wave: " + time, UtilServer.getPlayers());
				}

				_modifiedNextWave--;
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, TICK_MULTIPLIER);
	}
	

	private void createWave()
	{
		_trackedWave = false;

		if (_modifiedSpawnChance < SPAWN_CHANCE_MAX)
			_modifiedSpawnChance += SPAWN_CHANCE_INCREMENT;

		_wavesCompleted++;

		Sound nextSound = UtilMath.randomElement(SOUNDS);

		for (Player player : getPlayersAlive())
		{
			player.playSound(player.getLocation(), nextSound, WAVE_SOUND_VOLUME, WAVE_SOUND_PITCH);
		}

		Location center = getCenter().add(0, SPAWN_HEIGHT, 0);

		for (Location location : UtilShapes.getCircle(center, false, _arenaStartSize))
		{
			if (Math.random() <= _modifiedSpawnChance)
			{
				createFallingBlock(location);
			}
		}

		for (Player player : getPlayersAlive())
		{
			Location camp = player.getLocation();

			if (camp.getY() >= getCenter().getY() + 1 && camp.getY() <= getCenter().getY() + PLAYER_CAMP_MAX_HEIGHT)
			{
				createFallingBlock(new Location(Host.WorldData.World, camp.getX(), getCenter().getY() + SPAWN_HEIGHT, camp.getZ()));
			}
		}
	}
	


	@SuppressWarnings("deprecation")
	private void createFallingBlock(Location location)
	{
		Material material = UtilMath.randomElement(MATERIALS);

		World world = location.getWorld();
		FallingBlock block = world.spawnFallingBlock(location, material, (byte) 0);
		block.setDropItem(false);

		if ((material == Material.SMOOTH_BRICK || material == Material.DIRT) && UtilMath.random.nextBoolean())
		{
			block = world.spawnFallingBlock(location, material, ADDITIONAL_BLOCK_DATA);
		}

		Host.Manager.GetProjectile().AddThrow(block, null, Host, -1, true, false, false, true, BLOCK_HITBOX_GROW);
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

	@Override
	public Number getData(Player player)
	{
		return _waveTracker.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _waveTracker.containsKey(player);
	}
}
