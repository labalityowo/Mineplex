package mineplex.core.common.structs;

import org.bukkit.Material;

public class ItemContainer 
{
	public Material Type;
	public byte Data;
	public String Name;
	
	public ItemContainer(Material type, byte data, String name)
	{
		Type = type;
		Data = data;
		Name = name;
	}
	
	public ItemContainer(int id, byte data, String name)
	{
		Type = Material.getMaterial(id);
		Data = data;
		Name = name;
	}
}
