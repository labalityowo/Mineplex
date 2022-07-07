package mineplex.core.game.kit.ui;

import org.bukkit.entity.Player;

import mineplex.core.game.MineplexGameManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class KitShop extends ShopBase<MineplexGameManager>
{

	public KitShop(MineplexGameManager plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), "Kit");
	}

	@Override
	protected ShopPageBase<MineplexGameManager, ? extends ShopBase<MineplexGameManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
