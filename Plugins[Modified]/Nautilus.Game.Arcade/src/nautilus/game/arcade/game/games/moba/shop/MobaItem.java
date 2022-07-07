package nautilus.game.arcade.game.games.moba.shop;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.leaderboard.Leaderboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobaItem
{

	private final ItemStack _item;
	private final int _cost;
	private List<MobaItemEffect> _effects;

	public MobaItem(ItemStack item, int cost)
	{
		_item = item;
		_cost = cost;
	}

	public MobaItem addEffects(MobaItemEffect... effects)
	{
		if (_effects == null)
		{
			_effects = new ArrayList<>(effects.length);
		}

		Collections.addAll(_effects, effects);
		return this;
	}

	public ItemStack getItem()
	{
		ItemBuilder builder = new ItemBuilder(_item).setUnbreakable(true);

		if (getEffects() != null)
		{
			builder.addLore("", C.cWhite + "Effects:");

			for (MobaItemEffect effect : getEffects())
			{
				builder.addLore(" - " + effect.getDescription());
			}

			builder.addLore("");
		}

		return builder.build();
	}

	public int getCost()
	{
		return _cost;
	}

	public List<MobaItemEffect> getEffects()
	{
		return _effects;
	}

}
