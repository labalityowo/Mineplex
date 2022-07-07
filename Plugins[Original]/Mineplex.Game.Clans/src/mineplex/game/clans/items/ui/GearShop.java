package mineplex.game.clans.items.ui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.items.GearManager;

public class GearShop extends ShopBase<GearManager>
{
	public GearShop(final GearManager plugin, final CoreClientManager clientManager, final DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Customize New Gear");
	}
	
	@Override
	protected ShopPageBase<GearManager, ? extends ShopBase<GearManager>> buildPagesFor(final Player player)
	{
		return new GearPage(getPlugin(), this, getClientManager(), getDonationManager(), "Customize New Gear", player);
	}
}