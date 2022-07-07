package nautilus.game.arcade.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

import nautilus.game.arcade.ArcadeManager;
import static nautilus.game.arcade.command.SetCommand.MAP_PREFIX;
import static nautilus.game.arcade.command.SetCommand.MODE_PREFIX;
import static nautilus.game.arcade.command.SetCommand.SOURCE_PREFIX;

public class GameCommand extends MultiCommandBase<ArcadeManager>
{
	public enum Perm implements Permission
	{
		GAME_COMMAND_DUMMY_PERM,
	}

	public GameCommand(ArcadeManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND_DUMMY_PERM, "game");

		AddCommand(new StartCommand(Plugin));
		AddCommand(new StopCommand(Plugin));
		AddCommand(new SetCommand(Plugin));

		PermissionGroup.PLAYER.setPermission(Perm.GAME_COMMAND_DUMMY_PERM, true, true);
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		if (Plugin.canPlayerUseGameCmd(caller))
		{
			UtilPlayer.message(caller, F.main("Game", "Available Commands"));
			UtilPlayer.message(caller, F.help("/game start", "Start the current game", ChatColor.DARK_RED));
			UtilPlayer.message(caller, F.help("/game stop", "Stop the current game", ChatColor.DARK_RED));
			caller.sendMessage(F.help(String.format("/game set <gametype> [%s(gamemode)] [%s(mapsource)] [%s(mapname)]", MODE_PREFIX, SOURCE_PREFIX, MAP_PREFIX), "Set the current game or next game", ChatColor.DARK_RED));
		}
	}
}