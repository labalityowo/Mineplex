package mineplex.core.pet.sales;

import org.bukkit.Material;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.shop.item.SalesPackageBase;

public class PetExtraSalesPackage extends SalesPackageBase
{

	public PetExtraSalesPackage(String name, Material material, int price)
	{
		super(name, material, (byte)0, new String[0]);
		CurrencyCostMap.put(GlobalCurrency.TREASURE_SHARD, price);

		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}
}
