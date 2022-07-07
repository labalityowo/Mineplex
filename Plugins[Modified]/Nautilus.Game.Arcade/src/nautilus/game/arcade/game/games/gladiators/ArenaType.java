package nautilus.game.arcade.game.games.gladiators;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by William (WilliamTiger).
 * 07/12/15
 */
public enum ArenaType
{
	RED(1, new Loadout()
	{
		@Override
		public ItemStack getSword()
		{
			return new ItemStack(Material.DIAMOND_SWORD, 1);
		}

		@Override
		public ItemStack getRod()
		{
			return new ItemStack(Material.FISHING_ROD, 1);
		}

		@Override
		public ItemStack getBow()
		{
			return new ItemStack(Material.BOW, 1);
		}

		@Override
		public ItemStack getArrows()
		{
			return new ItemStack(Material.ARROW, 10);
		}

		@Override
		public ItemStack getHelmet()
		{
			return new ItemStack(Material.IRON_HELMET, 1);
		}

		@Override
		public ItemStack getChestplate()
		{
			return new ItemStack(Material.IRON_CHESTPLATE, 1);
		}

		@Override
		public ItemStack getLeggings()
		{
			return new ItemStack(Material.IRON_LEGGINGS, 1);
		}

		@Override
		public ItemStack getBoots()
		{
			return new ItemStack(Material.IRON_BOOTS, 1);
		}
	}),
	ORANGE(2, new Loadout()
	{
		@Override
		public ItemStack getSword()
		{
			return new ItemStack(Material.IRON_SWORD, 1);
		}

		@Override
		public ItemStack getRod()
		{
			return new ItemStack(Material.FISHING_ROD, 1);
		}

		@Override
		public ItemStack getBow()
		{
			return new ItemStack(Material.BOW, 1);
		}

		@Override
		public ItemStack getArrows()
		{
			return new ItemStack(Material.ARROW, 7);
		}

		@Override
		public ItemStack getHelmet()
		{
			return new ItemStack(Material.CHAINMAIL_HELMET);
		}

		@Override
		public ItemStack getChestplate()
		{
			return new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
		}

		@Override
		public ItemStack getLeggings()
		{
			return new ItemStack(Material.CHAINMAIL_LEGGINGS, 1);
		}

		@Override
		public ItemStack getBoots()
		{
			return new ItemStack(Material.CHAINMAIL_BOOTS, 1);
		}
	}),
	YELLOW(4, new Loadout()
	{
		@Override
		public ItemStack getSword()
		{
			return new ItemStack(Material.STONE_SWORD, 1);
		}

		@Override
		public ItemStack getRod()
		{
			return new ItemStack(Material.FISHING_ROD, 1);
		}

		@Override
		public ItemStack getBow()
		{
			return new ItemStack(Material.BOW, 1);
		}

		@Override
		public ItemStack getArrows()
		{
			return new ItemStack(Material.ARROW, 5);
		}

		@Override
		public ItemStack getHelmet()
		{
			return new ItemStack(Material.GOLD_HELMET, 1);
		}

		@Override
		public ItemStack getChestplate()
		{
			return new ItemStack(Material.GOLD_CHESTPLATE, 1);
		}

		@Override
		public ItemStack getLeggings()
		{
			return new ItemStack(Material.GOLD_LEGGINGS, 1);
		}

		@Override
		public ItemStack getBoots()
		{
			return new ItemStack(Material.GOLD_BOOTS, 1);
		}
	}),
	GREEN(8, new Loadout()
	{
		@Override
		public ItemStack getSword()
		{
			return new ItemStack(Material.WOOD_SWORD, 1);
		}

		@Override
		public ItemStack getRod()
		{
			return new ItemStack(Material.FISHING_ROD, 1);
		}

		@Override
		public ItemStack getBow()
		{
			return new ItemStack(Material.BOW, 1);
		}

		@Override
		public ItemStack getArrows()
		{
			return new ItemStack(Material.ARROW, 3);
		}

		@Override
		public ItemStack getHelmet()
		{
			return new ItemStack(Material.LEATHER_HELMET, 1);
		}

		@Override
		public ItemStack getChestplate()
		{
			return new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		}

		@Override
		public ItemStack getLeggings()
		{
			return new ItemStack(Material.LEATHER_LEGGINGS, 1);
		}

		@Override
		public ItemStack getBoots()
		{
			return new ItemStack(Material.LEATHER_BOOTS, 1);
		}
	});

	private int _endsAt;
	private Loadout _loadout;

	ArenaType(int endsAt, Loadout loadout)
	{
		_endsAt = endsAt;
		_loadout = loadout;
	}

	public Loadout getLoadout()
	{
		return _loadout;
	}

	public int getEndsAt()
	{
		return _endsAt;
	}

	public String getName()
	{
		return toString().toLowerCase();
	}

	public boolean furtherOut(ArenaType other)
	{
		return !(compareTo(other) <= 0);
	}

}