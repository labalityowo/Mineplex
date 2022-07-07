package nautilus.game.arcade.game.games.cakewars.island;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;

public class CakeIslandModule extends CakeModule
{

	private static final long ISLAND_INITIAL_DELAY = TimeUnit.MINUTES.toMillis(2);
	private static final long ISLAND_DELAY = TimeUnit.SECONDS.toMillis(30);
	private static final long ISLAND_CRUMBLE_DELAY = TimeUnit.SECONDS.toMillis(30);
	private static final int MAX_ATTEMPTS = 30;
	private static final int MAX_XZ = 60;
	public static final String CHEST_TYPE = "Island";
	private static final int MAX_ISLANDS = 2;
	private static final String WARN_MESSAGE = F.main("Game", C.cRed + "Watch out! The island is crumbling!");

	private final Map<Location, CakeIsland> _islands;
	private final Set<Pair<Location, Pair<Material, Byte>>> _schematic;

	private Location _center;
	private long _lastIsland;

	public CakeIslandModule(CakeWars game)
	{
		super(game);

		_islands = new HashMap<>(MAX_ISLANDS);
		_schematic = new HashSet<>(200);
	}

	@Override
	public void cleanup()
	{
		_islands.clear();
		_schematic.clear();
	}

	@EventHandler
	public void recruit(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		_center = _game.WorldData.GetDataLocs("WHITE").get(0);

		List<Location> corners = _game.WorldData.GetCustomLocs("CUSTOM_ISLAND");
		List<Block> blocks = UtilBlock.getInBoundingBox(corners.get(0), corners.get(1));
		Location center = null;

		for (Block block : blocks)
		{
			if (block.getType() == Material.CHEST)
			{
				center = block.getLocation();
			}
		}

		if (center == null)
		{
			return;
		}

		for (Block block : blocks)
		{
			if (block.getType() == Material.AIR)
			{
				continue;
			}

			Location blockLocation = block.getLocation();

			_schematic.add(Pair.create(
					new Location(
							center.getWorld(),
							center.getX() - blockLocation.getX(),
							center.getY() - blockLocation.getY(),
							center.getZ() - blockLocation.getZ()
					), Pair.create(block.getType(), block.getData())));

			block.setType(Material.AIR);
		}
	}

	@EventHandler
	public void updateSpawn(UpdateEvent event)
	{
		if (
				event.getType() != UpdateType.SEC_05 ||
						!_game.IsLive() ||
						!UtilTime.elapsed(_game.GetStateTime(), ISLAND_INITIAL_DELAY) ||
						!UtilTime.elapsed(_lastIsland, ISLAND_DELAY)
				)
		{
			return;
		}

		_lastIsland = System.currentTimeMillis();

		if (_islands.size() >= MAX_ISLANDS)
		{
			return;
		}

		Location random = getRandomLocation();

		if (random == null)
		{
			return;
		}

		List<Location> blocks = new ArrayList<>(_schematic.size());

		_schematic.forEach(pair ->
		{
			Location block = random.clone().subtract(pair.getLeft());
			Pair<Material, Byte> blockData = pair.getRight();

			MapUtil.QuickChangeBlockAt(block, blockData.getLeft(), blockData.getRight());
			blocks.add(block);
		});

		_islands.put(random, new CakeIsland(blocks));
		MapUtil.QuickChangeBlockAt(random, Material.CHEST, (byte) (UtilMath.r(4) + 2));
		_game.getChestLootModule().addChestLocation(CHEST_TYPE, random);
		_game.Announce(F.main("Game", "A new " + F.greenElem("Island") + " has spawned! Find it and claim the loot!"));
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (UtilPlayer.isSpectator(player) || block == null)
		{
			return;
		}

		Location blockLocation = block.getLocation();

		_islands.forEach((location, island) ->
		{
			if (UtilMath.offsetSquared(blockLocation, location) < 4)
			{
				island.setChestOpen();
			}
		});
	}

	@EventHandler
	public void updateIslandCrumble(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_game.IsLive())
		{
			return;
		}

		_islands.forEach((location, island) ->
		{
			if (island.isCrumbing())
			{
				Location random = UtilAlg.Random(island.getBlocks());

				if (random == null)
				{
					return;
				}

				double r = Math.random();

				if (r > 0.85)
				{
					Block block = random.getBlock();
					random.getWorld().playEffect(random, Effect.STEP_SOUND, block.getType());

					if (r > 0.97)
					{
						FallingBlock fallingBlock = random.getWorld().spawnFallingBlock(random, block.getType(), block.getData());
						fallingBlock.setDropItem(false);
						fallingBlock.setHurtEntities(false);
					}
				}

				island.getBlocks().remove(random);
				MapUtil.QuickChangeBlockAt(random, Material.AIR);
			}
			else if (island.getChestOpen() > 0 && UtilTime.elapsed(island.getChestOpen(), ISLAND_CRUMBLE_DELAY))
			{
				for (Player player : _game.GetPlayers(true))
				{
					if (UtilMath.offsetSquared(location, player.getLocation()) < 300)
					{
						player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 0.6F);
						player.sendMessage(WARN_MESSAGE);
					}
				}

				island.setCrumbing(true);
			}
		});

		_islands.values().removeIf(island -> island.getBlocks().isEmpty());
	}

	private Location getRandomLocation()
	{
		int attempts = 0;
		List<Player> alive = _game.GetPlayers(true);

		attemptsLoop:
		while (attempts < MAX_ATTEMPTS)
		{
			attempts++;
			Location location = UtilAlg.getRandomLocation(_center, MAX_XZ, 0, MAX_XZ);
			Chunk chunk = location.getChunk();

			// Prevent islands spawning inside other blocks
			for (Block block : UtilBlock.getInRadius(location, 8).keySet())
			{
				if (block.getType() != Material.AIR)
				{
					continue attemptsLoop;
				}
			}

			// Prevent islands spawning in the same chunk as the player
			for (Player player : alive)
			{
				if (player.getLocation().getChunk().equals(chunk))
				{
					continue attemptsLoop;
				}
			}

			return location;
		}

		return null;
	}

}
