package mineplex.core.mission.ui;

import org.bukkit.entity.Player;

import mineplex.core.mission.MissionManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class MissionShop extends ShopBase<MissionManager>
{

	public MissionShop(MissionManager plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), "Mission");
	}

	@Override
	protected ShopPageBase<MissionManager, ? extends ShopBase<MissionManager>> buildPagesFor(Player player)
	{
		return new MissionMainPage(getPlugin(), this, player);
	}
}
