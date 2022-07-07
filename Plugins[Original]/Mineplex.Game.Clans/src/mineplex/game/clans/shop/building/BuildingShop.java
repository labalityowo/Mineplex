package mineplex.game.clans.shop.building;

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

public class BuildingShop extends ShopBase<ClansManager>
{
	public BuildingShop(ClansManager plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Building Materials");
	}

	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new BuildingPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
