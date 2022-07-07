package nautilus.game.arcade.gui.privateServer.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;

public class StopGameButton implements IButton
{
	private ArcadeManager _arcadeManager;

	public StopGameButton(ArcadeManager arcadeManager)
	{
		_arcadeManager = arcadeManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (_arcadeManager.GetGame().GetState() == GameState.End || _arcadeManager.GetGame().GetState() == GameState.End)
		{
			player.sendMessage("Game is already ending..."); 
			return;
		}
		else if (_arcadeManager.GetGame().GetState() == GameState.Recruit)
		{
			_arcadeManager.GetGame().SetState(GameState.Dead);
		}
		else
		{
			_arcadeManager.GetGame().SetState(GameState.End);
		}


		_arcadeManager.GetGame().Announce(C.cAqua + C.Bold + player.getName() + " has stopped the game.");
	}
}
