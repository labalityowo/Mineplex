package nautilus.game.arcade.managers.voting.types;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import nautilus.game.arcade.managers.voting.Voteable;

public class VotableMap implements Voteable
{

	private final String _name;
	private final ItemStack _itemStack;

	public VotableMap(String name)
	{
		_name = name;
		_itemStack = new ItemStack(Material.PAPER);
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public String getDisplayName()
	{
		String[] split = getName().split("_");

		if (split.length == 1)
		{
			return getName();
		}

		return split[1];
	}

	@Override
	public ItemStack getItemStack()
	{
		return _itemStack;
	}
}
