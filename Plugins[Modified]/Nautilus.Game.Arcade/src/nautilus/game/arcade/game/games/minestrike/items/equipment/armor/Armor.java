package nautilus.game.arcade.game.games.minestrike.items.equipment.armor;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.game.games.minestrike.GunModule;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItem;
import nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;

public class Armor extends StrikeItem
{
	public Armor(String name, String[] desc, int cost, int gemCost, Material skin)
	{
		super(StrikeItemType.ARMOR, name, desc, cost, gemCost, skin);
	}

	@Override
	public boolean pickup(GunModule game, Player player)
	{
		return false;
	}
	
	public void giveToPlayer(Player player, Color color)
	{
		ItemStack armor = new ItemStack(getSkinMaterial(), 1, (short) 0, getSkinData());
		LeatherArmorMeta meta = (LeatherArmorMeta)armor.getItemMeta();
		meta.setColor(color);
		meta.setDisplayName(getName());
		armor.setItemMeta(meta);
		
		if (getSkinMaterial() == Material.LEATHER_CHESTPLATE)
			player.getInventory().setChestplate(armor);
		else
			player.getInventory().setHelmet(armor);
		
		UtilPlayer.message(player, F.main("Game", "You equipped " + getName() + "."));
		
		player.getWorld().playSound(player.getLocation(), Sound.HORSE_ARMOR, 1f, 1f);
	}
	
	public static boolean isArmor(ItemStack stack)
	{
		if (stack == null)
			return false;

		try
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)stack.getItemMeta();
			return (meta.getColor().getBlue() == 250 || meta.getColor().getRed() == 250);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	@Override
	public String getShopItemType()
	{
		return C.cDGreen + C.Bold + "Armor" + ChatColor.RESET;
	}
}
