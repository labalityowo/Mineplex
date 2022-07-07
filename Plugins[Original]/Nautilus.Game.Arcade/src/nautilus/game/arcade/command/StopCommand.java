package nautilus.game.arcade.command;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;

public class StopCommand extends CommandBase<ArcadeManager>
{
	public StopCommand(ArcadeManager plugin)
	{
		super(plugin, GameCommand.Perm.GAME_COMMAND_DUMMY_PERM, "stop");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.canPlayerUseGameCmd(caller))
		{
			return;
		}

		if (Plugin.GetGame() == null)
		{
			UtilPlayer.message(caller, F.main("Game", "There is no game to stop!"));
			return;
		}

		if (Plugin.GetGame().GetState() == GameState.End)
		{
			UtilPlayer.message(caller, F.main("Game", "The game is already ending, it cannot be ended again"));
			return;
		}
		else if (Plugin.GetGame().inLobby())
		{
			Plugin.GetGame().SetState(GameState.Dead);
		}
		else
		{
			Plugin.GetGame().SetState(GameState.End);
		}

		HandlerList.unregisterAll(Plugin.GetGame());

		Plugin.GetGame().Announce(C.cAqua + C.Bold + caller.getName() + " has stopped the game.");
	}
}