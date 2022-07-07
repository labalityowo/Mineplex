package mineplex.game.clans.clans.supplydrop;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class SupplyDropShop extends ShopBase<SupplyDropManager>
{
	public SupplyDropShop(SupplyDropManager plugin)
	{
		super(plugin, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), "Supply Drop");
	}

	@Override
	protected ShopPageBase<SupplyDropManager, ? extends ShopBase<SupplyDropManager>> buildPagesFor(Player player)
	{
		return new SupplyDropPage(getPlugin(), this, "Supply Drops", player);
	}
}