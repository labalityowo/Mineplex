package nautilus.game.pvp.modules.Benefit.Items;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.CurrencyType;
import mineplex.minecraft.account.GetClientEvent;

import nautilus.game.pvp.modules.Benefit.BenefitManager;

public class CoinPack extends BenefitItem
{
	private int _coinValue;
	
	public CoinPack(BenefitManager plugin, String name, Material material, int coinValue)
	{
		super(plugin, name, material);
		
		_coinValue = coinValue;
	}
	
	public void Sold(Player player, CurrencyType currencyType)
	{
		GetClientEvent clientEvent = new GetClientEvent(player);
		
		Plugin.GetPluginManager().callEvent(clientEvent);
		
		clientEvent.GetClient().Game().SetEconomyBalance(_coinValue + clientEvent.GetClient().Game().GetEconomyBalance());
		clientEvent.GetClient().Donor().DeductCost(GetCost(currencyType), currencyType, false);
	}
}
