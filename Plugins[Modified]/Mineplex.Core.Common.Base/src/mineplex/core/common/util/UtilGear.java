package mineplex.core.common.util;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UtilGear
{
	private static HashSet<Material> _axeSet = new HashSet<Material>();
	private static HashSet<Material> _swordSet = new HashSet<Material>();
	private static HashSet<Material> _maulSet = new HashSet<Material>();
	private static HashSet<Material> pickSet = new HashSet<Material>();
	private static HashSet<Material> diamondSet = new HashSet<Material>();
	private static HashSet<Material> goldSet = new HashSet<Material>();
	private static HashSet<Material> _armorSet = new HashSet<Material>();

	public static boolean isArmor(ItemStack item)
	{
		if (item == null)
			return false;

		if (_armorSet.isEmpty())
		{
			_armorSet.add(Material.LEATHER_HELMET);
			_armorSet.add(Material.LEATHER_CHESTPLATE);
			_armorSet.add(Material.LEATHER_LEGGINGS);
			_armorSet.add(Material.LEATHER_BOOTS);

			_armorSet.add(Material.GOLD_HELMET);
			_armorSet.add(Material.GOLD_CHESTPLATE);
			_armorSet.add(Material.GOLD_LEGGINGS);
			_armorSet.add(Material.GOLD_BOOTS);

			_armorSet.add(Material.CHAINMAIL_HELMET);
			_armorSet.add(Material.CHAINMAIL_CHESTPLATE);
			_armorSet.add(Material.CHAINMAIL_LEGGINGS);
			_armorSet.add(Material.CHAINMAIL_BOOTS);

			_armorSet.add(Material.IRON_HELMET);
			_armorSet.add(Material.IRON_CHESTPLATE);
			_armorSet.add(Material.IRON_LEGGINGS);
			_armorSet.add(Material.IRON_BOOTS);

			_armorSet.add(Material.DIAMOND_HELMET);
			_armorSet.add(Material.DIAMOND_CHESTPLATE);
			_armorSet.add(Material.DIAMOND_LEGGINGS);
			_armorSet.add(Material.DIAMOND_BOOTS);
		}

		return _armorSet.contains(item.getType());
	}

	public static boolean isAxe(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (_axeSet.isEmpty())
		{
			_axeSet.add(Material.WOOD_AXE);
			_axeSet.add(Material.STONE_AXE);
			_axeSet.add(Material.IRON_AXE);
			_axeSet.add(Material.GOLD_AXE);
			_axeSet.add(Material.DIAMOND_AXE);
		}
		
		return _axeSet.contains(item.getType());
	}
	
	public static boolean isSword(ItemStack item)
	{

		if (item == null)
			return false;
		
		if (_swordSet.isEmpty())
		{
			_swordSet.add(Material.WOOD_SWORD);
			_swordSet.add(Material.STONE_SWORD);
			_swordSet.add(Material.IRON_SWORD);
			_swordSet.add(Material.GOLD_SWORD);
			_swordSet.add(Material.DIAMOND_SWORD);
		}
		
		return _swordSet.contains(item.getType());
	}
		
	public static boolean isShovel(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (_maulSet.isEmpty())
		{
			_maulSet.add(Material.WOOD_SPADE);
			_maulSet.add(Material.STONE_SPADE);
			_maulSet.add(Material.IRON_SPADE);
			_maulSet.add(Material.GOLD_SPADE);
			_maulSet.add(Material.DIAMOND_SPADE);
		}
		
		return _maulSet.contains(item.getType());
	}
	
	public static HashSet<Material> scytheSet = new HashSet<Material>();
	public static boolean isHoe(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (scytheSet.isEmpty())
		{
			scytheSet.add(Material.WOOD_HOE);
			scytheSet.add(Material.STONE_HOE);
			scytheSet.add(Material.IRON_HOE);
			scytheSet.add(Material.GOLD_HOE);
			scytheSet.add(Material.DIAMOND_HOE);
		}
		
		return scytheSet.contains(item.getType());
	}
	
	public static boolean isPickaxe(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (pickSet.isEmpty())
		{
			pickSet.add(Material.WOOD_PICKAXE);
			pickSet.add(Material.STONE_PICKAXE);
			pickSet.add(Material.IRON_PICKAXE);
			pickSet.add(Material.GOLD_PICKAXE);
			pickSet.add(Material.DIAMOND_PICKAXE);
		}
		
		return pickSet.contains(item.getType());
	}	

	public static boolean isDiamond(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (diamondSet.isEmpty())
		{
			diamondSet.add(Material.DIAMOND_SWORD);
			diamondSet.add(Material.DIAMOND_AXE);
			diamondSet.add(Material.DIAMOND_SPADE);
			diamondSet.add(Material.DIAMOND_HOE);
		}
		
		return diamondSet.contains(item.getType());
	}
	
	public static boolean isGold(ItemStack item)
	{
		if (item == null)
			return false;
		
		if (goldSet.isEmpty())
		{
			goldSet.add(Material.GOLD_SWORD);
			goldSet.add(Material.GOLD_AXE);
			goldSet.add(Material.GOLD_HELMET);
			goldSet.add(Material.GOLD_CHESTPLATE);
			goldSet.add(Material.GOLD_LEGGINGS);
			goldSet.add(Material.GOLD_BOOTS);
		}
		
		return goldSet.contains(item.getType());
	}
	
	public static boolean isBow(ItemStack item)
	{
		if (item == null)
			return false;
		
		return item.getType() == Material.BOW;
	}
	
	public static boolean isWeapon(ItemStack item)
	{
		return isAxe(item) || isSword(item);
	}
	
	public static boolean isMat(ItemStack item, Material mat)
	{
		if (item == null)
			return false;
		
		return item.getType() == mat;
	}
	
	public static boolean isMatAndData(ItemStack item, Material mat, byte data)
	{
		if (item == null) return false;
		
		return item.getType() == mat && item.getData().getData() == data;
	}

	public static boolean isRepairable(ItemStack item) 
	{
		return (item.getType().getMaxDurability() > 0);
	}
}
