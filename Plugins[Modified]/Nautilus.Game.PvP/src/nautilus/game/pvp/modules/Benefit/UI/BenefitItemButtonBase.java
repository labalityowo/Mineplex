package nautilus.game.pvp.modules.Benefit.UI;

import me.chiss.Core.Shop.IButton;

public abstract class BenefitItemButtonBase implements IButton
{
	protected BenefitShopPage Shop;
	
	public BenefitItemButtonBase(BenefitShopPage shop)
	{
		Shop = shop;
	}
}
