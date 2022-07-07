package mineplex.core.tournament;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;

public class DebugShopCommand extends CommandBase<TournamentManager>
{
	public DebugShopCommand(TournamentManager plugin)
	{
		super(plugin, TournamentManager.Perm.DEBUG_SHOP_COMMAND, "ots");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.openShop(caller);
	}
}