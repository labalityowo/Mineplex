package mineplex.minecraft.game.classcombat.shop.salespackage;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.minecraft.game.classcombat.item.Item;
import org.bukkit.Material;

public class ItemSalesPackage extends SalesPackageBase
{
	public ItemSalesPackage(Item item)
	{
		super("Champions " + item.GetName(), Material.BOOK, (byte)0, item.GetDesc(), item.GetGemCost());
		Free = item.isFree();
		KnownPackage = false;
		CurrencyCostMap.put(GlobalCurrency.GEM, item.GetGemCost());
	}
}
