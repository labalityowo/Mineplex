package mineplex.core.account.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class RanksCommand extends MultiCommandBase<CoreClientManager>
{
	public RanksCommand(CoreClientManager plugin)
	{
		super(plugin, CoreClientManager.Perm.RANK_COMMAND, "rank", "ranks", "permissions");
		
		AddCommand(new AddRankCommand(plugin));
		AddCommand(new InfoPlayerCommand(plugin));
		AddCommand(new ListRanksCommand(plugin));
		AddCommand(new RemoveRankCommand(plugin));
		AddCommand(new ResetPlayerCommand(plugin));
		AddCommand(new SetRankCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " clear <Player>", "Resets a player's ranks to default", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " set <Player> <Rank>", "Sets a player's primary rank", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " add <Player> <Rank>", "Adds a sub-rank to a player", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " remove <Player> <Rank>", "Removes a sub-rank from a player", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " list", "Lists all existing ranks", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/" + _aliasUsed + " info <Player>", "Displays a player's rank information", ChatColor.DARK_RED));
	}
}