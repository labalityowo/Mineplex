package nautilus.game.arcade.game.games.build;

import org.bukkit.Material;

import mineplex.core.common.block.schematic.Schematic;

public class GroundData
{
	private final Material _material;
	private final byte _data;
	private final String _name;
	private final Schematic _schematic;

	public GroundData(Material material)
	{
		this(material, (byte) 0);
	}

	public GroundData(Material material, byte data)
	{
		this(material, data, null);
	}
	
	public GroundData(Material material, byte data, String name)
	{
		this(material, data, name, null);
	}
	
	public GroundData(Material material, byte data, String name, Schematic schematic)
	{
		_material = material;
		_data = data;
		_name = name;
		_schematic = schematic;
	}

	public Material getMaterial()
	{
		return _material;
	}
	
	public byte getData()
	{
		return _data;
	}

	public String getName()
	{
		return _name;
	}
	
	public boolean hasName()
	{
		return _name != null;
	}
	
	public Schematic getSchematic()
	{
		return _schematic;
	}
	
	public boolean hasSchematic()
	{
		return _schematic != null;
	}
}