package mineplex.game.nano.game.games.quick.challenges;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengePickASide extends Challenge
{

	private int _middleZ;

	public ChallengePickASide(Quick game)
	{
		super(game, ChallengeType.PICK_A_SIDE);
	}

	@Override
	public void challengeSelect()
	{
		_middleZ = _game.getMineplexWorld().getIronLocation("LIGHT_BLUE").getBlockZ();

		List<Location> corners = _game.getOrangePoints();
		UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false).forEach(block ->
		{
			int z = block.getZ();
			byte data = 0;

			if (z > _middleZ)
			{
				data = 14;
			}
			else if (z < _middleZ)
			{
				data = 11;
			}

			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.STAINED_CLAY, data);
		});
	}

	@Override
	public void disable()
	{

	}

	@Override
	protected void completeRemaining()
	{
		List<Player> total = _game.getAlivePlayers();
		List<Player> redSide = new ArrayList<>();

		for (Player player : total)
		{
			int z = player.getLocation().getBlockZ();

			if (z == _middleZ)
			{
				failPlayer(player, true);
			}
			else if (z > _middleZ)
			{
				redSide.add(player);
			}
		}

		int redPlayers = redSide.size(), bluePlayers = total.size() - redSide.size();

		if (redPlayers == bluePlayers)
		{
			for (Player player : total)
			{
				completePlayer(player, true);
			}
		}
		else
		{
			boolean containsWin = redPlayers < bluePlayers;

			for (Player player : total)
			{
				if ((containsWin && redSide.contains(player)) || (!containsWin && !redSide.contains(player)))
				{
					completePlayer(player, true);
				}
				else
				{
					failPlayer(player, true);
				}
			}
		}
	}
}
