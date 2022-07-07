package mineplex.core.website;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;

public class UnlinkCommand extends CommandBase<WebsiteLinkManager>
{
	public UnlinkCommand(WebsiteLinkManager plugin)
	{
		super(plugin, WebsiteLinkManager.Perm.UNLINK_COMMAND, "unlink");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 1)
		{
			UtilPlayer.message(caller, F.help("/" + _aliasUsed + " <Player>", "Removes a link to a forum and in-game account.", ChatColor.RED));
		}
		else
		{
			Plugin.unlink(caller, args[0]);
		}
	}
}