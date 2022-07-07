package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.Pair;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeSlimeJump extends Challenge
{

	public ChallengeSlimeJump(Quick game)
	{
		super(game, ChallengeType.SLIME_JUMP);

		_pvp = true;
	}

	@Override
	public void challengeSelect()
	{
		List<Location> corners = _game.getOrangePoints();
		UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false).forEach(block ->
		{
			Location location = block.getLocation();

			MapUtil.QuickChangeBlockAt(location, Material.SLIME_BLOCK);
			MapUtil.QuickChangeBlockAt(location.clone().add(0, 12, 0), Material.BARRIER);

			if (Math.random() < 0.1)
			{
				MapUtil.QuickChangeBlockAt(location.clone().add(0, 7, 0), Material.EMERALD_BLOCK);
			}
		});

		ItemStack itemStack = new ItemStack(Material.STICK);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 7, false, false);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStack);
			player.addPotionEffect(jump);
		}
	}

	@Override
	public void disable()
	{
	}

	@Override
	protected void completeRemaining()
	{
		playerLoop: for (Player player : _game.getAlivePlayers())
		{
			Pair<Location, Location> box = UtilEnt.getSideStandingBox(player);
			Location min = box.getLeft(), max = box.getRight();

			for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
			{
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
				{
					if (player.getLocation().add(x, -0.5, z).getBlock().getType() == Material.EMERALD_BLOCK)
					{
						completePlayer(player, true);
						continue playerLoop;
					}
				}
			}

			failPlayer(player, true);
		}
	}
}
