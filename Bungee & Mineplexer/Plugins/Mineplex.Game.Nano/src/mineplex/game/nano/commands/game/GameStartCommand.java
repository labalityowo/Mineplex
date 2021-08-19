package mineplex.game.nano.commands.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;
import mineplex.game.nano.game.Game;

public class GameStartCommand extends CommandBase<NanoManager>
{

	GameStartCommand(NanoManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND, "start");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Game game = Plugin.getGame();

		if (game == null)
		{
			Bukkit.broadcastMessage(C.cAquaB + caller.getName() + " started the game");
			Plugin.getGameCycle().checkForDeadGame(true);
		}
		else
		{
			caller.sendMessage(F.main(Plugin.getName(), "There is already a running game!"));
		}
	}
}
