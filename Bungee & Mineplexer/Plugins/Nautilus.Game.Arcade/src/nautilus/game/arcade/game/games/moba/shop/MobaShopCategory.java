package nautilus.game.arcade.game.games.moba.shop;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MobaShopCategory
{

	private final String _name;
	private final List<MobaItem> _items;
	private final ItemStack _menuItem;
	private boolean _dropOnDeath;
	private boolean _allowMultiple;
	private boolean _trackPurchases;

	public MobaShopCategory(String name, List<MobaItem> items, ItemStack menuItem)
	{
		_name = name;
		_items = items;
		_menuItem = menuItem;
		_trackPurchases = true;
	}

	public String getName()
	{
		return _name;
	}

	public List<MobaItem> getItems()
	{
		return _items;
	}

	public ItemStack getMenuItem()
	{
		return _menuItem;
	}

	public MobaShopCategory dropOnDeath()
	{
		_dropOnDeath = true;
		return this;
	}

	public boolean isDroppingOnDeath()
	{
		return _dropOnDeath;
	}

	public MobaShopCategory allowMultiple()
	{
		_allowMultiple = true;
		return this;
	}

	public boolean isAllowingMultiple()
	{
		return _allowMultiple;
	}

	public MobaShopCategory dontTrackPurchases()
	{
		_trackPurchases = false;
		return this;
	}

	public boolean isTrackingPurchases()
	{
		return _trackPurchases;
	}
}
