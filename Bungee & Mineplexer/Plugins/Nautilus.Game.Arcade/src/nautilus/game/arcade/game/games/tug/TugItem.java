package nautilus.game.arcade.game.games.tug;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.itemstack.ItemBuilder;

public class TugItem
{

	private final EntityType _entityType;
	private final ItemStack _itemStack;
	private final int _cost;

	TugItem(EntityType entityType, int cost)
	{
		_entityType = entityType;
		_itemStack = new ItemBuilder(Material.MONSTER_EGG)
				.setData(UtilEnt.getEntityEggData(entityType))
				.setTitle(C.cYellow + "Spawn a " + UtilEnt.getName(entityType) + C.mBody + " - " + C.cGold + cost + " Gold")
				.build();
		_cost = cost;
	}

	public EntityType getEntityType()
	{
		return _entityType;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public int getCost()
	{
		return _cost;
	}
}
