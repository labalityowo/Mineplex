package mineplex.gemhunters.supplydrop.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.gemhunters.supplydrop.SupplyDropModule;

public class SupplyDropCommand extends MultiCommandBase<SupplyDropModule>
{
	public SupplyDropCommand(SupplyDropModule plugin)
	{
		super(plugin, SupplyDropModule.Perm.SUPPLY_DROP_COMMAND, "supplydrop", "supply", "sd");
		
		AddCommand(new StartCommand(plugin));
		AddCommand(new EndCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Command List:"));
		caller.sendMessage(F.help("/" + _aliasUsed + " start [location]", "Starts the supply drop sequence at a certain location. Leaving [location] blank picks a random one.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " stop", "Stops the current supply drop.", ChatColor.DARK_RED));
	}
}