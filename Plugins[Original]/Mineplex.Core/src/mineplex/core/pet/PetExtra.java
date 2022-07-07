package mineplex.core.pet;

import org.bukkit.Material;

import mineplex.core.pet.sales.PetExtraSalesPackage;

public enum PetExtra
{
	NAME_TAG("Name Tag", Material.NAME_TAG, 100)
	;
	private final String _name;
	private final Material _material;
	private final int _price;

	PetExtra(String name, Material material, int price)
	{
		_name = name;
		_material = material;
		_price = price;
	}

	public String getName()
	{
		return _name;
	}

	public Material getMaterial()
	{
		return _material;
	}

	public int getPrice()
	{
		return _price;
	}

	public PetExtraSalesPackage toSalesPackage(String text)
	{
		return new PetExtraSalesPackage(text, _material, _price);
	}
}
