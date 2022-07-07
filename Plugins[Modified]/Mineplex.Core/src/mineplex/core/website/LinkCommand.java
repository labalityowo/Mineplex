package mineplex.core.website;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class LinkCommand extends CommandBase<WebsiteLinkManager>
{
	public LinkCommand(WebsiteLinkManager plugin)
	{
		super(plugin, WebsiteLinkManager.Perm.LINK_COMMAND, "link");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/" + _aliasUsed + " XXX-XXX-XXX", "Begins linking your Minecraft account with your website link code.", ChatColor.GREEN));
		}
		else
		{
			if (Plugin.Get(caller).Linked)
			{
				UtilPlayer.message(caller, F.main(Plugin.getName(), "Your account is already linked!"));
			}
			else
			{
				Plugin.startLink(caller, args[0]);
			}
		}
	}
}