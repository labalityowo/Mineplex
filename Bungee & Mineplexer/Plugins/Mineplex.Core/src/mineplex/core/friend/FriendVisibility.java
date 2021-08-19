package mineplex.core.friend;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mineplex.core.itemstack.ItemBuilder;

public enum FriendVisibility
{

	SHOWN("Visible", ChatColor.GREEN, Material.EMERALD),
	PRESENCE("Semi-Visible", ChatColor.YELLOW, Material.GLOWSTONE_DUST),
	INVISIBLE("Invisible", ChatColor.RED, Material.REDSTONE);

	private final String _name;
	private final ChatColor _colour;
	private final ItemStack _itemStack;

	FriendVisibility(String name, ChatColor colour, Material material)
	{
		_name = name;
		_colour = colour;
		_itemStack = new ItemBuilder(material)
				.setTitle(colour + name)
				.addLore("Click to set your status", "to " + name + "!")
				.build();
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getColour()
	{
		return _colour;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}
}
