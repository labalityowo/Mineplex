package nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.managers.GameHostManager;

public class KillButton implements IButton
{
	private ArcadeManager _arcade;
	private GameHostManager _manager;

	public KillButton(ArcadeManager arcadeManager)
	{
		_manager = arcadeManager.GetGameHostManager();
		_arcade = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType == clickType.SHIFT_RIGHT)
		{
			_manager.setHostExpired(true, "The host has closed this Mineplex Private Server.");
		}
	}
}
