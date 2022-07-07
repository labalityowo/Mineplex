package mineplex.core.gadget.gadgets;

import mineplex.core.shop.item.SalesPackageBase;
import org.bukkit.Material;

public class Ammo extends SalesPackageBase
{
	public Ammo(String name, String displayName, Material material, byte displayData, String[] description, int coins, int quantity)
	{
		super(name, material, displayData, description, coins, quantity);
		
		DisplayName = displayName;
		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}
}
