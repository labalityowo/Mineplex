package mineplex.staffServer.customerSupport;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.staffServer.ui.SupportShop;

public class CheckCommand extends CommandBase<CustomerSupport>
{
	private SupportShop _supportShop;

	public CheckCommand(CustomerSupport plugin)
	{
		super(plugin, CustomerSupport.Perm.CHECK_COMMAND, "check", "c");

		_supportShop = new SupportShop(plugin);
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length < 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Usage: " + C.cYellow + "/check <player>"));
			return;
		}

		String playerName = args[0];

		Player onlinePlayer = UtilPlayer.searchExact(playerName);
		if (onlinePlayer != null && Plugin.getClientManager().Get(onlinePlayer) != null)
		{
			_supportShop.handleOpen(caller, Plugin.getClientManager().Get(onlinePlayer));
			return;
		}

		_commandCenter.GetClientManager().checkPlayerName(caller, playerName, name ->
		{
			if (name != null)
			{
				_commandCenter.GetClientManager().loadClientByName(name, client ->
				{
					if (client != null)
					{
						_supportShop.handleOpen(caller, client);
					}
					else
					{
						UtilPlayer.message(caller, F.main(Plugin.getName(), "Could not load data for " + C.cYellow + playerName));
					}
				});
			}
			else
			{
				caller.sendMessage(F.main(Plugin.getName(), "Could not load data for " + C.cYellow + playerName));
			}
		});
	}
}
