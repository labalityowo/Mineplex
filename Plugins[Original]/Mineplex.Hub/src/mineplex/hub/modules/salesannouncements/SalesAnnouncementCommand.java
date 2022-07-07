package mineplex.hub.modules.salesannouncements;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class SalesAnnouncementCommand extends CommandBase<SalesAnnouncementManager>
{
	public SalesAnnouncementCommand(SalesAnnouncementManager plugin)
	{
		super(plugin, SalesAnnouncementManager.Perm.SALES_COMMAND, "sales");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 1 && args[0].equalsIgnoreCase("add"))
		{
			StringBuilder message = new StringBuilder();
			message.append(args[1]);
			for (int i = 2; i < args.length; i++)
			{
				message.append(" " + args[i]);
			}
			
			new SalesAnnouncementCreationPage(caller, message.toString());
		}
		else if (args.length >= 1)
		{
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Usage is /sales add <Message> (can take chat color codes)"));
		}
		else
		{
			new SalesAnnouncementPage(caller, Plugin);
		}
	}
}