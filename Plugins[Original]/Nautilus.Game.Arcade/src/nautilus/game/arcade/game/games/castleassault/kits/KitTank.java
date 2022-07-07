package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.EnchantedBookBuilder;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;

public class KitTank extends KitPlayer
{
	public KitTank(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_ASSAULT_TANK);
	}

	@Override
	public void GiveItems(Player player)
	{
		giveRegeneration(player);

		player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());

		player.getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
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
			player.sendMessage(C.cRed + "You have received a Regeneration II Potion as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION).setData((short) 8289).build());
		}
		else if (streak == 6)
		{
			player.sendMessage(C.cRed + "You have received a Resistance I Potion as a Kill Streak Reward!");
			ItemStack item = new ItemBuilder(Material.POTION).setData((short) 8205).build();
			PotionMeta pm = (PotionMeta) item.getItemMeta();
			pm.clearCustomEffects();
			pm.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60, 0), true);
			item.setItemMeta(pm);
			player.getInventory().addItem(item);
		}
		else if (streak == 8)
		{
			player.sendMessage(C.cRed + "You have received a Thorns II book as a Kill Streak Reward!");
			player.getInventory().addItem(new EnchantedBookBuilder(1).setLevel(Enchantment.THORNS, 2).build());
		}
	}
}