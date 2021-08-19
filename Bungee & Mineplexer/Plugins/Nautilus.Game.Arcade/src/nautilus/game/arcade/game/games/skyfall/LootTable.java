package nautilus.game.arcade.game.games.skyfall;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.loot.ChestLoot;
import mineplex.core.loot.RandomItem;

/**
 * Can be converted into a {@link ChestLoot} Object.
 *
 * @author xXVevzZXx
 */
public class LootTable
{
	// Survival Loot
	
	public final static LootTable BASIC = new LootTable(true,
		new RandomItem(Material.BAKED_POTATO, 30, 1, 3),
		new RandomItem(Material.COOKED_BEEF, 30, 1, 2),
		new RandomItem(Material.COOKED_CHICKEN, 30, 1, 2),
		new RandomItem(Material.CARROT_ITEM, 30, 1, 3),
		new RandomItem(Material.MUSHROOM_SOUP, 15, 1, 1),
		new RandomItem(Material.APPLE, 30, 1, 4),
		new RandomItem(Material.ROTTEN_FLESH, 40, 1, 6),
		
		new RandomItem(Material.WOOD_AXE, 100),
		new RandomItem(Material.WOOD_SWORD, 80),
		new RandomItem(Material.STONE_AXE, 70),
		new RandomItem(Material.STONE_SWORD, 50),
		new RandomItem(Material.IRON_AXE, 40),
		
		new RandomItem(Material.LEATHER_BOOTS, 40),
		new RandomItem(Material.LEATHER_HELMET, 40),
		new RandomItem(Material.LEATHER_LEGGINGS, 40),
		
		new RandomItem(Material.GOLD_BOOTS, 35),
		new RandomItem(Material.GOLD_HELMET, 35),
		new RandomItem(Material.GOLD_LEGGINGS, 35),
		
		new RandomItem(Material.CHAINMAIL_BOOTS, 30),
		new RandomItem(Material.CHAINMAIL_HELMET, 30),
		new RandomItem(Material.CHAINMAIL_LEGGINGS, 30),
		
		new RandomItem(Material.FISHING_ROD, 30),
		new RandomItem(Material.BOW, 50),
		new RandomItem(Material.ARROW, 50, 1, 3),
		new RandomItem(Material.SNOW_BALL, 30, 1, 2),
		new RandomItem(Material.EGG, 30, 1, 2),
		
		new RandomItem(Material.STICK, 30, 1, 2),
		new RandomItem(Material.FLINT, 30, 1, 2),
		new RandomItem(Material.FEATHER, 30, 1, 2),
		new RandomItem(Material.GOLD_INGOT, 20),
		new RandomItem(ItemStackFactory.Instance.CreateStack(
				Material.TNT, (byte) 0, 1, F.item("Throwing TNT")), 15),
		new RandomItem(Material.MUSHROOM_SOUP, 15),
		
		new RandomItem(Material.BAKED_POTATO, 25, 1, 5),
		new RandomItem(Material.MUSHROOM_SOUP, 25, 1, 1),
		new RandomItem(Material.COOKED_BEEF, 35, 1, 3),
		new RandomItem(Material.COOKED_CHICKEN, 35, 1, 3),
		new RandomItem(Material.COOKED_FISH, 35, 1, 6),
		new RandomItem(Material.GRILLED_PORK, 25, 1, 3),
		new RandomItem(Material.COOKIE, 30),
		new RandomItem(Material.PUMPKIN_PIE, 20, 1, 3),
		new RandomItem(Material.APPLE, 20, 2, 6),
		
		new RandomItem(Material.DIAMOND, 30)
		);
	
	public final static LootTable SUPPLY_DROP = new LootTable(true,	
		new RandomItem(Material.DIAMOND_HELMET, 30),
		new RandomItem(Material.DIAMOND_LEGGINGS, 27),
		new RandomItem(Material.DIAMOND_BOOTS, 30),
		new RandomItem(Material.DIAMOND_SWORD, 16),
		new RandomItem(Material.DIAMOND_AXE, 24),
		new RandomItem(Material.GOLDEN_APPLE, 4),
		new RandomItem(Material.BOW, 4),
		new RandomItem(Material.ARROW, 2)
//		new RandomItem(ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, 1, Enchantment.DAMAGE_ALL), 8),
//		new RandomItem(ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SWORD, 1, Enchantment.DAMAGE_ALL), 4),
//		
//		new RandomItem(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(1), 6),
//		new RandomItem(new Potion(PotionType.INSTANT_DAMAGE, 1, true).toItemStack(1), 3),
//		new RandomItem(new Potion(PotionType.SPEED, 1, true).toItemStack(1), 3),
//		new RandomItem(new Potion(PotionType.STRENGTH, 2, false).toItemStack(1), 3)
		);
	
	public final static LootTable ALL = new LootTable(BASIC.includes(SUPPLY_DROP));
	
	private RandomItem[] _items;
	private boolean _unbreakable;
	
	private LootTable(LootTable table)
	{
		_unbreakable = table.isUnbreakable();
		_items = table.getItems();
	}
	
	private LootTable(RandomItem... items)
	{
		_unbreakable = false;
		_items = items;
	}
	
	private LootTable(boolean unbreakable, RandomItem... items)
	{
		_unbreakable = unbreakable;
		_items = items;
	}
	
	public RandomItem[] getItems()
	{
		return _items;
	}
	
	public boolean isUnbreakable()
	{
		return _unbreakable;
	}
	
	public ChestLoot getloot()
	{
		ChestLoot loot = new ChestLoot(_unbreakable);
		for (RandomItem item : _items)
		{
			loot.addLoot(item);
		}
		return loot;
	}
	
	public LootTable includes(LootTable table)
	{
		int size = _items.length + table.getItems().length;
		RandomItem[] items = new RandomItem[size];
		
		int i = 0;
		
		for (RandomItem item : _items)
		{
			items[i] = item;
			i++;
		}
		
		for (RandomItem item : table.getItems())
		{
			items[i] = item;
			i++;
		}	
		
		return new LootTable(_unbreakable, items);
	}
	
	public LootTable excludes(ArrayList<Material> randomItems)
	{
		int size = _items.length - randomItems.size();
		RandomItem[] items = new RandomItem[size];
		
		int i = 0;
		
		for (RandomItem item : _items)
		{
			boolean cont = false;
			
			for (Material other : randomItems)
			{
				if (item.getItemStack().getType() == other)
				{
					cont = true;
				}
			}
	
			if (cont)
				continue;
			
			items[i] = item;
			i++;
		}

		return new LootTable(_unbreakable, items);
	}
}
