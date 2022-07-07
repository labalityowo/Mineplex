package nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.page.MenuPage;

public class PlayerHeadButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private MenuPage _menuPage;

	public PlayerHeadButton(ArcadeManager arcadeManager, MenuPage menuPage)
	{
		_arcadeManager = arcadeManager;
		_menuPage = menuPage;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		int maxPlayers = _arcadeManager.GetServerConfig().MaxPlayers;
		int newMax;

		int maxCap = _arcadeManager.GetGameHostManager().getMaxPlayerCap();

		if (_arcadeManager.GetGameHostManager().isEventServer())
			maxCap = 120;
		
		if (clickType.isLeftClick())
			newMax = ++maxPlayers > maxCap ? maxCap : maxPlayers;
		else
			newMax = --maxPlayers < 2 ? 2 : maxPlayers;

		_arcadeManager.GetServerConfig().MaxPlayers = newMax;
		_menuPage.refresh();
	}
}
