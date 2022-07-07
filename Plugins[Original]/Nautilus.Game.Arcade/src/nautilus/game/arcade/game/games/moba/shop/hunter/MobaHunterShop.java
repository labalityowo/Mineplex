package nautilus.game.arcade.game.games.moba.shop.hunter;

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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MobaHunterShop extends MobaShopMenu
{

	private static final MobaShopCategory BOW = new MobaShopCategory("Bow", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cGreenB + "Hunter's Bow")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
					.build(), 500),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cGreenB + "Elven Bow")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 2)
					.build(), 750),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cGreenB + "Eagle's Bow")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 3)
					.build(), 1000),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cYellowB + "Bow of Pursuit")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 2)
					.build(), 1200)
					.addEffects(
							new MobaHitConditionEffect("Bow of Pursuit", ConditionType.SPEED, 3, 1, false)
					),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cYellowB + "Vampiric Bow")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
					.build(), 1500)
					.addEffects(
							new MobaKillHealEffect(0.15)
					),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cYellowB + "Bow of Renewal")
					.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
					.build(), 1750)
					.addEffects(
							new MobaHitArrowAmmoEffect()
					),
			new MobaItem(new ItemBuilder(Material.BOW)
					.setTitle(C.cYellowB + "Specialist's Bow")
					.build(), 1000)
					.addEffects(
							new MobaCDREffect(0.1),
							new MobaAbilityDamageEffect("Specialist's Bow", 0.2)
					)
	), new ItemStack(Material.BOW));

	private static final MobaShopCategory HELMET = new MobaShopCategory("Helmet", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Leather Helmet")
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Leather Cap of Holding")
					.build(), 400)
					.addEffects(
							new MobaAmmoIncreaseEffect(1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Leather Cap of Nimble Fingers")
					.setColor(Color.GREEN)
					.build(), 400)
					.addEffects(
							new MobaCDRAmmoEffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Focused Cap")
					.setColor(Color.PURPLE)
					.build(), 500)
					.addEffects(
							new MobaCDREffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Vampiric Helmet")
					.setColor(Color.RED)
					.build(), 500)
					.addEffects(
							new MobaHPRegenEffect(0.2)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_HELMET)
					.setTitle(C.cGreen + "Chainmail Helmet")
					.build(), 500)
	), new ItemStack(Material.LEATHER_HELMET));

	private static final MobaShopCategory CHESTPLATE = new MobaShopCategory("Chestplate", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Leather Chestplate")
					.build(), 250),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Leather Chestplate of Nimble Fingers")
					.setColor(Color.GREEN)
					.build(), 750)
					.addEffects(
							new MobaCDRAmmoEffect(0.15)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Focused Chestplate")
					.setColor(Color.PURPLE)
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Vampiric Chestplate")
					.setColor(Color.RED)
					.build(), 750)
					.addEffects(
							new MobaKillHealEffect(3)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
					.setTitle(C.cGreen + "Chainmail Chestplate")
					.build(), 500)
	), new ItemStack(Material.LEATHER_CHESTPLATE));

	private static final MobaShopCategory LEGGINGS = new MobaShopCategory("Leggings", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Leather Leggings")
					.build(), 250),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Leather Leggings of Nimble Fingers")
					.setColor(Color.GREEN)
					.build(), 750)
					.addEffects(
							new MobaCDRAmmoEffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Focused Leggings")
					.setColor(Color.PURPLE)
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Vampiric Leggings")
					.setColor(Color.RED)
					.build(), 700)
					.addEffects(
							new MobaKillHealEffect(3)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_LEGGINGS)
					.setTitle(C.cGreen + "Chainmail Leggings")
					.build(), 500)
	), new ItemStack(Material.LEATHER_LEGGINGS));

	private static final MobaShopCategory BOOTS = new MobaShopCategory("Boots", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Leather Boots")
					.build(), 250),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Leather Boots of Nimble Fingers")
					.setColor(Color.GREEN)
					.build(), 400)
					.addEffects(
							new MobaCDRAmmoEffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Focused Boots")
					.setColor(Color.PURPLE)
					.build(), 600)
					.addEffects(
							new MobaCDREffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Vampiric Boots")
					.setColor(Color.RED)
					.build(), 500)
					.addEffects(
							new MobaKillHealEffect(1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Leather Hiking Boots")
					.build(), 300)
					.addEffects(
							new MobaSpeedEffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_BOOTS)
					.setTitle(C.cGreen + "Leather Moccasins")
					.build(), 500)
					.addEffects(
							new MobaSpeedEffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_BOOTS)
					.setTitle(C.cGreen + "Leather Crosstrainers")
					.build(), 800)
					.addEffects(
							new MobaSpeedEffect(0.15)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_BOOTS)
					.setTitle(C.cGreen + "Chainmail Boots")
					.build(), 500)
	), new ItemStack(Material.LEATHER_BOOTS));

	public MobaHunterShop(Moba host, MobaShop shop)
	{
		super(host, shop, MobaRole.HUNTER);

		addCategory(BOW);
		addCategory(HELMET);
		addCategory(CHESTPLATE);
		addCategory(LEGGINGS);
		addCategory(BOOTS);
		addConsumables();
	}
}
