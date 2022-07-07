package mineplex.staffServer.ui.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.shop.item.ShopItem;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public abstract class SupportGiveItemPage extends SupportPage
{
	protected int _count = 1;
	protected int _lowerBound = 1;
	protected int _upperBound = 64;

	protected int _submitButtonSlot = getSlotIndex(3, 4);

	public SupportGiveItemPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Give");
	}

	protected abstract String getItemName();

	protected void addItemIcon()
	{
		addItem(getSlotIndex(2, 4), buildItemIcon());
	}

	protected abstract ItemStack buildItemIcon();

	private ShopItem getBoundItem(String name)
	{
		return new ShopItem(Material.BARRIER, name, new String[0], 1, true, true);
	}

	private void addArrows()
	{
		int minusSlot = getSlotIndex(3, 2);
		if (_count > _lowerBound)
		{
			addButton(minusSlot,
					new ShopItem(Material.ARROW, "-1", new String[0], 1, false, true),
					(p, c) ->
					{
						_count--;
						refresh();
					});
		}
		else
		{
			addItem(minusSlot, getBoundItem("-1"));
		}

		int plusSlot = getSlotIndex(3, 6);
		if (_count < _upperBound)
		{
			addButton(plusSlot,
					new ShopItem(Material.ARROW, "+1", new String[0], 1, false, true),
					(p, c) ->
					{
						_count++;
						refresh();
					});
		}
		else
		{
			addItem(plusSlot, getBoundItem("+1"));
		}
	}

	protected void addSubmitButton()
	{
		addButton(_submitButtonSlot, new ShopItem(_count > 0 ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK, "Give " + getItemName(), new String[0], 1, false, true), (p, c) -> {
			giveUnknownSalesPackage(_count, getItemName(), true, (success) ->
			{
				_previousPage.refresh();
				goBack();
			});
		});
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		addItemIcon();
		addArrows();
		addSubmitButton();
	}
}
