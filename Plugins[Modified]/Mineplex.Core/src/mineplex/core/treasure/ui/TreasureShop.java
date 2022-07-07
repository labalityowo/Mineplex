package mineplex.core.treasure.ui;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class TreasureShop extends ShopBase<TreasureManager>
{

	private final TreasureLocation _treasureLocation;

	public TreasureShop(TreasureManager plugin, CoreClientManager clientManager, DonationManager donationManager, TreasureLocation treasureLocation)
	{
		super(plugin, clientManager, donationManager, "Treasure Chest");

		_treasureLocation = treasureLocation;
	}

	@Override
	protected ShopPageBase<TreasureManager, ? extends ShopBase<TreasureManager>> buildPagesFor(Player player)
	{
		return new TreasurePage(getPlugin(), this, player, _treasureLocation);
	}
}
