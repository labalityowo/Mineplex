package mineplex.game.nano.game.components.compass;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.nano.NanoManager;

public class GameCompassShop extends ShopBase<GameCompassComponent>
{

	GameCompassShop(GameCompassComponent plugin, NanoManager manager)
	{
		super(plugin, manager.getClientManager(), manager.getDonationManager(), "Game Compass");
	}

	@Override
	protected ShopPageBase<GameCompassComponent, ? extends ShopBase<GameCompassComponent>> buildPagesFor(Player player)
	{
		return new GameCompassPage(getPlugin(), this, getClientManager(),getDonationManager(), player);
	}
}
