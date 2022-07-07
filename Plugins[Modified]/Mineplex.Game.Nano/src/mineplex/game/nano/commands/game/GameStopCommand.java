package mineplex.game.nano.commands.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;

public class GameStopCommand extends CommandBase<NanoManager>
{

	GameStopCommand(NanoManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND, "stop");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Game game = Plugin.getGame();

		if (game == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "No game running."));
		}
		else if (game.getState() != GameState.End)
		{
			Bukkit.broadcastMessage(C.cAquaB + caller.getName() + " stopped the game");
			game.setState(GameState.End);
		}
	}
}
