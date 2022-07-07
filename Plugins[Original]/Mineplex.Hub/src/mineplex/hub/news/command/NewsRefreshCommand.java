package mineplex.hub.news.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.hub.news.NewsElement;
import mineplex.hub.news.NewsManager;
import mineplex.hub.news.NewsManager.Perm;

public class NewsRefreshCommand extends CommandBase<NewsManager>
{

	public NewsRefreshCommand(NewsManager plugin)
	{
		super(plugin, Perm.NEWS_REFRESH_COMMAND, "refresh");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Plugin.refreshNews();
		caller.sendMessage(F.main(Plugin.getName(), "Refreshed the news."));
	}
}
