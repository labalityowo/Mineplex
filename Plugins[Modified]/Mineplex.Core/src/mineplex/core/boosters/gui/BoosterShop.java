package mineplex.core.boosters.gui;

import mineplex.core.account.CoreClientManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * @author Shaun Bennett
 */
public class BoosterShop extends ShopBase<BoosterManager>
{
	public BoosterShop(BoosterManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Game Amplifiers");
	}

	@Override
	protected ShopPageBase<BoosterManager, ? extends ShopBase<BoosterManager>> buildPagesFor(Player player)
	{
		return new BoosterPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		getPlayerPageMap().values().stream().filter(value -> value instanceof BoosterPage).forEach(ShopPageBase::refresh);
	}
}
