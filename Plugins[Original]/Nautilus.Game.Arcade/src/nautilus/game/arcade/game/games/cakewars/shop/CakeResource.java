package nautilus.game.arcade.game.games.cakewars.shop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.updater.UpdateType;

public enum CakeResource
{

	BRICK("Brick", ChatColor.RED, new ItemStack(Material.CLAY_BRICK), UpdateType.TWOSEC, 64),
	EMERALD("Emerald", ChatColor.GREEN, new ItemStack(Material.EMERALD), UpdateType.SEC_05, 24),
	STAR("Nether Star", ChatColor.GOLD, new ItemStack(Material.NETHER_STAR), UpdateType.SEC_08, 8);

	private final String _name;
	private final ChatColor _chatColor;
	private final ItemStack _itemStack;
	private final UpdateType _spawnerUpdate;
	private final int _maxSpawned;

	CakeResource(String name, ChatColor chatColor, ItemStack itemStack, UpdateType spawnerUpdate, int maxSpawned)
	{
		_name = name;
		_chatColor = chatColor;
		_itemStack = itemStack;
		_spawnerUpdate = spawnerUpdate;
		_maxSpawned = maxSpawned;
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getChatColor()
	{
		return _chatColor;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public UpdateType getSpawnerUpdate()
	{
		return _spawnerUpdate;
	}

	public int getMaxSpawned()
	{
		return _maxSpawned;
	}
}
