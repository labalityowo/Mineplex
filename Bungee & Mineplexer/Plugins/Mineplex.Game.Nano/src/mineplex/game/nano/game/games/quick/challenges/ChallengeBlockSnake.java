package mineplex.game.nano.game.games.quick.challenges;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeBlockSnake extends Challenge
{

	private List<Block> _blocks;
	private Block _last;

	public ChallengeBlockSnake(Quick game)
	{
		super(game, ChallengeType.BLOCK_SNAKE);

		_timeout = TimeUnit.SECONDS.toMillis(90);
		_winConditions.setLastOne(true);
	}

	@Override
	public void challengeSelect()
	{
		List<Location> corners = _game.getOrangePoints();
		_blocks = UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false);
		_last = _game.getCenter().getBlock().getRelative(BlockFace.DOWN);

		PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false);
		_players.forEach(player -> player.addPotionEffect(effect));
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void updateSnake(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !UtilTime.elapsed(_startTime, 3000))
		{
			return;
		}

		List<Block> possible = new ArrayList<>(UtilBlock.horizontals.size());

		for (BlockFace face : UtilBlock.horizontals)
		{
			Block next = _last.getRelative(face);

			if (_blocks.contains(next))
			{
				possible.add(next);
			}
		}

		Block block = UtilAlg.Random(possible);

		if (block == null)
		{
			block = UtilAlg.Random(_blocks);

			if (block == null)
			{
				for (Player player : _game.getAlivePlayers())
				{
					completePlayer(player, false);
				}
				return;
			}
		}

		_blocks.remove(block);
		_last = block;

		Location location = block.getLocation();

		if (event.getTick() % 5 == 0)
		{
			location.getWorld().playSound(location, Sound.CHICKEN_EGG_POP, 1, 1);
		}

		MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 4);

		_game.getManager().runSyncLater(() ->
		{
			if (!isRunning())
			{
				return;
			}

			MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 14);
		}, 1);

		_game.getManager().runSyncLater(() ->
		{
			if (!isRunning())
			{
				return;
			}

			MapUtil.QuickChangeBlockAt(location, Material.AIR);
		}, 40);
	}
}
