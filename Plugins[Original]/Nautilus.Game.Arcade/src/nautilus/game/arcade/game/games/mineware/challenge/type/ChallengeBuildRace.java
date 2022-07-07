package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.LogicTracker;
import nautilus.game.arcade.game.games.mineware.tracker.SpeedyBuildersTracker;

/**
 * A challenge based on fast block placement.
 */
public class ChallengeBuildRace extends Challenge implements LogicTracker
{
	private static final int MAP_SPAWN_SHIFT = 2;
	private static final int MAP_HEIGHT = 1;
	private static final double UNPLACEABLE_BLOCK_RADIUS_FROM_PLAYER = 1.5;
	private static final int BLOCK_TOWER_HEIGHT_LIMIT = 3;

	private static final int BLOCK_AMOUNT = 5;

	private static final Material[] MATERIALS = {
		Material.DIRT,
		Material.STONE,
		Material.COBBLESTONE,
		Material.LOG,
		Material.WOOD,
		Material.WOOL,
		Material.BRICK,
		Material.SMOOTH_BRICK,
		Material.GLASS
	};

	private List<Player> _speedTracker = new ArrayList<>();

	public ChallengeBuildRace(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Build Race",
			"Your inventory is filled with blocks.",
			"Place them all on the ground!");

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
		for (Location location : circle(getCenter(), getArenaSize(), 1, false, false, 0))
		{
			Block block = location.getBlock();
			setBlock(block, Material.GRASS);
			addBlock(location.getBlock());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart()
	{
		for (Material allowed : MATERIALS)
		{
			Host.BlockPlaceAllow.add(allowed.getId());
		}

		for (Player player : getPlayersAlive())
		{
			setupInventoryContents(player);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnd()
	{
		_speedTracker.clear();

		for (Material allowed : MATERIALS)
		{
			Host.BlockPlaceAllow.remove(allowed.getId());
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
		{
			event.setCancelled(true);
			return;
		}

		Block block = event.getBlock();

		Block bottomVoid1 = block.getRelative(BlockFace.DOWN);
		Block bottomVoid2 = bottomVoid1.getRelative(BlockFace.DOWN);
		Block bottomVoid3 = bottomVoid2.getRelative(BlockFace.DOWN);

		if (bottomVoid1.isEmpty() && bottomVoid2.isEmpty() && bottomVoid3.isEmpty())
		{
			alert(player, C.cRed + "You can't place blocks that far from the ground.");
			blockBreakEffect(block, true);
			event.setCancelled(true);
			return;
		}

		if (!Data.isModifiedBlock(block.getRelative(BlockFace.DOWN)))
		{
			event.setCancelled(true);
			return;
		}

		for (Player others : getPlayersAlive())
		{
			if (others.equals(player))
				continue;

			if (UtilMath.offset2d(block.getLocation(), others.getLocation()) < UNPLACEABLE_BLOCK_RADIUS_FROM_PLAYER)
			{
				alert(player, C.cRed + "You can't place blocks near other players.");
				blockBreakEffect(block, true);
				event.setCancelled(true);
				return;
			}
		}

		if (block.getLocation().getY() >= getCenter().getY() + BLOCK_TOWER_HEIGHT_LIMIT)
		{
			alert(player, C.cRed + "You can't build a tower that high.");
			blockBreakEffect(block, true);
			event.setCancelled(true);
			return;
		}

		addBlock(block);

		ArrayList<ItemStack> items = UtilInv.getItems(player);

		if ((items.size() - 1) == 0)
		{
			if (items.get(0).getAmount() == 1)
			{
				trackSpeed(player);
				setCompleted(player);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_speedTracker.remove(event.getPlayer());
	}

	private void trackSpeed(Player player)
	{
		long startTime = Settings.getStartTime();
		long finishTime = System.currentTimeMillis();

		if (finishTime <= startTime + SpeedyBuildersTracker.GOAL)
		{
			_speedTracker.add(player);
		}
	}

	private void setupInventoryContents(Player player)
	{
		ArrayList<Material> shuffledMaterials = new ArrayList<Material>(Arrays.asList(MATERIALS));
		Collections.shuffle(shuffledMaterials);

		for (Material material : shuffledMaterials)
		{
			ItemStack itemStack = new ItemStack(material, BLOCK_AMOUNT);
			player.getInventory().addItem(itemStack);
		}
	}

	@Override
	public boolean hasData(Player player)
	{
		return _speedTracker.contains(player);
	}
}
