package mineplex.game.clans.clans.loot;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;

public class CustomItemLoot implements ILoot
{
	private Material _material;
	private byte _data;
	private int _min;
	private int _max;
	private String _displayName;
	private String[] _lore;
	
	public CustomItemLoot(Material material)
	{
		this(material, 1, 1);
	}
	
	public CustomItemLoot(Material material, int min, int max)
	{
		this(material, (byte) 0, min, max, null, new String[] {});
	}
	
	public CustomItemLoot(Material material, int min, int max, String displayName, String... lore)
	{
		this(material, (byte) 0, min, max, displayName, lore);
	}
	
	public CustomItemLoot(Material material, byte data, int min, int max, String displayName, String... lore)
	{
		_material = material;
		_data = data;
		_min = min;
		_max = max;
		_displayName = C.Reset + displayName;
		_lore = lore;
	}
	
	@Override
	public void dropLoot(Location location)
	{
		location.getWorld().dropItemNaturally(location.clone().add(0, 3, 0), getItemStack());
	}
	
	@Override
	public ItemStack getItemStack()
	{
		ItemStack stack = new ItemStack(_material, UtilMath.rRange(_min, _max), _data);
		
		ItemMeta meta = stack.getItemMeta();
		
		if (meta == null)
		{
			meta = Bukkit.getItemFactory().getItemMeta(_material);
		}
		
		if (meta != null && _displayName != null)
		{
			meta.setDisplayName(_displayName);
		}
		
		if (meta != null && _lore != null && _lore.length > 0)
		{
			meta.setLore(Arrays.asList(_lore));
		}
		
		if (meta != null)
		{
			stack.setItemMeta(meta);
		}
		
		return stack;
	}
}