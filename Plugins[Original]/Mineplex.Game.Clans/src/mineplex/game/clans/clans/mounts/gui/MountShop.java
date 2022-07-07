package mineplex.game.clans.clans.mounts.gui;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.mounts.MountManager;

public class MountShop extends ShopBase<MountManager>
{
	public MountShop(MountManager plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), "Manage Mounts");
	}

	@Override
	protected ShopPageBase<MountManager, ? extends ShopBase<MountManager>> buildPagesFor(Player player)
	{
		return new MountOverviewPage(getPlugin(), this, "Manage Mounts", player);
	}
}