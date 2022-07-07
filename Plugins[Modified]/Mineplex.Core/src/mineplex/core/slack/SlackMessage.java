package mineplex.core.slack;

import java.net.URL;

import com.google.gson.JsonObject;

/**
 * A message to be sent through the {@link SlackAPI}.
 */
public class SlackMessage
{
	private String _username;
	private String _icon;
	private URL _iconURL;

	private String _content;

	/**
	 * Class constructor.
	 *
	 * @param content The content of the message.
	 */
	public SlackMessage(String content)
	{
		_icon = SlackAPI.DEFAULT_ICON;
		_content = content;
	}

	/**
	 * Class constructor.
	 *
	 * @param username The username of the message.
	 * @param content The content of the message.
	 */
	public SlackMessage(String username, String content)
	{
		_username = username;
		_icon = SlackAPI.DEFAULT_ICON;
		_content = content;
	}

	/**
	 * Class constructor.
	 *
	 * @param username The username of the message.
	 * @param icon The icon/emoji of the message.
	 * @param content The content of the message.
	 */
	public SlackMessage(String username, String icon, String content)
	{
		_username = username;
		_icon = ":" + icon + ":";
		_content = content;
	}

	/**
	 * Class constructor.
	 *
	 * @param username The username of the message.
	 * @param iconURL The icon url of the message.
	 * @param content The content of the message.
	 */
	public SlackMessage(String username, URL iconURL, String content)
	{
		_username = username;
		_iconURL = iconURL;
		_content = content;
	}

	/**
	 * Converts the message to JSON format.
	 *
	 * @return The {@link SlackMessage} in the form of a {@link JsonObject}.
	 */
	public JsonObject toJson()
	{
		JsonObject msg = new JsonObject();

		if (_username != null)
		{
			msg.addProperty("username", _username);
		}

		if (_iconURL != null)
		{
			msg.addProperty("icon_url", _iconURL.toString());
		}
		else if (_icon != null)
		{
			msg.addProperty("icon_emoji", _icon);
		}

		if (_content != null)
		{
			msg.addProperty("text", _content);
		}

		return msg;
	}

	/**
	 * Gets the username that displays as a title.
	 *
	 * @return The username in use.
	 */
	public String getUsername()
	{
		return _username;
	}

	/**
	 * Sets the username that displays as a title.
	 *
	 * @param username The username to use.
	 */
	public void setUsername(String username)
	{
		_username = username;
	}

	/**
	 * Gets the icon that displays with the title.
	 *
	 * @return The icon in use.
	 */
	public String getIcon()
	{
		return _icon;
	}

	/**
	 * Sets the icon that displays with the title.
	 *
	 * @param icon The icon to use.
	 */
	public void setIcon(String icon)
	{
		_icon = icon;
	}

	/**
	 * Gets the content of the message.
	 *
	 * @return The content of the message.
	 */
	public String getContent()
	{
		return _content;
	}

	/**
	 * Sets the content of the message.
	 *
	 * @param content The content of the message.
	 */
	public void setContent(String content)
	{
		_content = content;
	}
}