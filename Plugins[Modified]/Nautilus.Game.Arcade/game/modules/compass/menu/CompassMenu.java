package nautilus.game.arcade.game.modules.compass.menu;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.compass.menu.page.CompassPage;

public class CompassMenu extends ShopBase<Game>
{
	private CompassModule _compassModule;

	public CompassMenu(CompassModule module)
	{
		super(module.getGame(), module.getGame().getArcadeManager().GetClients(), module.getGame().getArcadeManager().GetDonation(), "Spectate Menu");
		this._compassModule = module;
	}

	@Override
	protected ShopPageBase<Game, ? extends ShopBase<Game>> buildPagesFor(Player player)
	{
		return new CompassPage(this, _compassModule, player);
	}

	public void update()
	{
		for (ShopPageBase<Game, ? extends ShopBase<Game>> shopPage : getPlayerPageMap().values())
		{
			shopPage.refresh();
		}
	}

}