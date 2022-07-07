package mineplex.game.clans.shop.pvp;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.mounts.Mount.MountType;
import mineplex.game.clans.clans.mounts.MountClaimToken;
import mineplex.game.clans.economy.GoldManager;

public class MountBuyButton<T extends ShopPageBase<?, ?>> implements IButton
{
	private int _buyPrice;
	private ItemStack _item;
	private T _page;
	
	public MountBuyButton(T page)
	{
		_page = page;
		_buyPrice = 150000;
		_item = new MountClaimToken(1, 1, 1, MountType.HORSE).toItem();
	}
	
	@Override
	public void onClick(final Player player, ClickType clickType)
	{
		if (!Recharge.Instance.use(player, "Attempt Buy Clans Shop Item", 1500, false, false))
		{
			return;
		}
		if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.LEFT)
		{
			int goldCount = GoldManager.getInstance().getGold(player);
			
			if (goldCount >= _buyPrice)
			{
				GoldManager.getInstance().deductGold(success ->
				{
					if (success)
					{
						giftItem(player, 1);
						GoldManager.notify(player, String.format("You have purchased %d item(s) for %dg", 1, _buyPrice));

						_page.playAcceptSound(player);
					}
					else
					{
						GoldManager.notify(player, "You cannot afford that item! Please relog to update your gold count.");
						_page.playDenySound(player);
					}

					_page.refresh();
				}, player, _buyPrice);
			}
			else
			{
				GoldManager.notify(player, "You cannot afford that item.");
				_page.playDenySound(player);
			}
		}
		
		_page.refresh();
	}
	
	private void giftItem(Player player, int amount)
	{
		ItemStack item = _item.clone();
		item.setAmount(amount);
		player.getInventory().addItem(item);
	}
}