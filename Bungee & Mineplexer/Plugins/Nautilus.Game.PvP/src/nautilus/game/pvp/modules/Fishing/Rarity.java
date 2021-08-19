package nautilus.game.pvp.modules.Fishing;

import org.bukkit.ChatColor;

public enum Rarity
{
	Common(ChatColor.YELLOW),
	Moderate(ChatColor.GOLD),
	Rare(ChatColor.RED),
	Legendary(ChatColor.LIGHT_PURPLE);

	private ChatColor c;

	private Rarity(ChatColor c) 
	{
		this.c = c;
	}

	public ChatColor GetColor()
	{
		return c;
	}
}
