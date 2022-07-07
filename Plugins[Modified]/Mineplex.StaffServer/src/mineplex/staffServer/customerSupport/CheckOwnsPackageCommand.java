package mineplex.staffServer.customerSupport;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class CheckOwnsPackageCommand extends CommandBase<CustomerSupport>
{
	public CheckOwnsPackageCommand(CustomerSupport plugin)
	{
		super(plugin, CustomerSupport.Perm.CHECK_OWNS_PACKAGE_COMMAND, "checkownspackage");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args == null || args.length < 2)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage: /" + _aliasUsed + " <Player> <Package>"));
		}
		else
		{
			String playerName = args[0];
			String packageName = args[1];
			for (int i = 2; i < args.length; i++)
			{
				packageName += (" " + args[i]);
			}
			
			final String salesPackage = packageName;

			_commandCenter.GetClientManager().checkPlayerName(caller, playerName, name ->
			{
				if (name != null)
				{
					_commandCenter.GetClientManager().loadClientByName(name, client ->
					{
						if (client != null)
						{
							caller.sendMessage(F.main(Plugin.getName(),
									"Package "
											+ C.cYellow + salesPackage
											+ C.mBody + " unlocked for " + F.name(playerName) + ": "
											+ (Plugin.getDonationManager().Get(client.getUniqueId()).ownsUnknownSalesPackage(salesPackage) ? C.cGreen + "YES" : C.cRed + "NO")
							));
						}
						else
						{
							UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not load data for " + name));
						}
					});
				}
			});
		}
	}
}