package nautilus.game.arcade.game.modules;

import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/*
 * This module will give all players specific items at the start of the game
 */
public class ItemGiverModule extends Module
{
	private final List<ItemStack> _itemsToGive = new ArrayList<>();

	public ItemGiverModule withItem(ItemStack item)
	{
		_itemsToGive.add(item.clone());
		return this;
	}

	public ItemGiverModule withItems(ItemStack... items)
	{
		List<ItemStack> clones = new ArrayList<>();
		for (ItemStack item : items)
		{
			clones.add(item.clone());
		}

		_itemsToGive.addAll(clones);
		return this;
	}


	@EventHandler
	public void on(GameStateChangeEvent event)
	{
		if(event.GetState() != Game.GameState.Live)
			return;

		for (Player player : getGame().GetPlayers(true))
		{
			for (ItemStack toGive : _itemsToGive)
			{
				UtilInv.insert(player, toGive.clone());
			}
		}
	}
}
