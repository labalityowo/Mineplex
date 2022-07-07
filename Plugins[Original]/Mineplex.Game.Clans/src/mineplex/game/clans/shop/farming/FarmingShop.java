package mineplex.game.clans.shop.farming;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class FarmingShop extends ShopBase<ClansManager>
{
	public FarmingShop(ClansManager plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Organic Produce");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new FarmingPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
