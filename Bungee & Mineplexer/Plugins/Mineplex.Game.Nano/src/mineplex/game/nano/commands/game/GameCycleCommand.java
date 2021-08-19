package mineplex.game.nano.commands.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;

public class GameCycleCommand extends CommandBase<NanoManager>
{

	GameCycleCommand(NanoManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND, "cycle");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		boolean newState = !Plugin.getGameCycle().isTestingMode();

		Bukkit.broadcastMessage(C.cAquaB + caller.getName() + " set testing mode to " + newState + ".");
		Plugin.getGameCycle().setTestingMode(newState);
	}
}
