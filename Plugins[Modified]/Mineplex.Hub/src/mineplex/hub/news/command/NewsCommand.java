package mineplex.hub.news.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.hub.news.NewsManager;
import mineplex.hub.news.NewsManager.Perm;

public class NewsCommand extends MultiCommandBase<NewsManager>
{

	public NewsCommand(NewsManager plugin)
	{
		super(plugin, Perm.NEWS_COMMAND, "news");

		AddCommand(new NewsAddCommand(plugin));
		AddCommand(new NewsDeleteCommand(plugin));
		AddCommand(new NewsListCommand(plugin));
		AddCommand(new NewsRefreshCommand(plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "Commands List:"));
		caller.sendMessage(F.help("/" + _aliasUsed + " add <news>", "Adds a new news entry.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " delete <id>", "Deletes a news entry based on id.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " list", "Lists all new news entries.", ChatColor.DARK_RED));
		caller.sendMessage(F.help("/" + _aliasUsed + " refresh", "Refreshes all news entries on every server.", ChatColor.DARK_RED));

		if (!Plugin.isEnabled())
		{
			caller.sendMessage(F.main(Plugin.getName(), "News has been disabled by the plugin. Thus any news will not be displayed."));
		}
	}
}
