package nautilus.game.arcade.game.games.castleassault.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBomberHG;

public class KitDemolitionist extends KitPlayer
{

	private static final Perk[] PERKS =
			{
					new PerkBomberHG(10, 2)
			};

	public KitDemolitionist(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_ASSAULT_DEMO, PERKS);
	}

	public double getThrowMultiplier(Player player)
	{
		return 1;
	}

	@Override
	public void GiveItems(Player player)
	{
		giveRegeneration(player);

		player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND_SWORD).setUnbreakable(true).build());
		player.getInventory().setItem(1, getGame().getNewFlintAndSteel(true));
		player.getInventory().setItem(2, new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).setAmount(2).build());

		player.getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setLeggings(new ItemBuilder(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
		player.getInventory().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4).setLore(C.cGold + "Kit Item").setUnbreakable(true).build());
	}

	@Override
	public void awardKillStreak(Player player, int streak)
	{
		if (streak == 2)
		{
			player.sendMessage(C.cRed + "You have received 2 Throwing TNT as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).setAmount(2).build());
		}
		else if (streak == 4)
		{
			player.sendMessage(C.cRed + "You have received 3 Throwing TNT as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).setAmount(3).build());
		}
		else if (streak == 6)
		{
			player.sendMessage(C.cRed + "You have received 4 Throwing TNT as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).setAmount(4).build());
		}
		else if (streak == 8)
		{
			player.sendMessage(C.cRed + "You have received 5 Throwing TNT and a 30-Use Flint and Steel as a Kill Streak Reward!");
			player.getInventory().addItem(new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).setAmount(5).build());
			player.getInventory().addItem(new ItemBuilder(Material.FLINT_AND_STEEL).setData((short) (Material.FLINT_AND_STEEL.getMaxDurability() - 30)).build());
		}
	}
}