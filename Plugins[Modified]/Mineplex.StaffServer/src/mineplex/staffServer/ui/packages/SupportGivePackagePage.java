package mineplex.staffServer.ui.packages;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;
import mineplex.staffServer.ui.item.SupportGiveItemPage;

public class SupportGivePackagePage extends SupportGiveItemPage
{
	private ItemBuilder _builder;
	private String _salesPackage;

	public SupportGivePackagePage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage, ItemBuilder item, String salesPackage)
	{
		super(plugin, shop, player, target, previousPage);

		_builder = item;
		_salesPackage = salesPackage;

		buildPage();
	}

	@Override
	protected String getItemName()
	{
		return _salesPackage;
	}

	@Override
	protected ItemStack buildItemIcon()
	{
		return _builder
				// 1 - 64
				.setAmount(Math.min(Math.max(_count, 1), 64))
				.setLore(
						C.cYellow + _target.getName() + C.mBody + " will receive " + C.cYellow + _count,
						C.cYellow + getItemName() + C.mBody + "."
				)
				.build();
	}
}
