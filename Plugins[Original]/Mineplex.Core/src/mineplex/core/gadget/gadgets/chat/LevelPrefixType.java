package mineplex.core.gadget.gadgets.chat;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum LevelPrefixType
{

	GRAY(ChatColor.GRAY, DyeColor.GRAY, "Gray", 0),
	BLUE(ChatColor.BLUE, DyeColor.BLUE, "Blue", 20),
	DARK_GREEN(ChatColor.DARK_GREEN, DyeColor.GREEN, "Dark Green", 40),
	GOLD(ChatColor.GOLD, DyeColor.ORANGE, "Gold", 60),
	RED(ChatColor.RED, DyeColor.RED, "Red", 80),
	DARK_RED(ChatColor.DARK_RED, DyeColor.BROWN, "Dark Red"),
	YELLOW(ChatColor.YELLOW, DyeColor.YELLOW, "Yellow"),
	GREEN(ChatColor.GREEN, DyeColor.LIME, "Green"),
	AQUA(ChatColor.AQUA, DyeColor.LIGHT_BLUE, "Aqua"),
	CYAN(ChatColor.DARK_AQUA, DyeColor.CYAN, "Cyan"),
	DARK_BLUE(ChatColor.DARK_BLUE, DyeColor.BLUE, "Dark Blue"),
	PURPLE(ChatColor.DARK_PURPLE, DyeColor.PURPLE, "Purple"),
	PINK(ChatColor.LIGHT_PURPLE, DyeColor.PINK, "Pink"),
	WHITE(ChatColor.WHITE, DyeColor.WHITE, "White"),
	BLACK(ChatColor.BLACK, DyeColor.BLACK, "Black")

	;

	private final ChatColor _chatColor;
	private final DyeColor _dyeColor;
	private final String _name;
	private int _unlockAt;

	LevelPrefixType(ChatColor chatColor, DyeColor dyeColor, String name)
	{
		this(chatColor, dyeColor, name, 100);
	}

	LevelPrefixType(ChatColor chatColor, DyeColor dyeColor, String name, int unlockAt)
	{
		_chatColor = chatColor;
		_dyeColor = dyeColor;
		_name = name;
		_unlockAt = unlockAt;
	}

	public ChatColor getChatColor()
	{
		return _chatColor;
	}

	public DyeColor getDyeColor()
	{
		return _dyeColor;
	}

	public String getName()
	{
		return _name;
	}

	public int getUnlockAt()
	{
		return _unlockAt;
	}
}
