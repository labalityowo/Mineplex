package mineplex.hub.news.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.hub.news.NewsManager;
import mineplex.hub.news.NewsManager.Perm;

public class NewsAddCommand extends CommandBase<NewsManager>
{

	public NewsAddCommand(NewsManager plugin)
	{
		super(plugin, Perm.NEWS_ADD_COMMAND, "add");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You must specify some news."));
			return;
		}

		StringBuilder builder = new StringBuilder();

		for (String arg : args)
		{
			builder.append(arg).append(" ");
		}

		String newsValue = builder.toString().trim();

		if (newsValue.length() > 64)
		{
			caller.sendMessage(F.main(Plugin.getName(), "The maximum length of news is 64 characters!"));
			return;
		}

		Plugin.addNews(newsValue);
		caller.sendMessage(F.main(Plugin.getName(), "Added news: " + ChatColor.translateAlternateColorCodes('&', newsValue)));
	}
}
