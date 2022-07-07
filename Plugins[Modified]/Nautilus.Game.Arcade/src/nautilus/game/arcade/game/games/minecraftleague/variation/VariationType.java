package nautilus.game.arcade.game.games.minecraftleague.variation;

import nautilus.game.arcade.game.games.minecraftleague.variation.wither.WitherVariation;

import org.bukkit.ChatColor;

public enum VariationType
{
	STANDARD("STANDARD", StandardGameplay.class, "Standard Gameplay", ChatColor.GREEN),
	WITHER("WITHER", WitherVariation.class, "Wither Variation", ChatColor.RED),
	//GUARDIAN(3, GuardianVariation.class, "Guardian Variation", ChatColor.DARK_AQUA)
	;
	
	private String _id;
	private String _name;
	private ChatColor _color;
	private Class<? extends GameVariation> _variation;
	
	private VariationType(String id, Class<? extends GameVariation> variationClass, String displayName, ChatColor displayColor)
	{
		_id = id;
		_name = displayName;
		_color = displayColor;
		_variation = variationClass;
	}
	
	public Class<? extends GameVariation> getVariation()
	{
		return _variation;
	}
	
	public String getDisplayMessage()
	{
		return ChatColor.DARK_AQUA + "Game Type Selected: " + _color + _name;
	}
	
	public static VariationType getFromID(String id)
	{
		for (VariationType type : VariationType.values())
		{
			if (type._id.equalsIgnoreCase(id))
				return type;
		}
		
		return VariationType.STANDARD;
	}
}
