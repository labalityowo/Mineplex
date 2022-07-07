package mineplex.core.game.kit;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KitEntityData<T extends LivingEntity>
{

	private final Class<T> _classOfT;
	private final ItemStack _inHand;
	private final ItemStack[] _armour;
	private final int _variant;

	KitEntityData(Class<T> classOfT, ItemStack inHand)
	{
		this(classOfT, inHand, null);
	}

	KitEntityData(Class<T> classOfT, ItemStack inHand, ItemStack[] armour)
	{
		this(classOfT, inHand, armour, 0);
	}

	KitEntityData(Class<T> classOfT, ItemStack inHand, int variant)
	{
		this(classOfT, inHand, null, variant);
	}

	KitEntityData(Class<T> classOfT, ItemStack inHand, ItemStack[] armour, int variant)
	{
		_classOfT = classOfT;
		_inHand = inHand;
		_armour = armour;
		_variant = variant;

		if (_armour != null)
		{
			for (ItemStack itemStack : _armour)
			{
				if (itemStack != null)
				{
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.spigot().setUnbreakable(true);
					itemStack.setItemMeta(itemMeta);
				}
			}
		}
	}

	public Class<T> getClassOfT()
	{
		return _classOfT;
	}

	public ItemStack getInHand()
	{
		return _inHand;
	}

	public ItemStack[] getArmour()
	{
		return _armour;
	}

	public int getVariant()
	{
		return _variant;
	}
}
