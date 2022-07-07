package mineplex.game.clans.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;

public enum ClansShopItem
{
	// PvP Shop
	GOLD_HELMET(2500, 500, Material.GOLD_HELMET, 1),
	GOLD_CHESTPLATE(4000, 800, Material.GOLD_CHESTPLATE, 1),
	GOLD_LEGGINGS(3500, 700, Material.GOLD_LEGGINGS, 1),
	GOLD_BOOTS(2000, 400, Material.GOLD_BOOTS, 1),
	LEATHER_HELMET(2500, 500, Material.LEATHER_HELMET, 1),
	LEATHER_CHESTPLATE(4000, 800, Material.LEATHER_CHESTPLATE, 1),
	LEATHER_LEGGINGS(3500, 700, Material.LEATHER_LEGGINGS, 1),
	LEATHER_BOOTS(2000, 400, Material.LEATHER_BOOTS, 1),
	CHAINMAIL_HELMET(2500, 500, Material.CHAINMAIL_HELMET, 1),
	CHAINMAIL_CHESTPLATE(4000, 800, Material.CHAINMAIL_CHESTPLATE, 1),
	CHAINMAIL_LEGGINGS(3500, 700, Material.CHAINMAIL_LEGGINGS, 1),
	CHAINMAIL_BOOTS(2000, 500, Material.CHAINMAIL_BOOTS, 1),
	IRON_HELMET(2500, 500, Material.IRON_HELMET, 1),
	IRON_CHESTPLATE(4000, 800, Material.IRON_CHESTPLATE, 1),
	IRON_LEGGINGS(3500, 700, Material.IRON_LEGGINGS, 1),
	IRON_BOOTS(2000, 400, Material.IRON_BOOTS, 1),
	DIAMOND_HELMET(2500, 500, Material.DIAMOND_HELMET, 1),
	DIAMOND_CHESTPLATE(4000, 800, Material.DIAMOND_CHESTPLATE, 1),
	DIAMOND_LEGGINGS(3500, 700, Material.DIAMOND_LEGGINGS, 1),
	DIAMOND_BOOTS(2000, 400, Material.DIAMOND_BOOTS, 1),
	IRON_SWORD(1000, 200, Material.IRON_SWORD, 1),
	DIAMOND_SWORD(9000, 1800, Material.DIAMOND_SWORD, 1),
	GOLD_SWORD(9000, 1800, Material.GOLD_SWORD, 1),
	IRON_AXE(1500, 300, Material.IRON_AXE, 1),
	DIAMOND_AXE(13500, 2700, Material.DIAMOND_AXE, 1),
	GOLD_AXE(13500, 2700, Material.GOLD_AXE, 1),
	BOW(175, 35, Material.BOW, 1),
	ARROW(10, 2, Material.ARROW, 1),
	ENCHANTMENT_TABLE(30000, 0, Material.ENCHANTMENT_TABLE, 1),
	TNT(20000, 0, Material.TNT, 1),
	TNT_GENERATOR(300000, 0, Material.BREWING_STAND_ITEM, 1),
	// Mining Shop
	IRON_INGOT(500, 100, Material.IRON_INGOT, 1),
	GOLD_INGOT(500, 100, Material.GOLD_INGOT, 1),
	DIAMOND(500, 100, Material.DIAMOND, 1),
	LEATHER(500, 100, Material.LEATHER, 1),
	COAL(50, 10, Material.COAL, 1),
	REDSTONE(10, 2, Material.REDSTONE, 1),
	LAPIS_BLOCK(500, 100, Material.LAPIS_BLOCK, 1),
	// Farming Shop
	POTATO_ITEM(15, 8, Material.POTATO_ITEM, 1),
	MELON(5, 3, Material.MELON, 1),
	BREAD(30, 16, Material.BREAD, 1),
	COOKED_BEEF(50, 27, Material.COOKED_BEEF, 1),
	GRILLED_PORK(50, 27, Material.GRILLED_PORK, 1),
	COOKED_CHICKEN(35, 19, Material.COOKED_CHICKEN, 1),
	FEATHER(50, 10, Material.FEATHER, 1),
	CARROT_ITEM(10, 5, Material.CARROT_ITEM, 1),
	MUSHROOM_SOUP(200, 109, Material.MUSHROOM_SOUP, 1),
	SUGAR_CANE(15, 3, Material.SUGAR_CANE, 1),
	PUMPKIN(30, 6, Material.PUMPKIN, 1),
	STRING(50, 10, Material.STRING, 1),
	BONE(1, 1, Material.BONE, 1),
	ROTTEN_FLESH(5, 5, Material.ROTTEN_FLESH, 1),
	SPIDER_EYE(5, 5, Material.SPIDER_EYE, 1),
	// Building Shop
	STONE(100, 20, Material.STONE, 1),
	SMOOTH_BRICK(100, 20, Material.SMOOTH_BRICK, 1),
	CRACKED_STONE_BRICK(25, 5, Material.SMOOTH_BRICK, 1, (byte) 2),
	COBBLESTONE(100, 20, Material.COBBLESTONE, 1),
	LOG(50, 10, Material.LOG, 1),
	LOG_2(50, 10, Material.LOG_2, 1),
	SAND(20, 4, Material.SAND, 1),
	GLASS(30, 6, Material.GLASS, 1),
	SANDSTONE(80, 16, Material.SANDSTONE, 1),
	DIRT(10, 2, Material.DIRT, 1),
	NETHER_BRICK(50, 10, Material.NETHER_BRICK, 1),
	QUARTZ_BLOCK(75, 15, Material.QUARTZ_BLOCK, 1),
	CLAY(30, 6, Material.CLAY, 1),
	GOLD_TOKEN(50000, 50000, Material.GOLD_RECORD, 1, (byte) 0, "Gold Token"),
	OUTPOST(100000, 0, Material.BEACON, 1, (byte) 0, C.cBlue + "Outpost"),
	CANNON(25000, 0, Material.SPONGE, 1, (byte) 1, C.cBlue + "Cannon");
	
