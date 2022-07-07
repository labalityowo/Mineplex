package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.EnchantedBookBuilder;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;

public class KitFighter extends KitPlayer
{	
	public KitFighter(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_ASSAULT_FIGHTER);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		giveRegeneration(player);

		player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setItem(1, new ItemBuilder(Material.GOLDEN_APPLE).setAmount(1).setTitle(C.cPurple + "Golden Applegate").build());
		
		player.getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
	}
	
	@Override
	public void awardKillStreak(Player player, int streak)
	{
		if (streak == 2)
		{
			player.sendMessage(C.cRed + "You have received a Golden Applegate as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_APPLE).setAmount(1).setTitle(C.cPurple + "Golden Applegate").build());
		}
		else if (streak == 4)
		{
			player.sendMessage(C.cRed + "You have received a Healing II Splash Potion as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION).setData((short)16421).build());
		}
		else if (streak == 6)
		{
			player.sendMessage(C.cRed + "You have received a Speed II Potion as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION).setData((short)8226).build());
		}
		else if (streak == 8)
		{
			player.sendMessage(C.cRed + "You have received a Fire Aspect I book as a Kill Streak Reward!");
			player.getInventory().addItem(new EnchantedBookBuilder(1).setLevel(Enchantment.FIRE_ASPECT, 1).build());
		}
	}
}