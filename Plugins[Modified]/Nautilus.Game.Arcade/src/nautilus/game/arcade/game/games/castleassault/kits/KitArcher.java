package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.EnchantedBookBuilder;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitArcher extends KitPlayer
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(6, 10, true, false)
			};

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_ASSAULT_ARCHER, PERKS);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		giveRegeneration(player);
		
		player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setItem(1, new ItemBuilder(Material.BOW).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setItem(2, new ItemBuilder(Material.ARROW).setAmount(10).setTitle(F.item("Fletched Arrow")).build());

		
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
			player.sendMessage(C.cRed + "You have received 8 Arrows as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.ARROW).setAmount(8).setTitle(F.item("Fletched Arrow")).build());
		}
		else if (streak == 4)
		{
			player.sendMessage(C.cRed + "You have received 12 Arrows as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.ARROW).setAmount(12).setTitle(F.item("Fletched Arrow")).build());
		}
		else if (streak == 6)
		{
			player.sendMessage(C.cRed + "You have received a Punch I book as a Kill Streak Reward!");
			player.getInventory().addItem(new EnchantedBookBuilder(1).setLevel(Enchantment.ARROW_KNOCKBACK, 1).build());
		}
		else if (streak == 8)
		{
			player.sendMessage(C.cRed + "You have received 32 Arrows and a Flame I book as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.ARROW).setAmount(32).setTitle(F.item("Fletched Arrow")).build());
			player.getInventory().addItem(new EnchantedBookBuilder(1).setLevel(Enchantment.ARROW_FIRE, 1).build());
		}
	}
}