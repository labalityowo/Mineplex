package nautilus.game.arcade.game.games.mineware.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAlg;

/**
 * This class contains a list of collection fields to store challenge data.
 */
public class ChallengeData
{
	private List<Location> _spawns = new ArrayList<>();
	private Set<Player> _invisible = new HashSet<>();
	private Set<Block> _modifiedBlocks = new LinkedHashSet<>();
	private Set<Player> _completed = new HashSet<>();
	private Set<Player> _lost = new HashSet<>();

	public void reset()
	{
		_spawns.clear();
		_invisible.clear();
		_modifiedBlocks.clear();
		_completed.clear();
		_lost.clear();
	}

	public void setSpawns(ArrayList<Location> spawns)
	{
		_spawns = spawns;
	}

	public List<Location> getDefinedSpawns()
	{
		return _spawns;
	}

	public boolean isSpawnLocation(Location location)
	{
		for (Location spawn : _spawns)
		{
			if ((int) spawn.getX() == (int) location.getX() && (int) spawn.getY() == (int) location.getY() && (int) spawn.getZ() == (int) location.getZ())
			{
				return true;
			}
		}

		return false;
	}

	public boolean isNearSpawnLocation(Location location)
	{
		for (Location spawn : _spawns)
		{
			Block spawnBlock = spawn.getBlock();

			Block[] near = {
				spawnBlock.getRelative(BlockFace.NORTH),
				spawnBlock.getRelative(BlockFace.EAST),
				spawnBlock.getRelative(BlockFace.SOUTH),
				spawnBlock.getRelative(BlockFace.WEST),
				spawnBlock.getRelative(BlockFace.NORTH_EAST),
				spawnBlock.getRelative(BlockFace.NORTH_WEST),
				spawnBlock.getRelative(BlockFace.SOUTH_EAST),
				spawnBlock.getRelative(BlockFace.SOUTH_WEST)
			};

			for (Block block : near)
			{
				if (UtilAlg.isSimilar(block.getLocation(), location))
				{
					return true;
				}
			}
		}

		return false;
	}

	public void removePlayer(Player player)
	{
		_lost.remove(player);
		_completed.remove(player);
		_invisible.remove(player);
	}

	public boolean isCompleted(Player player)
	{
		return _completed.contains(player);
	}

	public boolean isDone(Player player)
	{
		return _completed.contains(player) || _lost.contains(player);
	}

	public boolean isLost(Player player)
	{
		return _lost.contains(player);
	}

	public void addInvisiblePlayer(Player player)
	{
		_invisible.add(player);
	}

	public Set<Player> getInvisiblePlayers()
	{
		return _invisible;
	}

	public boolean hasInvisiblePlayers()
	{
		return !_invisible.isEmpty();
	}

	public void addModifiedBlock(Block block)
	{
		_modifiedBlocks.add(block);
	}

	public boolean isModifiedBlock(Block block)
	{
		return _modifiedBlocks.contains(block);
	}

	public void removeModifiedBlock(Block block)
	{
		_modifiedBlocks.remove(block);
	}

	public Set<Block> getModifiedBlocks()
	{
		return _modifiedBlocks;
	}

	public void addCompletedPlayer(Player player)
	{
		_completed.add(player);
	}

	public Set<Player> getCompletedPlayers()
	{
		return _completed;
	}

	public boolean hasAnyoneCompleted()
	{
		return !_completed.isEmpty();
	}

	public void addLostPlayer(Player player)
	{
		_lost.add(player);
	}

	public Set<Player> getLostPlayers()
	{
		return _lost;
	}
}
