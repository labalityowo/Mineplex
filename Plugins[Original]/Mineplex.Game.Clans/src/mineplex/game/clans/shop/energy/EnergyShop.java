package mineplex.game.clans.shop.energy;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanEnergyManager;
import mineplex.game.clans.clans.ClansManager;

public class EnergyShop extends ShopBase<ClanEnergyManager>
{
	public EnergyShop(ClanEnergyManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Energy Shop");
	}
	
	@Override
	protected ShopPageBase<ClanEnergyManager, ? extends ShopBase<ClanEnergyManager>> buildPagesFor(Player player)
	{
		return new EnergyPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
