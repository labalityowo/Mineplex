package mineplex.core.shop.page;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.ShopItem;

public class MultiPageManager<ItemType>
{

	private int _elementsPerPage = 28;
	private int _page;

	private final BiPredicate<ItemType, Integer> _addItem;
	private final Supplier<List<ItemType>> _getItems;
	private final ShopPageBase _shopPage;

	public MultiPageManager(ShopPageBase shopPage, Supplier<List<ItemType>> getItems, BiPredicate<ItemType, Integer> addItem)
	{
		_shopPage = shopPage;
		_getItems = getItems;
		_addItem = addItem;
	}

	public MultiPageManager(ShopPageBase shopPage, Supplier<List<ItemType>> getItems, BiConsumer<ItemType, Integer> addItem)
	{
		this(shopPage, getItems, (item, slot)->
		{
			addItem.accept(item, slot);
			return true;
		});
	}

	public void setElementsPerPage(int elements)
	{
		_elementsPerPage = elements;
	}

	public int getElementsPerPage()
	{
		return _elementsPerPage;
	}

	public void setPage(int page)
	{
		_page = page;
		_shopPage.refresh();
	}

	public void buildPage()
	{
		int slot = 10;
		int startIndex = _page * _elementsPerPage;
		int endIndex = startIndex + _elementsPerPage;

		List<ItemType> items = _getItems.get();
		items = items.subList(Math.min(startIndex, endIndex), Math.min(endIndex, items.size()));

		for (ItemType item : items)
		{
			if (!_addItem.test(item, slot))
			{
				continue;
			}

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}

		if (_page != 0)
		{
			_shopPage.addButton(45, new ShopItem(Material.ARROW, C.cGreen + "Previous Page", new String[0], 1, false), (player, clickType) ->
			{
				if (!changePage(player))
				{
					return;
				}

				_page--;
				_shopPage.refresh();
			});
		}

		if (endIndex < _getItems.get().size())
		{
			_shopPage.addButton(53, new ShopItem(Material.ARROW, C.cGreen + "Next Page", new String[0], 1, false), (player, clickType) ->
			{
				if (!changePage(player))
				{
					return;
				}

				_page++;
				_shopPage.refresh();
			});
		}
	}

	private boolean changePage(Player player)
	{
		boolean can = Recharge.Instance.use(player, "Change Page", 200, false, false);

		if (!can)
		{
			_shopPage.playDenySound(player);
		}

		return can;
	}
}
