package nautilus.game.pvp.modules.Benefit.UI;

import org.bukkit.entity.Player;

import nautilus.game.pvp.modules.Benefit.Items.CoinPack;

public class CoinPackButton extends BenefitItemButtonBase
{
	private CoinPack _coinPack;
	
	public CoinPackButton(BenefitShopPage shop, CoinPack coinPack)
	{
		super(shop);
		
		_coinPack = coinPack;
	}

	@Override
	public void Clicked(Player player)
	{
		Shop.PurchaseCoinPack(player, _coinPack);
	}
}
