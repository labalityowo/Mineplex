package mineplex.gemhunters.economy.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.gemhunters.economy.EconomyModule;

public class GiveGemsCommand extends CommandBase<EconomyModule>
{
	public GiveGemsCommand(EconomyModule plugin)
	{
		super(plugin, EconomyModule.Perm.GIVE_GEMS_COMMAND, "givegems");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			caller.sendMessage(F.help("/" + _aliasUsed + " <player> <amount>", "Adds an amount of gems to a player's gems earned.", ChatColor.DARK_RED));
			return;
		}
		
		Player target = UtilPlayer.searchOnline(caller, args[0], true);

		if (target == null)
		{
			return;
		}
		
		try
		{
			int amount = Integer.parseInt(args[1]);
			
			Plugin.addToStore(target, "Given by " + F.name(caller.getName()), amount);
		}
		catch (NumberFormatException e)
		{
			caller.sendMessage(F.main(Plugin.getName(), "That is not a number."));
		}
	}
}