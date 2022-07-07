package nautilus.game.arcade.game.games.moba.progression.ui;

import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import org.bukkit.entity.Player;

public class MobaRoleShop extends ShopBase<ArcadeManager>
{

	public MobaRoleShop(ArcadeManager plugin)
	{
		super(plugin, plugin.GetClients(), plugin.GetDonation(), "Moba Heroes");
	}

	public void openShop(Player player, MobaRole role)
	{
		openPageForPlayer(player, new MobaRolePage(getPlugin(), this, getClientManager(), getDonationManager(), player, (Moba) getPlugin().GetGame(), role));
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
