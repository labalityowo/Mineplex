package nautilus.game.arcade.game.games.moba.shop.warrior;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.shop.MobaItem;
import nautilus.game.arcade.game.games.moba.shop.MobaShop;
import nautilus.game.arcade.game.games.moba.shop.MobaShopCategory;
import nautilus.game.arcade.game.games.moba.shop.MobaShopMenu;
import nautilus.game.arcade.game.games.moba.shop.effects.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MobaWarriorShop extends MobaShopMenu
{

	private static final MobaShopCategory SWORD = new MobaShopCategory("Sword", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.STONE_SWORD)
					.setTitle(C.cGreenB + "Stone Sword")
					.build(), 500),
			new MobaItem(new ItemBuilder(Material.GOLD_SWORD)
					.setTitle(C.cYellowB + "Frostbound Sword")
					.build(), 750)
					.addEffects(
							new MobaHitConditionEffect("Frostbound Sword", ConditionType.SLOW, 3, 0, true)
					),
			new MobaItem(new ItemBuilder(Material.GOLD_SWORD)
					.setTitle(C.cYellowB + "Swift Blade")
					.build(), 750)
					.addEffects(
							new MobaConditionImmunityEffect(ConditionType.SLOW)
					),
			new MobaItem(new ItemBuilder(Material.IRON_SWORD)
					.setTitle(C.cDRedB + "Knight's Blade")
					.build(), 1250)
	), new ItemStack(Material.WOOD_SWORD));

	private static final MobaShopCategory HELMET = new MobaShopCategory("Helmet", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.IRON_HELMET)
					.setTitle(C.cGreenB + "Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
					.build(), 150)
					.addEffects(
							new MobaHPRegenEffect(0.03)
					),
			new MobaItem(new ItemBuilder(Material.IRON_HELMET)
					.setTitle(C.cYellowB + "Superior Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
					.build(), 350)
					.addEffects(
							new MobaHPRegenEffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.IRON_HELMET)
					.setTitle(C.cGreenB + "Brawler's Plate")
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
					.build(), 350)
					.addEffects(
							new MobaHPRegenEffect(0.03)
					),
//			new MobaItem(new ItemBuilder(Material.IRON_HELMET)
//					.setTitle(C.cYellowB + "Superior Brawler's Plate")
//					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
//					.build(), 750)
//					.addEffects(
//							new MobaHPRegenEffect(0.05)
//					),
			new MobaItem(new ItemBuilder(Material.DIAMOND_HELMET)
					.setTitle(C.cDRedB + "Prince's Plate")
					.build(), 1200)
					.addEffects(
							new MobaHPRegenEffect(0.15)
					)
	), new ItemStack(Material.IRON_HELMET));

	private static final MobaShopCategory CHESTPLATE = new MobaShopCategory("Chestplate", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.IRON_CHESTPLATE)
					.setTitle(C.cGreenB + "Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
					.build(), 200)
					.addEffects(
							new MobaTotalHealthEffect(2)
					),
			new MobaItem(new ItemBuilder(Material.IRON_CHESTPLATE)
					.setTitle(C.cYellowB + "Superior Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
					.build(), 450)
					.addEffects(
							new MobaTotalHealthEffect(4)
					),
			new MobaItem(new ItemBuilder(Material.IRON_CHESTPLATE)
					.setTitle(C.cGreenB + "Brawler's Plate")
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
					.build(), 450)
					.addEffects(
							new MobaTotalHealthEffect(2)
					),
//			new MobaItem(new ItemBuilder(Material.IRON_CHESTPLATE)
//					.setTitle(C.cYellowB + "Superior Brawler's Plate")
//					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
//					.build(), 1000)
//					.addEffects(
//							new MobaTotalHealthEffect(4)
//					),
			new MobaItem(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
					.setTitle(C.cDRedB + "Prince's Plate")
					.build(), 1500)
					.addEffects(
							new MobaTotalHealthEffect(4)
					)
	), new ItemStack(Material.IRON_CHESTPLATE));

	private static final MobaShopCategory LEGGINGS = new MobaShopCategory("Leggings", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.IRON_LEGGINGS)
					.setTitle(C.cGreenB + "Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
					.build(), 200)
					.addEffects(
							new MobaCDREffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.IRON_LEGGINGS)
					.setTitle(C.cYellowB + "Superior Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
					.build(), 450)
					.addEffects(
							new MobaCDREffect(0.07)
					),
			new MobaItem(new ItemBuilder(Material.IRON_LEGGINGS)
					.setTitle(C.cGreenB + "Brawler's Plate")
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
					.build(), 450)
					.addEffects(
							new MobaCDREffect(0.05)
					),
//			new MobaItem(new ItemBuilder(Material.IRON_LEGGINGS)
//					.setTitle(C.cYellowB + "Superior Brawler's Plate")
//					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
//					.build(), 1000)
//					.addEffects(
//							new MobaCDREffect(0.07)
//					),
			new MobaItem(new ItemBuilder(Material.DIAMOND_LEGGINGS)
					.setTitle(C.cDRedB + "Prince's Plate")
					.build(), 1500)
					.addEffects(
							new MobaCDREffect(0.1)
					)
	), new ItemStack(Material.IRON_LEGGINGS));

	private static final MobaShopCategory BOOTS = new MobaShopCategory("Boots", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.IRON_BOOTS)
					.setTitle(C.cGreenB + "Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
					.build(), 150)
					.addEffects(
							new MobaSpeedEffect(0.04)
					),
			new MobaItem(new ItemBuilder(Material.IRON_BOOTS)
					.setTitle(C.cYellowB + "Superior Archer's Bane")
					.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
					.build(), 350)
					.addEffects(
							new MobaSpeedEffect(0.06)
					),
			new MobaItem(new ItemBuilder(Material.IRON_BOOTS)
					.setTitle(C.cGreenB + "Brawler's Plate")
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
					.build(), 350)
					.addEffects(
							new MobaSpeedEffect(0.04)
					),
//			new MobaItem(new ItemBuilder(Material.IRON_BOOTS)
//					.setTitle(C.cYellowB + "Superior Brawler's Plate")
//					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
//					.build(), 750)
//					.addEffects(
//							new MobaSpeedEffect(0.06)
//					),
			new MobaItem(new ItemBuilder(Material.DIAMOND_BOOTS)
					.setTitle(C.cDRedB + "Prince's Plate")
					.build(), 1200)
					.addEffects(
							new MobaSpeedEffect(0.1)
					)
	), new ItemStack(Material.IRON_BOOTS));

	public MobaWarriorShop(Moba host, MobaShop shop)
	{
		super(host, shop, MobaRole.WARRIOR);

		addCategory(SWORD);
		addCategory(HELMET);
		addCategory(CHESTPLATE);
		addCategory(LEGGINGS);
		addCategory(BOOTS);
		addConsumables();
	}
}