	private int _buyPrice;
	private int _sellPrice;
	private int _amount;
	private short _data;
	
	private String _displayName;
	
	private Material _material;
	
	ClansShopItem(int buyPrice, int sellPrice, Material material, int amount, short data)
	{
		_buyPrice = buyPrice;
		_sellPrice = sellPrice;
		_material = material;
		_amount = amount;
		_data = data;
	}
	
	ClansShopItem(int buyPrice, int sellPrice, Material material, int amount)
	{
		this(buyPrice, sellPrice, material, amount, (byte) 0);
	}
	
	ClansShopItem(int buyPrice, int sellPrice, Material material, int amount, byte data, String name)
	{
		this(buyPrice, sellPrice, material, amount, data);
		_displayName = name;
	}
	
	public int getBuyPrice()
	{
		return _buyPrice;
	}
	
	public int getSellPrice()
	{
		return _sellPrice;
	}
	
	public short getData()
	{
		return _data;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	public int getBuyPrice(int amount)
	{
		return _amount == 1 ? _buyPrice * amount : ((int) (((int) ((double) _buyPrice) / ((double) _amount))) * amount);
	}
	
	public int getSellPrice(int amount)
	{
		return _amount == 1 ? _sellPrice * amount : ((int) (((int) ((double) _sellPrice) / ((double) _amount))) * amount);
	}
	
	public int getAmount()
	{
		return _amount;
	}
	
	public Material getMaterial()
	{
		return _material;
	}
	
	public static ClansShopItem getByMaterial(Material material)
	{
		for (ClansShopItem item : values())
		{
			if (item.getMaterial().equals(material))
			{
				return item;
			}
		}
		
		return null;
	}
	
	public static ClansShopItem getByItem(Material material, short data)
	{
		for (ClansShopItem item : values())
		{
			if (item.getMaterial().equals(material) && (item.getData() == -1 || item.getData() == data))
			{
				return item;
			}
		}
		
		return null;
	}
	
	public static ClansShopItem getByItem(ItemStack stack, boolean checkData, boolean checkDisplayName)
	{
		for (ClansShopItem item : values())
		{
			if (item.getMaterial().equals(stack.getType()))
			{
				if (checkData && stack.getDurability() != item.getData())
				{
					continue;
				}
				
				if (checkDisplayName && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() && !stack.getItemMeta().getDisplayName().equals(item.getDisplayName()))
				{
					continue;
				}
				
				return item;
			}
		}
		
		return null;
	}
}