package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengePlatform extends Challenge
{

	public ChallengePlatform(Quick game)
	{
		super(game, ChallengeType.PLATFORM);

		_timeout = TimeUnit.SECONDS.toMillis(10);
		_winConditions.setTimeoutWin(true);
	}

	@Override
	public void challengeSelect()
	{
		int size = _game.getArenaSize() - 3;
		Location center = UtilAlg.getRandomLocation(_game.getCenter(), size, 0, size);

		size = 2;

		for (Block block : UtilBlock.getInBoundingBox(center.clone().add(size, 0, size), center.clone().subtract(size, 0, size), false))
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.STAINED_CLAY, (byte) 14);
		}

		_game.getManager().runSyncLater(() ->
		{
			List<Location> corners = _game.getOrangePoints();
			UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false).forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR));
		}, 5 * 20);
	}

	@Override
	public void disable()
	{
	}
}
