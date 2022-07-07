package mineplex.core.game.nano;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class NanoShop extends ShopBase<NanoFavourite>
{

	NanoShop(NanoFavourite plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), plugin.getName());
	}

	@Override
	protected ShopPageBase<NanoFavourite, ? extends ShopBase<NanoFavourite>> buildPagesFor(Player player)
	{
		return new NanoFavouritePage(getPlugin(), this, player);
	}
}
