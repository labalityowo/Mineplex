package mineplex.game.clans.shop.bank;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.game.clans.economy.GoldManager;
import mineplex.game.clans.items.CustomItem;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.economy.GoldToken;

public class CashInButton implements IButton
{
	private BankPage _page;
	
	public CashInButton(BankPage page)
	{
		_page = page;
	}
	
	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType != ClickType.LEFT && clickType != ClickType.RIGHT)	return;
		
		if (!Recharge.Instance.use(player, "Attempt Buy Clans Shop Item", 1500, false, false))
		{
			return;
		}
		
		ItemStack item = player.getItemOnCursor();
		CustomItem cursorItem = GearManager.parseItem(item);
		
		if (cursorItem instanceof GoldToken)
		{
			GoldToken token = (GoldToken) cursorItem;
			GoldManager.getInstance().cashIn(player, token);
			player.setItemOnCursor(null);	// Delete the gold token on cursor
			_page.playAcceptSound(player);
			_page.refresh();
		}
	}
}