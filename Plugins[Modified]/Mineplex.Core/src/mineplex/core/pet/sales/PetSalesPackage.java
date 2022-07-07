package mineplex.core.pet.sales;

import org.bukkit.Material;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.pet.PetType;
import mineplex.core.shop.item.SalesPackageBase;

public class PetSalesPackage extends SalesPackageBase
{

	@SuppressWarnings("deprecation")
	public PetSalesPackage(PetType type, String tagName)
	{
		super(type.getName(), Material.MONSTER_EGG, (byte)type.getEntityType().getTypeId(), new String[0]);
		CurrencyCostMap.put(GlobalCurrency.TREASURE_SHARD, type.getPrice());
		KnownPackage = false;

		setDisplayName(C.cGreen + "Purchase " + tagName);
	}
}
