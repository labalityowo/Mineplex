package nautilus.game.arcade.game.games.moba;

import mineplex.core.common.skin.SkinData;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum MobaRole
{

	ASSASSIN("Assassin", Color.GRAY, ChatColor.GRAY, SkinData.HATTORI),
	HUNTER("Hunter", Color.LIME, ChatColor.GREEN, SkinData.DEVON),
	MAGE("Mage", Color.PURPLE, ChatColor.DARK_PURPLE, SkinData.ANATH),
	WARRIOR("Warrior", Color.YELLOW, ChatColor.GOLD, SkinData.DANA),
	;

	private final String _name;
	private final Color _color;
	private final ChatColor _chatColor;
	private final SkinData _skinData;

	MobaRole(String name, Color color, ChatColor chatColor, SkinData skinData)
	{
		_name = name;
		_color = color;
		_chatColor = chatColor;
		_skinData = skinData;
	}

	public String getName()
	{
		return _name;
	}

	public Color getColor()
	{
		return _color;
	}

	public ChatColor getChatColor()
	{
		return _chatColor;
	}

	public SkinData getSkin()
	{
		return _skinData;
	}
}
