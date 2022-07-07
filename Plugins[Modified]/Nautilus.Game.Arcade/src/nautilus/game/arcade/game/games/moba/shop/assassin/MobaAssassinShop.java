package nautilus.game.arcade.game.games.moba.shop.assassin;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
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

public class MobaAssassinShop extends MobaShopMenu
{

	private static final MobaShopCategory SWORD = new MobaShopCategory("Sword", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.GOLD_SWORD)
					.setTitle(C.cGreenB + "Sword of Time")
					.build(), 800)
					.addEffects(
							new MobaKillHealEffect(3)
					),
			new MobaItem(new ItemBuilder(Material.IRON_SWORD)
					.setTitle(C.cYellowB + "Adventurer's Sword")
					.build(), 1000),
			new MobaItem(new ItemBuilder(Material.DIAMOND_SWORD)
					.setTitle(C.cDRedB + "Pumpkin King's Blade")
					.addEnchantment(Enchantment.DAMAGE_ALL, 2)
					.build(), 1750)
	), new ItemStack(Material.WOOD_SWORD));

	private static final MobaShopCategory HELMET = new MobaShopCategory("Helmet", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Leather Cap")
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Ninja's Mask")
					.build(), 500)
					.addEffects(
							new MobaKillHealEffect(2)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Urchin's Cap")
					.build(), 600)
					.addEffects(
							new MobaHPRegenEffect(0.2)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_HELMET)
					.setTitle(C.cYellow + "Bruiser's Helm")
					.build(), 1000)
	), new ItemStack(Material.LEATHER_HELMET));

	private static final MobaShopCategory CHESTPLATE = new MobaShopCategory("Chestplate", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Leather Chestplate")
					.build(), 250),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Ninja's Chestcloth")
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.15)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Urchin's Chestcloth")
					.build(), 850)
					.addEffects(
							new MobaMeleeDamageEffect("Urchin's Chestcloth", 1)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
					.setTitle(C.cYellow + "Bruiser's Chestplate")
					.build(), 1250)
	), new ItemStack(Material.LEATHER_CHESTPLATE));

	private static final MobaShopCategory LEGGINGS = new MobaShopCategory("Leggings", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Leather Leggings")
					.build(), 250),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Ninja's Leggings")
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Urchin's Leggings")
					.build(), 850)
					.addEffects(
							new MobaAbilityDamageEffect("Urchin's Leggings", 0.1)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_LEGGINGS)
					.setTitle(C.cYellow + "Bruiser's Leggings")
					.build(), 1250)
	), new ItemStack(Material.LEATHER_LEGGINGS));

	private static final MobaShopCategory BOOTS = new MobaShopCategory("Boots", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Leather Boots")
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Ninja's Boots")
					.build(), 500)
					.addEffects(
							new MobaSpeedEffect(0.15)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Urchin's Boots")
					.build(), 600)
					.addEffects(
							new MobaSpeedEffect(0.17)
					),
			new MobaItem(new ItemBuilder(Material.CHAINMAIL_BOOTS)
					.setTitle(C.cYellow + "Bruiser's Boots")
					.build(), 1000)
	), new ItemStack(Material.LEATHER_BOOTS));

	public MobaAssassinShop(Moba host, MobaShop shop)
	{
		super(host, shop, MobaRole.ASSASSIN);

		addCategory(SWORD);
		addCategory(HELMET);
		addCategory(CHESTPLATE);
		addCategory(LEGGINGS);
		addCategory(BOOTS);
		addConsumables();
	}
}
