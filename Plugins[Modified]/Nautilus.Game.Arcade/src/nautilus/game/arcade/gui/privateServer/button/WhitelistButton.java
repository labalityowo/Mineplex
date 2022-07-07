package nautilus.game.arcade.gui.privateServer.button;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import nautilus.game.arcade.gui.privateServer.page.WhitelistedPage;

public class WhitelistButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private PrivateServerShop _shop;

	public WhitelistButton(ArcadeManager arcadeManager, PrivateServerShop shop)
	{
		_shop = shop;
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_shop.openPageForPlayer(player, new WhitelistedPage(_arcadeManager, _shop, player));
	}
}
