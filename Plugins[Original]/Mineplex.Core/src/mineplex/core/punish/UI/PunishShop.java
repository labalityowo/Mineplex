package mineplex.core.punish.UI;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.donation.DonationManager;
import mineplex.core.punish.Punish;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class PunishShop extends ShopBase<Punish>
{
	public PunishShop(Punish plugin)
	{
		super(plugin, plugin.GetClients(), Managers.require(DonationManager.class), "Punish");
	}

	@Override
	protected ShopPageBase<Punish, ? extends ShopBase<Punish>> buildPagesFor(Player player)
	{
		return null;
	}
}
