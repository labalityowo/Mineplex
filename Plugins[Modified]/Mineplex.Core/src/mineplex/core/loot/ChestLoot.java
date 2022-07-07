package mineplex.core.loot;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilMath;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestLoot
{
	private ArrayList<RandomItem> _randomItems = new ArrayList<RandomItem>();
	private int _totalLoot;
	private boolean _unbreakableLoot;

	public ChestLoot()
	{
		this(false);
	}

	public ChestLoot(boolean unbreakableLoot)
	{
		_unbreakableLoot = unbreakableLoot;
	}

	public void cloneLoot(ChestLoot loot)
	{
		_totalLoot += loot._totalLoot;
		_randomItems.addAll(loot._randomItems);
	}

	public ItemStack getLoot()
	{
		return getLoot(new ArrayList<>());
	}
	
	public ItemStack getLoot(ArrayList<Material> exclude)
	{
		int totalLoot = _totalLoot;
		ArrayList<RandomItem> items = (ArrayList<RandomItem>) _randomItems.clone();
		
		Iterator<RandomItem> rItems = items.iterator();
		while (rItems.hasNext())
		{
			RandomItem item = rItems.next();
			
			for (Material mat : exclude)
			{
				if (item.getItemStack().getType() == mat)
				{
					totalLoot -= item.getAmount();
					rItems.remove();
				}
			}
		}
		
		int no = UtilMath.r(totalLoot);

		for (RandomItem item : items)
		{
			no -= item.getAmount();

			if (no < 0)
			{
				ItemStack itemstack = item.getItemStack();

				if (_unbreakableLoot && itemstack.getType().getMaxDurability() > 16)
				{
					ItemMeta meta = itemstack.getItemMeta();
					meta.spigot().setUnbreakable(true);
					itemstack.setItemMeta(meta);
				}

				return itemstack;
			}
		}

		return null;
	}
	
	public void addLoot(ItemStack item, int amount)
	{
		addLoot(item, amount, item.getAmount(), item.getAmount());
	}

	public void addLoot(ItemStack item, int amount, int minStackSize, int maxStackSize)
	{
		addLoot(new RandomItem(item, amount, minStackSize, maxStackSize));
	}

	public void addLoot(Material material, int amount)
	{
		addLoot(material, amount, 1, 1);
	}

	public void addLoot(Material material, int amount, int minStackSize, int maxStackSize)
	{
		addLoot(new ItemStack(material), amount, minStackSize, maxStackSize);
	}

	public void addLoot(RandomItem item)
	{
		_totalLoot += item.getAmount();
		_randomItems.add(item);
	}
}
