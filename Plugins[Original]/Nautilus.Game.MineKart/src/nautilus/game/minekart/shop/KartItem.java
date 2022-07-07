package nautilus.game.minekart.shop;

import mineplex.core.common.CurrencyType;
import mineplex.core.shop.item.SalesPackageBase;
import nautilus.game.minekart.kart.KartType;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KartItem extends SalesPackageBase
{
	private KartType _kartType;
	
	public KartItem(Material monsterEgg, KartType kartType)
	{
		super(kartType.GetName(), monsterEgg, (byte)0, kartType.GetDescription());
		
		_kartType = kartType;
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType)
	{

	}
	
	public KartType GetKartType()
	{
		return _kartType;
	}
}
