package mineplex.game.clans.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.game.clans.items.attributes.ItemAttribute;
import mineplex.game.clans.items.legendaries.LegendaryItem;
import mineplex.game.clans.items.rares.RareItem;

/**
 * Quick little guide on how to use this thing:
 * <p>
 * The method fabricate() returns an ItemStack.
 * <p>
 * 
 * For creating a legendary item, do:
 * <p>
 * RareItemFactory.begin(ItemType.LEGENDARY).setLegendary(AlligatorsTooth.class)
 * .fabricate();
 * <p>
 * 
 * For creating a Flaming Jagged Diamond Sword of Conquering, simply do:
 * <p>
 * RareItemFactory.begin(ItemType.WEAPON).setType(Material.DIAMOND_SWORD).
 * setSuperPrefix(FlamingAttribute.class).setPrefix(JaggedAttribute.class).
 * setSuffix(ConqueringAttribute.class).fabricate();
 */
public class RareItemFactory
{
	private Class<? extends ItemAttribute> _superPrefix;
	private Class<? extends ItemAttribute> _prefix;
	private Class<? extends ItemAttribute> _suffix;
	
	private ItemType _itemType;
	private Material _material;
	private CustomItem _item;
	
	public RareItemFactory(ItemType itemType)
	{
		_itemType = itemType;
	}
	
	public static RareItemFactory begin(ItemType itemType)
	{
		return new RareItemFactory(itemType);
	}
	
	public RareItemFactory setType(Material type)
	{
		_item = new CustomItem(type);
		_item.addDullEnchantment();
		_material = type;
		return this;
	}
	
	public RareItemFactory setLegendary(Class<? extends LegendaryItem> legendary)
	{
		if (_itemType.equals(ItemType.LEGENDARY))
		{
			try
			{
				_item = legendary.newInstance();
				_material = _item.getMaterial();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			throw new RuntimeException("Unexpected call to setLegendary(LegendaryType)");
		}
		
		return this;
	}
	
	public RareItemFactory setRare(Class<? extends RareItem> rare)
	{
		if (_itemType.equals(ItemType.RARE))
		{
			try
			{
				_item = rare.newInstance();
				_material = _item.getMaterial();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			throw new RuntimeException("Unexpected call to setRare(RareType)");
		}
		
		return this;
	}
	
	public RareItemFactory setSuperPrefix(Class<? extends ItemAttribute> superPrefix)
	{
		_superPrefix = superPrefix;
		return this;
	}
	
	public RareItemFactory setSuperPrefix(ItemAttribute superPrefixType)
	{
		if (superPrefixType != null)
		{
			setSuperPrefix(superPrefixType.getClass());
		}
		return this;
	}
	
	public RareItemFactory setPrefix(Class<? extends ItemAttribute> prefix)
	{
		_prefix = prefix;
		return this;
	}
	
	public RareItemFactory setPrefix(ItemAttribute prefixType)
	{
		if (prefixType != null)
		{
			setPrefix(prefixType.getClass());
		}
		return this;
	}
	
	public RareItemFactory setSuffix(Class<? extends ItemAttribute> suffix)
	{
		_suffix = suffix;
		return this;
	}
	
	public RareItemFactory setSuffix(ItemAttribute suffixType)
	{
		if (suffixType != null)
		{
			setSuffix(suffixType.getClass());
		}
		return this;
	}
	
	public ItemStack fabricate()
	{
		applyAttributes();
		
		ItemStack item = _item.toItemStack();
		
		return item;
	}

	private void applyAttributes()
	{
		try
		{
			if (_superPrefix != null)
			{
				_item.getAttributes().addAttribute(_superPrefix.newInstance());
			}

			if (_prefix != null)
			{
				_item.getAttributes().addAttribute(_prefix.newInstance());
			}

			if (_suffix != null)
			{
				_item.getAttributes().addAttribute(_suffix.newInstance());
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	public ItemType getItemType()
	{
		return _itemType;
	}
	
	public Material getMaterial()
	{
		return _material;
	}
	
	public CustomItem getWrapper()
	{
		applyAttributes();
		return _item;
	}
}
