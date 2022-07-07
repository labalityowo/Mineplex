package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.NumberTracker;

/**
 * A challenge based on mazes.
 * 
 * @deprecated
 */
public class ChallengeNavigationMaze extends Challenge implements NumberTracker
{
	private Map<Player, Long> _completionTime = new HashMap<>();

	public ChallengeNavigationMaze(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Nagivation Maze",
			"Go to the other side of the maze.");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int z = -9; z <= 12; z++)
		{
			spawns.add(getCenter().add(-15, 1, z)); 
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -18; x <= 21; x++)
		{
			for (int z = -12; z <= 15; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);

				if (z == -12 || z == 15)
				{
					for (int y = 1; y <= 3; y++)
					{
						Block relativeBlock = block.getRelative(0, y, 0);
						setBlock(relativeBlock, Material.STONE, (byte) 0);
						addBlock(relativeBlock);
					}
				}

				if (x > 15 || x < -12)
				{
					setBlock(block, Material.WOOL, (byte) (x < 0 ? 14 : 13));
				}
				else
				{
					setBlock(block, Material.STONE);

					Block relativeBlock = block.getRelative(0, 4, 0);

					if (relativeBlock.getX() == -13 || relativeBlock.getX() == 14)
					{
						setBlock(relativeBlock, Material.STONE, (byte) 0);
					}
					else
					{
						setBlock(relativeBlock, Material.STAINED_GLASS, (byte) 8);
					}

					addBlock(relativeBlock);
				}

				addBlock(block);
			}
		}

		ArrayList<Block> mazeBlocks = generateMaze();

		for (Block mazeBlock : mazeBlocks)
		{
			addBlock(mazeBlock);
		}

		//		for (int i = 0; i < 30; i++)
		//		{
		//			ArrayList<Block> mazeBlocks = generateMaze();
		//
		//			if (isMazeValid())
		//			{
		//				for (Block mazeBlock : mazeBlocks)
		//				{
		//					addBlock(mazeBlock);
		//				}
		//
		//				break;
		//			}
		//			else
		//			{
		//				System.out.print("Generated bad maze, trying again..");
		//
		//				for (Block mazeBlock : mazeBlocks)
		//				{
		//					mazeBlock.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);
		//				}
		//			}
		//		}
	}

	@Override
	public void onEnd()
	{
		_completionTime.clear();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		if (event.getTo().getY() >= getCenter().getY() + 1 && event.getTo().getX() > getCenter().getX() + 15)
		{
			setCompleted(player);
			_completionTime.put(player, System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (_completionTime.containsKey(player))
		{
			_completionTime.remove(player);
		}
	}

	private boolean isMazeValid()
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		ArrayList<Block> nextLoop = new ArrayList<Block>();

		nextLoop.add(getCenter().getBlock().getRelative(-15, 1, 0));

		blocks.addAll(nextLoop);

		while (!nextLoop.isEmpty())
		{
			Block block = nextLoop.remove(0);

			for (BlockFace face : new BlockFace[] { BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH })
			{
				Block b = block.getRelative(face);

				if (blocks.contains(b))
				{
					continue;
				}

				blocks.add(b);

				if (b.getType() == Material.STONE || b.getX() < getCenter().getX() + -14 || b.getZ() < getCenter().getZ() + -12 || b.getZ() > getCenter().getZ() + 15)
				{
					continue;
				}

				if (b.getX() >= getCenter().getX() + 15)
				{
					return true;
				}

				nextLoop.add(b);
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	private ArrayList<Block> generateMaze()
	{
		ArrayList<Block> blocks = new ArrayList<Block>();
		int[][] maze = new MazeGenerator(11, 10).getMaze();

		for (int x = 1; x < 11; x++)
		{
			for (int z = 1; z < 10; z++)
			{
				Block b = getCenter().getBlock().getRelative((x - 5) * 3, 1, (z - 5) * 3);

				for (int y = 0; y < 3; y++)
				{
					Block block = b.getRelative(0, y, 0);

					if (block.getType() == Material.STONE)
						continue;

					setBlock(block, Material.STONE);
					blocks.add(block);
				}

				if (x < 10 && (maze[x][z] & 8) == 0)
				{
					for (int i = 1; i <= 2; i++)
					{
						for (int y = 0; y < 3; y++)
						{
							Block block = b.getRelative(i, y, 0);

							if (block.getType() == Material.STONE)
								continue;

							setBlock(block, Material.STONE);
							blocks.add(block);
						}
					}

				}

				if ((maze[x][z] & 1) == 0)
				{
					for (int i = 1; i <= 2; i++)
					{
						for (int y = 0; y < 3; y++)
						{
							Block block = b.getRelative(0, y, i);

							if (block.getType() == Material.STONE)
								continue;

							setBlock(block, Material.STONE);
							blocks.add(block);
						}
					}

				}
			}
		}

		return blocks;
	}

	@Override
	public Number getData(Player player)
	{
		return _completionTime.get(player);
	}

	@Override
	public boolean hasData(Player player)
	{
		return _completionTime.containsKey(player);
	}
}
