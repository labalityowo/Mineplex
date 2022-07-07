package mineplex.core.shop.item;

import org.bukkit.Material;

public interface IDisplayPackage
{
	String getName();
	String[] getDescription();
	Material getDisplayMaterial();
	byte getDisplayData();
}
