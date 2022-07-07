package nautilus.game.arcade.game.games.cakewars.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.arcade.ArcadeManager;

public class CakeResourceShop extends ShopBase<ArcadeManager>
{

	public CakeResourceShop(ArcadeManager plugin)
	{
		super(plugin, plugin.GetClients(), plugin.GetDonation(), "Cake Wars Shop");
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (isPlayerInShop(event.getWhoClicked()))
		{
			event.setCancelled(true);
		}
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
