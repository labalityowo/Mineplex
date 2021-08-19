package mineplex.core.chat;

public enum ChatChannel
{

	PUBLIC(null, true),
	PARTY("@", false),
	COMMUNITY("!", false),
	TEAM("#", true);

	private final String _prefix;
	private final boolean _moderated;

	ChatChannel(String prefix, boolean moderated)
	{
		_prefix = prefix;
		_moderated = moderated;
	}

	public String getPrefix()
	{
		return _prefix;
	}

	public boolean isModerated()
	{
		return _moderated;
	}
}
