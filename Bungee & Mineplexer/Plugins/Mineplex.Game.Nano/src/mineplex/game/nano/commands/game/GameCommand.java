package mineplex.game.nano.commands.game;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;

public class GameCommand extends MultiCommandBase<NanoManager>
{

	public GameCommand(NanoManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND, "game");

		AddCommand(new GameSetCommand(plugin));
		AddCommand(new GameStartCommand(plugin));
		AddCommand(new GameStopCommand(plugin));
		AddCommand(new GameCycleCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.help("/" + _aliasUsed + " set <game> [map]", "Sets the next game and map", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " start", "Forcefully starts the game", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " stop", "Stops the current game", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " cycle", "Toggles the game cycle", ChatColor.DARK_RED));
	}
}
