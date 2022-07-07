package mineplex.game.clans.shop.bank;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.Callback;
import mineplex.core.donation.DonationManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.economy.GoldToken;

public class GemTransferButton implements IButton
{
	private BankPage _page;
	private int _gemAmount;
	
	public GemTransferButton(BankPage page, int gemAmount)
	{
		_page = page;
		_gemAmount = gemAmount;
	}
	
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType != ClickType.LEFT)	return;
		
		if (!Recharge.Instance.use(player, "Attempt Buy Clans Shop Item", 1500, false, false))
		{
			return;
		}
		
		if (GoldManager.getInstance().canTransferGems(player))
		{
			GoldManager.getInstance().transferGemsToCoins(player, _gemAmount);
			_page.refresh();
		}
	}

}
