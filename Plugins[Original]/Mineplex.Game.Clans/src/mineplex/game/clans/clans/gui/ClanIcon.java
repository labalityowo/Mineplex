package mineplex.game.clans.clans.gui;

import org.bukkit.Material;

public enum ClanIcon
{
	JOIN(Material.PRISMARINE, (byte) 1),
	LEAVE(Material.PRISMARINE, (byte) 2),
	TERRITORY(Material.PRISMARINE, (byte) 0),
	MEMBER(Material.WATER_BUCKET, (byte) 0),
	COMMANDS(Material.LAVA_BUCKET, (byte) 0),
	ENERGY(Material.SEA_LANTERN, (byte) 0),
	CASTLE(Material.RECORD_9, (byte) 0),
	WAR(Material.RECORD_11, (byte) 0),
	ALLIANCE(Material.RECORD_10, (byte) 0);

	private Material _material;
	private byte _data;

	ClanIcon(Material material, byte data)
	{
		_material = material;
		_data = data;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getData()
	{
		return _data;
	}
}
