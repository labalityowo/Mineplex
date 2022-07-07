package nautilus.game.pvp.modules.Benefit;

import org.bukkit.entity.Player;

import me.chiss.Core.Shopv2.ShopBase;
import me.chiss.Core.Shopv2.page.ShopPageBase;
import mineplex.core.CurrencyType;
import nautilus.game.pvp.modules.Benefit.UI.BenefitShopPage;

public class BenefitShop extends ShopBase<BenefitManager>
{
	public BenefitShop(BenefitManager plugin, String name, CurrencyType...currencyTypes) 
	{
		super(plugin, name, currencyTypes);
	}

	@Override
	protected ShopPageBase<BenefitManager, BenefitShop> BuildPagesFor(Player player)
	{
		// Always return first page you want opened.
		return new BenefitShopPage(Plugin, this, player);
	}
}
