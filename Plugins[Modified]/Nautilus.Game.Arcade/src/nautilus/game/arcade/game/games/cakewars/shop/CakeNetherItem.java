package nautilus.game.arcade.game.games.cakewars.shop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemBuilder;

public enum CakeNetherItem implements CakeTeamItem
{

	PROTECTION(
			"Protection",
			new ItemStack(Material.DIAMOND_CHESTPLATE),
			"Gives your entire team %s.",
			Pair.create("Protection I", 4),
			Pair.create("Protection II", 10)
	)
			{
				@Override
				public void apply(Player player, int level, Location cake)
				{
					for (ItemStack itemStack : player.getInventory().getArmorContents())
					{
						if (UtilItem.isArmor(itemStack) && itemStack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
						}
					}
				}
			},
	HASTE(
			"Haste",
			new ItemStack(Material.GOLD_PICKAXE),
			"Gives your entire team %s.",
			Pair.create("Haste I", 4),
			Pair.create("Haste II", 10)
	)
			{
				@Override
				public void apply(Player player, int level, Location cake)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, level - 1, true, false), true);
				}
			},
	SHARPNESS(
			"Sharpness",
			new ItemStack(Material.DIAMOND_SWORD),
			"Gives your entire team %s.",
			Pair.create("Sharpness I", 8),
			Pair.create("Sharpness II", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location cake)
				{
					for (ItemStack itemStack : player.getInventory().getContents())
					{
						if (UtilItem.isSword(itemStack) && itemStack.getEnchantmentLevel(Enchantment.DAMAGE_ALL) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level);
						}
					}
				}
			},
	POWER(
			"Power",
			new ItemStack(Material.BOW),
			"Gives your entire team %s.",
			Pair.create("Power I", 8),
			Pair.create("Power II", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location cake)
				{
					for (ItemStack itemStack : player.getInventory().getContents())
					{
						if (itemStack != null && itemStack.getType() == Material.BOW && itemStack.getEnchantmentLevel(Enchantment.ARROW_DAMAGE) < level)
						{
							itemStack.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, level);
						}
					}
				}
			},
	RESOURCE(
			"Resource Generator",
			new ItemStack(Material.NETHER_STAR),
			"When an item generates in your generator it will also generate %s extra.",
			Pair.create("1", 10),
			Pair.create("2", 20)
	),
	REGENERATION(
			"Healing Station",
			new ItemStack(Material.GOLDEN_APPLE),
			"Receive %s when within %s of your cake.",
			Pair.create("Regeneration I;" + CakeShopModule.getHealingStationRadius(1) + " blocks", 8),
			Pair.create("Regeneration I;" + CakeShopModule.getHealingStationRadius(2) + " blocks", 12)
	)
			{
				@Override
				public void apply(Player player, int level, Location cake)
				{
					int maxDist = CakeShopModule.getHealingStationRadius(level);

					if (UtilMath.offset2dSquared(player.getLocation(), cake) < maxDist * maxDist)
					{
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false), true);
					}
				}
			},
	;

	private final String _name;
	private final ItemStack _itemStack;
	private final String _description;
	private final Pair<String, Integer>[] _levels;

	@SafeVarargs
	CakeNetherItem(String name, ItemStack itemStack, String description, Pair<String, Integer>... levels)
	{
		_name = name;
		_itemStack = new ItemBuilder(itemStack)
				.setTitle(name)
				.setGlow(true)
				.build();
		_description = description;
		_levels = levels;
	}

	@Override
	public void apply(Player player, int level, Location cake)
	{
	}

	@Override
	public CakeShopItemType getItemType()
	{
		return CakeShopItemType.TEAM_UPGRADE;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	@Override
	public String[] getDescription(int level)
	{
		String description = C.mBody + _description;
		String[] vars = _levels[level].getLeft().split(";");

		for (String var : vars)
		{
			description = description.replaceFirst("%s", C.cGreen + var + C.mBody);
		}

		return UtilText.splitLineToArray(description, LineFormat.LORE);
	}

	@Override
	public Pair<String, Integer>[] getLevels()
	{
		return _levels;
	}

	@Override
	public int getCost()
	{
		return 0;
	}
}
