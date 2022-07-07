package mineplex.game.clans.clans.siege.weapon.util;

import org.bukkit.Material;

public class WeaponStateInfo
{
	private Material _material;
	private byte _data;
	
	public WeaponStateInfo(Material material, byte data)
	{
		_material = material;
		_data = data;
	}
	
	public Material getType()
	{
		return _material;
	}
	
	public byte getData()
	{
		return _data;
	}
}
