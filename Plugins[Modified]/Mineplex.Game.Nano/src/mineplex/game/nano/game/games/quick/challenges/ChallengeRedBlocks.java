package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.Pair;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeRedBlocks extends Challenge
{

	private List<Block> _blocks;

	public ChallengeRedBlocks(Quick game)
	{
		super(game, ChallengeType.RED_BLOCKS);

		_timeout = TimeUnit.MINUTES.toMillis(2);
		_winConditions.setLastOne(true);
	}

	@Override
	public void challengeSelect()
	{
		List<Location> corners = _game.getOrangePoints();

		_blocks = UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false);
		_blocks.forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.WOOL));
	}

	@Override
	public void disable()
	{
		_blocks.clear();
	}

	@EventHandler
	public void updateFloor(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !UtilTime.elapsed(_startTime, 1000))
		{
			return;
		}

		for (int i = 0; i < 4; i++)
		{
			Block block = UtilAlg.Random(_blocks);

			if (block == null)
			{
				return;
			}

			tickBlock(block);
		}
	}

	@EventHandler
	public void updatePlayers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC)
		{
			return;
		}

		for (Player player : _players)
		{
			Pair<Location, Location> box = UtilEnt.getSideStandingBox(player);
			Location min = box.getLeft(), max = box.getRight();

			for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			{
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				{
					tickBlock(player.getLocation().subtract(x, 0.5, z).getBlock());
				}
			}
		}
	}

	private void tickBlock(Block block)
	{
		if (block.getType() == Material.AIR)
		{
			return;
		}

		byte newData = 0;

		switch (block.getData())
		{
			case 0:
				newData = 4;
				break;
			case 4:
				newData = 1;
				break;
			case 1:
				newData = 14;
				break;
			case 14:
				_blocks.remove(block);
				MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
				return;
		}

		MapUtil.QuickChangeBlockAt(block.getLocation(), Material.WOOL, newData);
	}
}
