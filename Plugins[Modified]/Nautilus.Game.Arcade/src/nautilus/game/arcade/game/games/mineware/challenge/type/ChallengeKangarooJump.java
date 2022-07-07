package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;

/**
 * A challenge based on jumping.
 */
public class ChallengeKangarooJump extends Challenge
{
	private static final int DURATION_RANDOM_ADD = 5;
	private static final int DURATION_MIN = 20;
	private static final int DURATION_MULTIPLIER = 1000;

	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLE = 2;
	private static final int MAP_HEIGHT = 1;
	private static final int MAP_OBSTACLE_HEIGHT = 150;
	private static final byte DIRT_DATA = 2;

	private static final int OBSTACLE_START_HEIGHT = 2;
	private static final double OBSTACLE_SPAWN_CHANCE = 0.05;
	private static final int OBSTACLE_COLOR_RANGE = 16;

	private static final double JUMP_POWER = 2;
	private static final double JUMP_HEIGHT = 0.4;
	private static final double JUMP_HEIGHT_MAX = 4;

	private static final int TOTAL_HEIGHTS_DIVIDER = 2;

	public ChallengeKangarooJump(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Kangaroo Jump",
			"Jump from block to block.",
			"Player with the highest height wins!");

		Settings.setUseMapHeight();
		Settings.hideTimerRanOutMessage();
		Settings.setDuration((UtilMath.r(DURATION_RANDOM_ADD) + DURATION_MIN) * DURATION_MULTIPLIER);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -(size); x <= size; x++)
		{
			for (int z = -(size); z <= size; z++)
			{
				if (x % SPAWN_COORDINATE_MULTIPLE == 0 && z % SPAWN_COORDINATE_MULTIPLE == 0)
				{
					spawns.add(getCenter().add(x, MAP_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -getArenaSize(); x <= getArenaSize(); x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_OBSTACLE_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						setBlock(block, Material.DIRT, DIRT_DATA);
					}
					else if (y > OBSTACLE_START_HEIGHT)
					{
						if (Math.random() < OBSTACLE_SPAWN_CHANCE)
						{
							setBlock(block, Material.WOOL, (byte) UtilMath.r(OBSTACLE_COLOR_RANGE));
						}
					}
					else
					{
						if (Math.abs(x) == getArenaSize() || Math.abs(z) == getArenaSize())
						{
							setBlock(block, Material.FENCE);
						}
					}

					addBlock(block);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJump(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getFrom().getY() >= event.getTo().getY())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player) || !player.isOnGround())
			return;

		UtilAction.velocity(player, JUMP_POWER, JUMP_HEIGHT, JUMP_HEIGHT_MAX, true);
	}

	@Override
	public void onTimerFinish()
	{
		Map<Player, Integer> heights = new HashMap<>();

		for (Player player : getPlayersAlive())
		{
			heights.put(player, player.getLocation().getBlockY());
		}

		List<Player> sortedHeights = sortHash(heights);
		int indexMiddle = (int) sortedHeights.size() / TOTAL_HEIGHTS_DIVIDER - 1;

		for (int i = 0; i < (sortedHeights.size()); i++)
		{
			Player player = sortedHeights.get(i);

			if (isPlayerValid(player) && i <= indexMiddle)
			{
				setCompleted(player);
			}
		}
	}

	private ArrayList<Player> sortHash(final Map<Player, Integer> unsortedMap)
	{
		ArrayList<Player> players = new ArrayList<Player>(unsortedMap.keySet());

		Collections.sort(players, new Comparator<Player>()
		{
			@Override
			public int compare(Player player1, Player player2)
			{
				Integer height1 = unsortedMap.get(player1);
				Integer height2 = unsortedMap.get(player2);

				return height2.compareTo(height1);
			}
		});

		return players;
	}
}
