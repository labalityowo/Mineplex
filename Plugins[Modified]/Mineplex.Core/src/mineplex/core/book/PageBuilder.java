package mineplex.core.book;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class PageBuilder
{
	private final BookBuilder _parent;

	private BaseComponent[] _components = new BaseComponent[0];

	protected PageBuilder(BookBuilder builder)
	{
		this._parent = builder;
	}

	public int getPage()
	{
		return this._parent.getPageNumber(this);
	}

	public PageBuilder component(BaseComponent... components)
	{
		_components = components;
		return this;
	}

	public BookBuilder bookBuilder()
	{
		return this._parent;
	}

	protected String build()
	{
		return ComponentSerializer.toString(_components);
	}
}
