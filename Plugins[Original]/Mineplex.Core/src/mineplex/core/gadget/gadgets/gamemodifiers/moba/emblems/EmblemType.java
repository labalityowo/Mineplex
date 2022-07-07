package mineplex.core.gadget.gadgets.gamemodifiers.moba.emblems;

import org.bukkit.Material;

import mineplex.core.treasure.reward.RewardRarity;

public enum EmblemType
{

	HEART("Heart", "Heart", Material.NAME_TAG, RewardRarity.RARE),

	;

	private final String _name;
	private final String _schematic;
	private final Material _material;
	private final byte _materialData;
	private final RewardRarity _rarity;

	EmblemType(String name, String schematic, Material material, RewardRarity rarity)
	{
		this(name, schematic, material, 0, rarity);
	}

	EmblemType(String name, String schematic, Material material, int materialData, RewardRarity rarity)
	{
		_name = name;
		_schematic = schematic;
		_material = material;
		_materialData = (byte) materialData;
		_rarity = rarity;
	}

	public String getName()
	{
		return _name + " Emblem";
	}

	public String getSchematic()
	{
		return _schematic;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public byte getMaterialData()
	{
		return _materialData;
	}

	public RewardRarity getRarity()
	{
		return _rarity;
	}
}
