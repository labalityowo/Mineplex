package mineplex.core.progression.data;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.progression.KitProgressionManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

/*
 * This is currently just used to open a confirmation menu and no other references should be made to this.
 */
public class KitUpgradeShop extends ShopBase<KitProgressionManager>
{

	public KitUpgradeShop(KitProgressionManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Kit Upgrade");
	}

	@Override
	protected ShopPageBase<KitProgressionManager, ? extends ShopBase<KitProgressionManager>> buildPagesFor(Player player)
	{
		return null;
	}

}
