package nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;

public class StartGameButton implements IButton
{
	private ArcadeManager _arcadeManager;

	public StartGameButton(ArcadeManager arcadeManager)
	{
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (_arcadeManager.GetGame().GetState() != GameState.Recruit)
		{
			player.sendMessage("Game is already in progress...");
			return;
		}

		_arcadeManager.GetGameManager().StateCountdown(_arcadeManager.GetGame(), 20, true);

		_arcadeManager.GetGame().Announce(C.cAqua + C.Bold + player.getName() + " has started the game.");
	}
}
