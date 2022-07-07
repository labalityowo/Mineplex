package mineplex.staffServer.salespackage.command;

import java.time.LocalDate;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class PowerPlayCommand extends CommandBase<SalesPackageManager>
{
	public PowerPlayCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "powerplay");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length < 2)
			return;

		final String playerName = args[0];
		final String duration = args[1];
		
		if (!duration.equalsIgnoreCase("month") && !duration.equalsIgnoreCase("year"))
		{
			return;
		}
		
		Plugin.getClientManager().loadClientByName(playerName, client ->
		{
			if (client != null)
			{
				Plugin.getPowerPlay().addSubscription(client.getAccountId(), LocalDate.now(), duration.toLowerCase());
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Given a Power Play Club 1 " + duration + " subscription to " + playerName + "!"));
			}
			else
			{
				caller.sendMessage(F.main(Plugin.getName(), "Couldn't find " + playerName + "'s account!"));
			}
		});
	}
}