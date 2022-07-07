package mineplex.core.shop.item;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.donation.repository.GameSalesPackageToken;

public interface ICurrencyPackage
{
	int getSalesPackageId();
	int getCost(GlobalCurrency currencytype);
	boolean isFree();
	void update(GameSalesPackageToken token);
}
