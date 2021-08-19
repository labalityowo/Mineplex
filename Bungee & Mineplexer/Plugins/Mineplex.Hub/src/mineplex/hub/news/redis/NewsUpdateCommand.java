package mineplex.hub.news.redis;

import java.util.List;

import mineplex.hub.news.NewsElement;
import mineplex.serverdata.commands.ServerCommand;

public class NewsUpdateCommand extends ServerCommand
{

	private final List<NewsElement> _elements;

	public NewsUpdateCommand(List<NewsElement> elements)
	{
		_elements = elements;
	}

	public List<NewsElement> getElements()
	{
		return _elements;
	}
}
