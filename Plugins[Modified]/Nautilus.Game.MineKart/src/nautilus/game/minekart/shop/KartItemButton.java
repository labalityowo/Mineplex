package nautilus.game.minekart.shop;

import mineplex.core.shop.item.IButton;
import nautilus.game.minekart.shop.page.KartPage;

import org.bukkit.entity.Player;

public class KartItemButton implements IButton
{
	private KartPage _shop;
	private KartItem _kartItem;
	
	public KartItemButton(KartPage shop, KartItem kartItem)
	{
		_shop = shop;
		_kartItem = kartItem;
	}

	@Override
	public void Clicked(Player player)
	{
		_shop.PurchaseKart(player, _kartItem);
	}
}
