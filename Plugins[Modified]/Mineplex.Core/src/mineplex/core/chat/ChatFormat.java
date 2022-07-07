package mineplex.core.chat;

/**
 * The format for a chat message being sent by a player
 *
 * @author Shaun Bennett
 */
public class ChatFormat
{
	private String _format;
	private boolean _json;

	public ChatFormat(String format, boolean json)
	{
		_format = format;
		_json = json;
	}

	/**
	 * Get the string representing the chat format. This will be represented as a JSON string if {@link #isJson()},
	 * or should be in the standard Spigot chat format otherwise.
	 *
	 * @return A string representing how to format the chat message
	 */
	public String getFormat()
	{
		return _format;
	}

	/**
	 * Is this chat format a JSON string (should be sent to player as a json message)
	 *
	 * @return boolean representing if this chat format is in the json format
	 */
	public boolean isJson()
	{
		return _json;
	}
}
