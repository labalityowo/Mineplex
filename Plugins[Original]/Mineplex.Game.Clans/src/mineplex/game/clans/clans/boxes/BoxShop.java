package mineplex.game.clans.clans.boxes;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class BoxShop extends ShopBase<BoxManager>
{
	public BoxShop(BoxManager plugin)
	{
		super(plugin, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), "Boxes");
	}

	@Override
	protected ShopPageBase<BoxManager, ? extends ShopBase<BoxManager>> buildPagesFor(Player player)
	{
		return new DyeBoxPage(getPlugin(), this, "Dye Boxes", player);
	}
}