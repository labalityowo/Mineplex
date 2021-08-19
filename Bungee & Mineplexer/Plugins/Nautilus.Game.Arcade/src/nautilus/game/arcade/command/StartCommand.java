package nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;

public class StartCommand extends CommandBase<ArcadeManager>
{
	public StartCommand(ArcadeManager plugin)
	{
		super(plugin, GameCommand.Perm.GAME_COMMAND_DUMMY_PERM, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (!Plugin.canPlayerUseGameCmd(caller))
		{
			return;
		}

		Game game = Plugin.GetGame();

		if (game == null)
		{
			caller.sendMessage(F.main("Game", "There is currently no game running!"));
			return;
		}

		GameState state = game.GetState();

		if (state == GameState.PreLoad || state == GameState.Loading)
		{
			UtilPlayer.message(caller, F.main("Game", "The game is currently loading, it cannot be started!"));
			return;
		}

		if (!Plugin.GetGame().inLobby())
		{
			UtilPlayer.message(caller, F.main("Game", "The game is already starting, it cannot be started again!"));
			return;
		}

		int seconds = 10;

		if (args.length > 0)
		{
			try
			{
				seconds = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException ex)
			{
				UtilPlayer.message(caller, F.main("Game", F.elem(args[0]) + " is not a number!"));
			}
		}

		Plugin.GetGameManager().StateCountdown(Plugin.GetGame(), seconds, true);

		Plugin.GetGame().Announce(C.cAqua + C.Bold + caller.getName() + " has started the game.");
	}
}