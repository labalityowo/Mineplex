package nautilus.game.arcade.command;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;

public class TournamentStopCommand extends CommandBase<ArcadeManager>
{
	public TournamentStopCommand(ArcadeManager plugin)
	{
		super(plugin, ArcadeManager.Perm.TOURNAMENT_STOP_COMMAND, "tstopgame");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.IsTournamentServer())
		{
			UtilPlayer.message(caller, F.main("Game", "This command can only be used on tournament servers!"));
			return;
		}

		if (Plugin.GetGame() == null)
		{
			UtilPlayer.message(caller, F.main("Game", "There is no game to stop!"));
			return;
		}

		if (Plugin.GetGame().GetState() == Game.GameState.End)
		{
			UtilPlayer.message(caller, F.main("Game", "The game is already ending, it cannot be ended again"));
			return;
		}
		else if (Plugin.GetGame().GetState() == Game.GameState.Recruit)
		{
			Plugin.GetGame().SetState(Game.GameState.Dead);
		}
		else
		{
			Plugin.GetGame().SetState(Game.GameState.End);
		}

		HandlerList.unregisterAll(Plugin.GetGame());
		Plugin.GetGame().Announce(C.cAqua + C.Bold + caller + " has stopped the game.");
	}
}
