package mineplex.hub.news.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.Color;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.hub.news.NewsElement;
import mineplex.hub.news.NewsManager;
import mineplex.hub.news.NewsManager.Perm;

public class NewsListCommand extends CommandBase<NewsManager>
{

	public NewsListCommand(NewsManager plugin)
	{
		super(plugin, Perm.NEWS_LIST_COMMAND, "list");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller.sendMessage(F.main(Plugin.getName(), "News List:"));

		for (NewsElement element : Plugin.getElements())
		{
			new JsonMessage("[DELETE]")
					.bold()
					.color(Color.RED)
					.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to delete this news entry.")
					.click(ClickEvent.RUN_COMMAND, "/news delete " + element.getId())
					.extra(C.Reset + " " + element.getValue())
					.sendToPlayer(caller);
		}
	}
}
