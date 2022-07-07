package nautilus.game.arcade.game.games.survivalgames.modes;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.survivalgames.modules.TrackingCompassModule;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;
import nautilus.game.arcade.game.modules.chest.ChestLootPool;

class OverpoweredSGModule extends Module
{

	private static final int GRACE_PERIOD = 30;

	void setupTier1Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		lootModule.registerChestType("Tier 1", chests, 0.35,

				new ChestLootPool()
						.addItem(new ItemStack(Material.DIAMOND_AXE), 240)
						.addItem(new ItemStack(Material.DIAMOND_SWORD), 210)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.DIAMOND_HELMET))
						.addItem(new ItemStack(Material.DIAMOND_CHESTPLATE))
						.addItem(new ItemStack(Material.DIAMOND_LEGGINGS))
						.addItem(new ItemStack(Material.DIAMOND_BOOTS))
						.setAmountsPerChest(1, 2)
						.setUnbreakable(true)
						.setEnchantmentRarity(0.3)
						.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
						.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.BOW))
						.setProbability(0.2)
						.setUnbreakable(true)
						.setEnchantmentRarity(0.5)
						.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
						.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.FISHING_ROD))
						.addItem(new ItemStack(Material.ARROW), 10, 20, 50)
						.addItem(new ItemStack(Material.SNOW_BALL), 10, 20)
						.addItem(new ItemStack(Material.EGG), 10, 20)
						.addItem(new Potion(PotionType.INSTANT_HEAL, 1)
								.splash()
								.toItemStack(1), 40)
						.addItem(new Potion(PotionType.INSTANT_HEAL, 2)
								.toItemStack(1), 30)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.COOKED_BEEF), 4, 8)
						.addItem(new ItemStack(Material.COOKED_CHICKEN), 4, 8)
						.addItem(new ItemStack(Material.GRILLED_PORK), 4, 8)
						.addItem(new ItemStack(Material.MUSHROOM_SOUP))
						.addItem(new ItemStack(Material.GOLDEN_APPLE))
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.EXP_BOTTLE), 16, 32)
						.addItem(new ItemStack(Material.STICK), 1, 2)
						.addItem(new ItemStack(Material.BOAT), 50)
						.addItem(new ItemStack(Material.FLINT), 1, 2, 70)
						.addItem(new ItemStack(Material.FEATHER), 1, 2, 70)
						.addItem(new ItemStack(Material.GOLD_INGOT), 1, 1, 80)
						.addItem(compassModule.getCompass(10))
						.addItem(tnt)
		);
	}

	void setupTier2Loot(ChestLootModule lootModule, TrackingCompassModule compassModule, ItemStack tnt, List<Location> chests)
	{
		lootModule.registerChestType("Tier 2", chests,

				new ChestLootPool()
						.addItem(new ItemStack(Material.DIAMOND_SWORD))
						.setUnbreakable(true)
						.setEnchantmentRarity(0.5)
						.addEnchantment(Enchantment.DAMAGE_ALL, 2)
						.addEnchantment(Enchantment.KNOCKBACK, 2)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.DIAMOND_HELMET))
						.addItem(new ItemStack(Material.DIAMOND_CHESTPLATE))
						.addItem(new ItemStack(Material.DIAMOND_LEGGINGS))
						.addItem(new ItemStack(Material.DIAMOND_BOOTS))
						.setAmountsPerChest(1, 2)
						.setUnbreakable(true)
						.setEnchantmentRarity(0.6)
						.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
						.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.BOW))
						.setProbability(0.2)
						.setUnbreakable(true)
						.setEnchantmentRarity(0.5)
						.addEnchantment(Enchantment.ARROW_DAMAGE, 2)
						.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2),

				new ChestLootPool()
						.addItem(new ItemStack(Material.FISHING_ROD))
						.addItem(new ItemStack(Material.ARROW), 10, 20, 50)
						.addItem(new ItemStack(Material.SNOW_BALL), 10, 20)
						.addItem(new ItemStack(Material.EGG), 10, 20)
						.addItem(new Potion(PotionType.INSTANT_HEAL, 1)
								.splash()
								.toItemStack(1), 50)
						.addItem(new Potion(PotionType.INSTANT_HEAL, 2)
								.toItemStack(1), 40)
						.setUnbreakable(true)
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.COOKED_BEEF), 4, 8)
						.addItem(new ItemStack(Material.COOKED_CHICKEN), 4, 8)
						.addItem(new ItemStack(Material.GRILLED_PORK), 4, 8)
						.addItem(new ItemStack(Material.MUSHROOM_SOUP))
						.addItem(new ItemStack(Material.GOLDEN_APPLE))
				,

				new ChestLootPool()
						.addItem(new ItemStack(Material.EXP_BOTTLE), 16, 32)
						.addItem(new ItemStack(Material.STICK), 1, 2)
						.addItem(new ItemStack(Material.BOAT), 50)
						.addItem(new ItemStack(Material.FLINT), 1, 2, 70)
						.addItem(new ItemStack(Material.FEATHER), 1, 2, 70)
						.addItem(new ItemStack(Material.GOLD_INGOT), 1, 1, 80)
						.addItem(new ItemStack(Material.DIAMOND), 1, 1, 70)
						.addItem(compassModule.getCompass(10))
						.addItem(tnt)

		);
	}

	void setupSupplyDropLoot(Map<Integer, List<ItemStack>> items)
	{
		items.put(1, Lists.newArrayList
				(
						new ItemBuilder(Material.DIAMOND_HELMET)
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.DIAMOND_CHESTPLATE)
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.DIAMOND_LEGGINGS)
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.DIAMOND_BOOTS)
								.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
								.setUnbreakable(true)
								.build()
				));
		items.put(2, Lists.newArrayList
				(
						new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.DAMAGE_ALL, 3)
								.setUnbreakable(true)
								.build(),
						new ItemBuilder(Material.DIAMOND_SWORD)
								.addEnchantment(Enchantment.KNOCKBACK, 2)
								.setUnbreakable(true)
								.build()
				));
		items.put(3, Lists.newArrayList
				(
						new ItemBuilder(Material.STICK)
								.addEnchantment(Enchantment.KNOCKBACK, 4)
								.build()
				));
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		getGame().DamagePvP = false;
		getGame().Announce(C.cGreenB + "PvP is disabled for " + GRACE_PERIOD + " seconds.");

		getGame().getArcadeManager().runSyncLater(() ->
		{
			if (!getGame().IsLive())
			{
				return;
			}

			getGame().DamagePvP = true;
			getGame().Announce(C.cRedB + "PvP has been enabled!", false);

			for (Player player : UtilServer.getPlayersCollection())
			{
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			}

		}, GRACE_PERIOD * 20);
	}
}
