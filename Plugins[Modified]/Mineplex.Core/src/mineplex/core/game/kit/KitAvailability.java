package mineplex.core.game.kit;

import org.bukkit.ChatColor;

public enum KitAvailability
{

	Free(ChatColor.YELLOW),
	Gem(ChatColor.GREEN),
	Achievement(ChatColor.LIGHT_PURPLE),
	Hide(Free._colour),
	Null(ChatColor.BLACK)
	;
	
	private final ChatColor _colour;

	KitAvailability(ChatColor colour)
	{
		_colour = colour;
	}

	public ChatColor getColour()
	{
		return _colour;
	}
}
