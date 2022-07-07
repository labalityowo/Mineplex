package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeFood extends Challenge
{

	public ChallengeFood(Quick game)
	{
		super(game, ChallengeType.FOOD);
	}

	@Override
	public void challengeSelect()
	{
		ItemStack[] itemStacks =
				{
						new ItemStack(Material.COOKED_BEEF),
						new ItemStack(Material.COOKED_CHICKEN),
						new ItemStack(Material.COOKED_FISH),
						new ItemStack(Material.GOLDEN_APPLE),
						new ItemStack(Material.RAW_CHICKEN),
						new ItemStack(Material.SPIDER_EYE),
						new ItemStack(Material.APPLE),
						new ItemStack(Material.RAW_FISH),
				};
		UtilAlg.shuffle(itemStacks);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStacks);
			player.setFoodLevel(2);
		}

		_timeout = TimeUnit.SECONDS.toMillis(8);
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void updateComplete(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : _game.getAlivePlayers())
		{
			if (player.getFoodLevel() == 20)
			{
				completePlayer(player, true);
			}
		}
	}
}
