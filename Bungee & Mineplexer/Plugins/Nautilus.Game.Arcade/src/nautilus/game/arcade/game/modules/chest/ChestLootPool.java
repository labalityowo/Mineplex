package nautilus.game.arcade.game.modules.chest;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.weight.WeightSet;

public class ChestLootPool
{

	private static final int DEFAULT_RARITY = 100;

	private final WeightSet<ChestLootItem> _items;
	private final WeightSet<Entry<Enchantment, Integer>> _enchantments;
	private int _minimumPerChest, _maximumPerChest;
	private double _rarity, _enchantmentRarity;
	private boolean _unbreakable;

	public ChestLootPool()
	{
		_items = new WeightSet<>();
		_enchantments = new WeightSet<>();
		_minimumPerChest = 1;
		_maximumPerChest = 1;
		_rarity = 1;
	}

	public ChestLootPool addItem(ItemStack itemStack)
	{
		return addItem(itemStack, itemStack.getAmount(), itemStack.getAmount(), DEFAULT_RARITY);
	}

	public ChestLootPool addItem(ItemStack itemStack, int rarity)
	{
		return addItem(itemStack, itemStack.getAmount(), itemStack.getAmount(), rarity);
	}

	public ChestLootPool addItem(ItemStack itemStack, int lowestAmount, int highestAmount)
	{
		return addItem(itemStack, lowestAmount, highestAmount, DEFAULT_RARITY);
	}

	public ChestLootPool addItem(ItemStack itemStack, int lowestAmount, int highestAmount, int rarity)
	{
		_items.add(rarity, new ChestLootItem(itemStack, lowestAmount, highestAmount));
		return this;
	}

	public ChestLootPool addEnchantment(Enchantment enchantment, int maxLevel)
	{
		return addEnchantment(enchantment, maxLevel, DEFAULT_RARITY);
	}

	public ChestLootPool addEnchantment(Enchantment enchantment, int maxLevel, int rarity)
	{
		_enchantments.add(rarity, new SimpleEntry<>(enchantment, maxLevel));
		return this;
	}

	public ChestLootPool setEnchantmentRarity(double probability)
	{
		_enchantmentRarity = probability;
		return this;
	}

	public ChestLootPool setAmountsPerChest(int minimumPerChest, int maximumPerChest)
	{
		_minimumPerChest = minimumPerChest;
		_maximumPerChest = maximumPerChest;
		return this;
	}

	public ChestLootPool setProbability(double probability)
	{
		_rarity = probability;
		return this;
	}

	public ChestLootPool setUnbreakable(boolean unbreakable)
	{
		_unbreakable = unbreakable;
		return this;
	}

	public void populateChest(Chest chest, List<Integer> slots)
	{
		Inventory inventory = chest.getBlockInventory();

		for (int i = 0; i < UtilMath.rRange(_minimumPerChest, _maximumPerChest); i++)
		{
			int slot = UtilMath.r(slots.size());
			slots.remove(slot);
			inventory.setItem(slot, getRandomItem());
		}

		chest.update();
	}

	public ItemStack getRandomItem()
	{
		ChestLootItem item = _items.generateRandom();

		if (item == null)
		{
			return null;
		}

		ItemStack itemStack = item.getItem();

		if (_enchantments.elements() != null && Math.random() < _enchantmentRarity)
		{
			Entry<Enchantment, Integer> enchantment = _enchantments.generateRandom();

			itemStack.addUnsafeEnchantment(enchantment.getKey(), UtilMath.r(enchantment.getValue()) + 1);
		}

		if (_unbreakable && itemStack.getType().getMaxDurability() > 0)
		{
			UtilItem.makeUnbreakable(itemStack);
		}

		return itemStack;
	}

	public double getProbability()
	{
		return _rarity;
	}
}