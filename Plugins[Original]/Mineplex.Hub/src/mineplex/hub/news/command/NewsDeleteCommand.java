package mineplex.hub.news.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.hub.news.NewsElement;
import mineplex.hub.news.NewsManager;
import mineplex.hub.news.NewsManager.Perm;

public class NewsDeleteCommand extends CommandBase<NewsManager>
{

	public NewsDeleteCommand(NewsManager plugin)
	{
		super(plugin, Perm.NEWS_DELETE_COMMAND, "delete");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You must specify an id."));
			return;
		}

		int id;

		try
		{
			id = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e)
		{
			caller.sendMessage(F.main(Plugin.getName(), F.elem(args[0]) + " is not a number."));
			return;
		}

		NewsElement element = Plugin.deleteNews(id);

		if (element == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "There is no news with an id of " + F.elem(id)) + ".");
		}
		else
		{
			caller.sendMessage(F.main(Plugin.getName(), "Deleted news " + element.getValue()) + ".");
		}
	}
}
