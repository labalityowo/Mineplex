package nautilus.game.arcade.game.games.build.gui;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.gui.page.MobPage;

public class MobShop extends ShopBase<ArcadeManager>
{
	public MobShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Mob Options");
	}

	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player, BuildData data, Entity entity)
	{
		return new MobPage(getPlugin(), this, getClientManager(), getDonationManager(), player, data, entity);
	}

	public boolean attemptShopOpen(Player player, BuildData data, Entity entity)
	{
		if (!getOpenedShop().contains(player.getUniqueId()))
		{
			if (!canOpenShop(player))
				return false;

			getOpenedShop().add(player.getUniqueId());

			openShopForPlayer(player);
			if (!getPlayerPageMap().containsKey(player.getUniqueId()))
			{
				getPlayerPageMap().put(player.getUniqueId(), buildPagesFor(player, data, entity));
			}

			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
