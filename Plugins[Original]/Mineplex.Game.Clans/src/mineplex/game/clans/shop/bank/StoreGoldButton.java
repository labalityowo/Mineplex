package mineplex.game.clans.shop.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.economy.GoldManager;

public class StoreGoldButton implements IButton
{
	private BankPage _page;

	public StoreGoldButton(BankPage page)
	{
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType != ClickType.LEFT)	return;
		
		if (!Recharge.Instance.use(player, "Attempt Buy Clans Shop Item", 1500, false, false))
		{
			return;
		}
		
		if (_page.hasEnoughGold())
		{
			int cost = BankPage.TOKEN_VALUE;
			GoldManager.getInstance().purchaseToken(player, cost);
			_page.refresh();
		}
	}
}