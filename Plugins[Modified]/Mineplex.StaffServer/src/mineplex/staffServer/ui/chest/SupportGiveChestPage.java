package mineplex.staffServer.ui.chest;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.treasure.types.TreasureType;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportShop;
import mineplex.staffServer.ui.item.SupportGiveItemPage;

public class SupportGiveChestPage extends SupportGiveItemPage
{
	private TreasureType _treasureType;

	public SupportGiveChestPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportChestPage chestPage, TreasureType treasureType)
	{
		super(plugin, shop, player, target, chestPage);

		_treasureType = treasureType;
		_lowerBound = -100;
		_upperBound = 100;

		buildPage();
	}

	@Override
	protected String getItemName()
	{
		return _treasureType.getItemName();
	}

	@Override
	protected ItemStack buildItemIcon()
	{
		ItemStack item = ((SupportChestPage) _previousPage).getTreasureItem(_treasureType, Arrays.asList(
				C.cYellow + _target.getName() + C.mBody + " will receive " + C.cYellow + _count,
				C.cYellow + _treasureType.getItemName() + C.mBody + "."
		));

		// Clamp between 1 and 64 always
		item.setAmount(Math.min(Math.max(Math.abs(_count), 1), 64));

		return item;
	}
}
