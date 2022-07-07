package mineplex.core.common.jsonchat;

public enum ClickEvent
{
	RUN_COMMAND("run_command"),
	SUGGEST_COMMAND("suggest_command"),
	OPEN_URL("open_url"),
	CHANGE_PAGE("change_page"); // Change Page only applies to books, which we haven't been able to use yet

	private String _minecraftString;

	ClickEvent(String minecraftString)
	{
		_minecraftString = minecraftString;
	}

	@Override
	public String toString()
	{
		return _minecraftString;
	}
}
