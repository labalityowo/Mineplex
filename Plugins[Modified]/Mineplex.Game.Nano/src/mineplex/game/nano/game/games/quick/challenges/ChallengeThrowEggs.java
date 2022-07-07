package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeThrowEggs extends Challenge
{

	public ChallengeThrowEggs(Quick game)
	{
		super(game, ChallengeType.THROW_EGGS);
	}

	@Override
	public void challengeSelect()
	{
		ItemStack itemStack = new ItemStack(Material.EGG, 64);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStack);
		}
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void updateEggs(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		playerLoop: for (Player player : _game.getAlivePlayers())
		{
			for (ItemStack itemStack : player.getInventory().getContents())
			{
				if (itemStack != null && itemStack.getType() == Material.EGG)
				{
					continue playerLoop;
				}
			}

			completePlayer(player, false);
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		event.setCancelled(true);
	}
}
