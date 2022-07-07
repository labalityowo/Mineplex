package mineplex.core.common;

import org.bukkit.Material;

public class MaterialData
{
	private final Material _material;
	private final byte _data;

	private MaterialData(Material material, byte data)
	{
		_material = material;
		_data = data;
	}

	public static MaterialData of(Material material)
	{
		return new MaterialData(material, (byte) 0);
	}

	public static MaterialData of(Material material, byte data)
	{
		return new MaterialData(material, data);
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getData()
	{
		return _data;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MaterialData that = (MaterialData) o;

		if (_data != that._data) return false;
		return _material == that._material;

	}

	@Override
	public int hashCode()
	{
		int result = _material.hashCode();
		result = 31 * result + (int) _data;
		return result;
	}
}
