package nautilus.game.arcade.game.games.moba.shop.mage;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.shop.MobaItem;
import nautilus.game.arcade.game.games.moba.shop.MobaShop;
import nautilus.game.arcade.game.games.moba.shop.MobaShopCategory;
import nautilus.game.arcade.game.games.moba.shop.MobaShopMenu;
import nautilus.game.arcade.game.games.moba.shop.effects.MobaAbilityDamageEffect;
import nautilus.game.arcade.game.games.moba.shop.effects.MobaBasicAttackDamageEffect;
import nautilus.game.arcade.game.games.moba.shop.effects.MobaCDREffect;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class MobaMageShop extends MobaShopMenu
{

	// Dark Purple
	private static final Color ABILITY_DAMAGE_COLOUR = Color.fromBGR(181, 7, 123);
	// Purple
	private static final Color COOLDOWN_COLOUR = Color.PURPLE;
	// Light Purple
	private static final Color BASIC_DAMAGE_COLOUR = Color.fromBGR(254, 89, 200);

	private static final MobaShopCategory HELMET = new MobaShopCategory("Helmet", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Leather Cap")
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Adept's Cap")
					.setColor(ABILITY_DAMAGE_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaAbilityDamageEffect("Adept's Cap", 0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Helm of Focus")
					.setColor(COOLDOWN_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.03)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_HELMET)
					.setTitle(C.cGreen + "Battle Mage Cap")
					.setColor(BASIC_DAMAGE_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaBasicAttackDamageEffect("Battle Mage Cap", 0.03)
					),
			new MobaItem(new ItemBuilder(Material.GOLD_HELMET)
					.setTitle(C.cYellow + "Golden Helmet")
					.build(), 1000)
	), new ItemStack(Material.LEATHER_HELMET));

	private static final MobaShopCategory CHESTPLATE = new MobaShopCategory("Chestplate", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Leather Chestplate")
					.build(), 400),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Adept's Chestplate")
					.setColor(ABILITY_DAMAGE_COLOUR)
					.build(), 1500)
					.addEffects(
							new MobaAbilityDamageEffect("Adept's Chestplate", 0.15)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Chestplate of Focus")
					.setColor(COOLDOWN_COLOUR)
					.build(), 1250)
					.addEffects(
							new MobaCDREffect(0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setTitle(C.cGreen + "Battle Mage Chestplate")
					.setColor(BASIC_DAMAGE_COLOUR)
					.build(), 1250)
					.addEffects(
							new MobaBasicAttackDamageEffect("Battle Mage Chestplate", 0.1)
					),
			new MobaItem(new ItemBuilder(Material.GOLD_CHESTPLATE)
					.setTitle(C.cYellow + "Golden Chestplate")
					.build(), 1250)
	), new ItemStack(Material.LEATHER_CHESTPLATE));

	private static final MobaShopCategory LEGGINGS = new MobaShopCategory("Leggings", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Leather Leggings")
					.build(), 400),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Adept's Leggings")
					.setColor(ABILITY_DAMAGE_COLOUR)
					.build(), 1000)
					.addEffects(
							new MobaAbilityDamageEffect("Adept's Leggings", 0.1)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Leggings of Focus")
					.setColor(COOLDOWN_COLOUR)
					.build(), 1000)
					.addEffects(
							new MobaCDREffect(0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setTitle(C.cGreen + "Battle Mage Leggings")
					.setColor(BASIC_DAMAGE_COLOUR)
					.build(), 1000)
					.addEffects(
							new MobaBasicAttackDamageEffect("Battle Mage Leggings", 0.05)
					),
			new MobaItem(new ItemBuilder(Material.GOLD_LEGGINGS)
					.setTitle(C.cYellow + "Golden Leggings")
					.build(), 1000)
	), new ItemStack(Material.LEATHER_LEGGINGS));

	private static final MobaShopCategory BOOTS = new MobaShopCategory("Boots", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Leather Boots")
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Adept's Boots")
					.setColor(ABILITY_DAMAGE_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaAbilityDamageEffect("Adept's Boots", 0.05)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Boots of Focus")
					.setColor(COOLDOWN_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaCDREffect(0.03)
					),
			new MobaItem(new ItemBuilder(Material.LEATHER_BOOTS)
					.setTitle(C.cGreen + "Battle Mage Boots")
					.setColor(BASIC_DAMAGE_COLOUR)
					.build(), 750)
					.addEffects(
							new MobaBasicAttackDamageEffect("Battle Mage Boots", 0.03)
					),
			new MobaItem(new ItemBuilder(Material.GOLD_BOOTS)
					.setTitle(C.cYellow + "Golden Boots")
					.build(), 1000)
	), new ItemStack(Material.LEATHER_BOOTS));

	public MobaMageShop(Moba host, MobaShop shop)
	{
		super(host, shop, MobaRole.MAGE);

		addCategory(HELMET);
		addCategory(CHESTPLATE);
		addCategory(LEGGINGS);
		addCategory(BOOTS);
		addConsumables();
	}
}
