package mineplex.game.clans.clans.bosstoken;

import org.bukkit.entity.Player;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;

public class BossTokenShop extends ShopBase<WorldEventManager>
{
	public BossTokenShop(WorldEventManager plugin)
	{
		super(plugin, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), "Boss Token");
	}

	@Override
	protected ShopPageBase<WorldEventManager, ? extends ShopBase<WorldEventManager>> buildPagesFor(Player player)
	{
		return new BossTokenPage(getPlugin(), this, "Boss Tokens", player);
	}
}