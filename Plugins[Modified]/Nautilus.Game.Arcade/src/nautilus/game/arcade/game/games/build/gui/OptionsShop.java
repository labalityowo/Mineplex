package nautilus.game.arcade.game.games.build.gui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.build.gui.page.OptionsPage;

public class OptionsShop extends ShopBase<ArcadeManager>
{
	private Build _game;

	public OptionsShop(Build game, ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Options");
		_game = game;
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return new OptionsPage(_game, getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
