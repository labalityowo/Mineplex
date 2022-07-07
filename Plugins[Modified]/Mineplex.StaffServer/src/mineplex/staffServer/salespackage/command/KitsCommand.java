package mineplex.staffServer.salespackage.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class KitsCommand extends CommandBase<SalesPackageManager>
{
	public KitsCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "kits");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length != 1)
			return;
		
		String playerName = args[0];
		
		Plugin.getDonationManager().applyKits(playerName);
		caller.sendMessage(F.main(Plugin.getName(), "Unlocked kits for " + playerName + "'s account!"));
	}
}