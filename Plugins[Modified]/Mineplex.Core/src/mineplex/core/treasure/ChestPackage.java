package mineplex.core.treasure;

import mineplex.core.shop.item.SalesPackageBase;
import org.bukkit.Material;

public class ChestPackage extends SalesPackageBase
{
	public ChestPackage(String name, Material mat, int cost)
	{
		super(name, mat, (byte) 0, new String[] {}, cost);
 
		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}
}
