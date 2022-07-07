package mineplex.core.itemstack;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;;

public class EnchantedBookBuilder
{
	private Map<Enchantment, Integer> _enchantments;
	private int _amount;
	
	public EnchantedBookBuilder(int amount)
	{
		_enchantments = new HashMap<>();
		_amount = amount;
	}
	
	public Map<Enchantment, Integer> getEnchantments()
	{
		return _enchantments;
	}
	
	public Integer getLevel(Enchantment enchantment)
	{
		return _enchantments.getOrDefault(enchantment, 0);
	}
	
	public EnchantedBookBuilder setLevel(Enchantment enchantment, Integer level)
	{
		if (level <= 0)
		{
			_enchantments.remove(enchantment);
		}
		else
		{
			_enchantments.put(enchantment, level);
		}
		
		return this;
	}
	
	public EnchantedBookBuilder removeEnchantment(Enchantment enchantment)
	{
		_enchantments.remove(enchantment);
		
		return this;
	}
	
	public ItemStack build()
	{
		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, _amount);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
		
		_enchantments.entrySet().forEach(entry -> meta.addStoredEnchant(entry.getKey(), entry.getValue(), true));
		item.setItemMeta(meta);
		
		return item;
	}
}