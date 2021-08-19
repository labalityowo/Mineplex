package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;

public class KitAlchemist extends KitPlayer
{	
	public KitAlchemist(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_ASSAULT_ALCHEMIST);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		giveRegeneration(player);
		player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100000, 0));
		
		player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setItem(1, new ItemBuilder(Material.POTION).setLore(C.cGold + "Kit Item").setData((short)8194).build());
		
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
			player.sendMessage(C.cRed + "You have received a Slowness I Splash Potion as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION).setData((short)16394).build());
		}
		else if (streak == 4)
		{
			player.sendMessage(C.cRed + "You have received a Weakness I Splash Potion as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION).setData((short)16392).build());
		}
		else if (streak == 6)
		{
			player.sendMessage(C.cRed + "You have received 4 Instant Damage II Splash Potions as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.POTION, 4).setData((short)16428).build());
		}
		else if (streak == 8)
		{
			player.sendMessage(C.cRed + "You have received a Regeneration III Potion as a Kill Streak Reward!");
			ItemStack item = new ItemBuilder(Material.POTION).setData((short)8193).build();
			PotionMeta pm = (PotionMeta) item.getItemMeta();
			pm.clearCustomEffects();
			pm.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 2), true);
			item.setItemMeta(pm);
			player.getInventory().addItem(item);
		}
	}
}