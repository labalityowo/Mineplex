package mineplex.game.clans.items.smelting;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.items.GearManager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Smelter 
{

	public static void smeltItemInHand(Player player)
	{
		ItemStack item = player.getInventory().getItemInHand();
		
		if (isSmeltable(item))
		{
			ItemStack returns = smeltItem(item);
			player.getInventory().setItemInHand(returns);
		}

		notify(player, "You have successfully smelted your item!");
	}
	
	public static ItemStack smeltItem(ItemStack item)
	{
		Material material = getSmeltedType(item.getType());
		int maxAmount = getSmeltAmount(item.getType());
		int amount = maxAmount;
		
		if (!GearManager.isCustomItem(item))
		{
			short maxDurability = item.getType().getMaxDurability();
			int durability = maxDurability - item.getDurability();
			double percent = durability / (double) maxDurability;
			System.out.println("Durability: " + item.getDurability() + " -- max: " + item.getType().getMaxDurability() + " --- percent: " + percent);
			amount = Math.max(1, (int) (maxAmount * percent)); 
		}
		
		return new ItemStack(material, amount);
	}
	
	private static int getSmeltAmount(Material itemType)
	{
		switch (itemType)
		{
		case IRON_BOOTS:
		case DIAMOND_BOOTS:
		case GOLD_BOOTS:
			return 4;
		case IRON_HELMET:
		case DIAMOND_HELMET:
		case GOLD_HELMET:
			return 5;
		case IRON_LEGGINGS:
		case DIAMOND_LEGGINGS:
		case GOLD_LEGGINGS:
			return 7;
		case IRON_CHESTPLATE:
		case DIAMOND_CHESTPLATE:
		case GOLD_CHESTPLATE:
			return 8;
		case IRON_SWORD:
		case DIAMOND_SWORD:
		case GOLD_SWORD:
			return 2;
		case IRON_AXE:
		case DIAMOND_AXE:
		case GOLD_AXE:
			return 3;
		default:
			return 0;
		}
	}
	
	private static boolean isSmeltable(ItemStack item)
	{
		if (item == null) return false;
		
		return getSmeltedType(item.getType()) != null;
	}
	
	private static Material getSmeltedType(Material itemType)
	{
		switch (itemType)
		{
		case IRON_BOOTS:
		case IRON_LEGGINGS:
		case IRON_CHESTPLATE:
		case IRON_HELMET:
		case IRON_SWORD:
		case IRON_AXE:
			return Material.IRON_ORE;
		case DIAMOND_BOOTS:
		case DIAMOND_LEGGINGS:
		case DIAMOND_CHESTPLATE:
		case DIAMOND_HELMET:
		case DIAMOND_SWORD:
		case DIAMOND_AXE:
			return Material.DIAMOND_ORE;
		case GOLD_BOOTS:
		case GOLD_LEGGINGS:
		case GOLD_CHESTPLATE:
		case GOLD_HELMET:
		case GOLD_SWORD:
		case GOLD_AXE:
			return Material.GOLD_ORE;
		default:
			return null;
		}
	}

	private static void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Smelter", message));
	}
}
