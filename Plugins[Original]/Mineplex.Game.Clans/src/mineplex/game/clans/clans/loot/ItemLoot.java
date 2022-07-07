package mineplex.game.clans.clans.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilMath;

public class ItemLoot implements ILoot
{
	private final Material _material;
	private final byte _data;
	private final int _min, _max;

	public ItemLoot(Material material)
	{
		this(material, 1, 1);
	}

	public ItemLoot(Material material, int min, int max)
	{
		this(material, (byte) 0, min, max);
	}

	public ItemLoot(Material material, byte data, int min, int max)
	{
		_material = material;
		_data = data;
		_min = min;
		_max = max;
	}

	@Override
	public void dropLoot(Location location)
	{
		location.getWorld().dropItemNaturally(location.clone().add(0, 1, 0), getItemStack());
	}

	@Override
	public ItemStack getItemStack()
	{
		int count = UtilMath.rRange(_min, _max);
		return new ItemStack(_material, count, (short) 0, _data);
	}
}