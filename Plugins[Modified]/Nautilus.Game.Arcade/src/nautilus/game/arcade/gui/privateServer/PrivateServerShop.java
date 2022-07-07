package nautilus.game.arcade.gui.privateServer;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.page.MenuPage;

public class PrivateServerShop extends ShopBase<ArcadeManager>
{
	public PrivateServerShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Private Server Menu");
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return new MenuPage(getPlugin(), this, player);
	}
}
