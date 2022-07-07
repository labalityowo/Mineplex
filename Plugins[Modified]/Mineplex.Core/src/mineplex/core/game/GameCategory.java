package mineplex.core.game;

import org.bukkit.ChatColor;

public enum GameCategory
{

	CASUAL("Casual", ChatColor.AQUA),
	INTERMEDIATE("Intermediate", ChatColor.GREEN),
	HARDCORE("Hardcore", ChatColor.RED),
	EVENT("Event", ChatColor.LIGHT_PURPLE),
	NONE("None", ChatColor.GRAY);

	private final String _name;
	private final ChatColor _chatColor;

	GameCategory(String name, ChatColor chatColor)
	{
		_name = name;
		_chatColor = chatColor;
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getChatColor()
	{
		return _chatColor;
	}
}
