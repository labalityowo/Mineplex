package mineplex.core.friend.ui;

import org.bukkit.entity.Player;

import mineplex.core.friend.FriendManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class FriendShop extends ShopBase<FriendManager>
{

	public FriendShop(FriendManager plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), plugin.getName());
	}

	@Override
	protected ShopPageBase<FriendManager, ? extends ShopBase<FriendManager>> buildPagesFor(Player player)
	{
		return new FriendMainPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	public void updatePages()
	{
		getPageMap().values().forEach(ShopPageBase::refresh);
	}
}
