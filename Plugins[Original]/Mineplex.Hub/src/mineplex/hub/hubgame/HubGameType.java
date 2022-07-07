package mineplex.hub.hubgame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum HubGameType
{

	TRON("Slime Cycles", new String[]
			{
					"Control your Slime by looking in",
					"the direction you want to go!",
					"Avoid other trails and walls.",
					"Last player standing wins!",
			}, new ItemStack(Material.SLIME_BALL), 2, 4),
	DUELS("Gladiators", new String[]
			{
					"A 1v1 duel against another player.",
					"You do not regenerate health.",
					"You can place blocks!",
					"Kill your opponent to win!"
			}, new ItemStack(Material.DIAMOND_SWORD), 2, 2),
	;

	private final String _name;
	private final String[] _description;
	private final ItemStack _itemStack;
	private final int _minPlayers;
	private final int _maxPlayers;

	HubGameType(String name, String[] description, ItemStack itemStack, int minPlayers, int maxPlayers)
	{
		_name = name;
		_description = description;
		_itemStack = itemStack;
		_minPlayers = minPlayers;
		_maxPlayers = maxPlayers;
	}

	public String getName()
	{
		return _name;
	}

	public String[] getDescription()
	{
		return _description;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public int getMinPlayers()
	{
		return _minPlayers;
	}

	public int getMaxPlayers()
	{
		return _maxPlayers;
	}
}
