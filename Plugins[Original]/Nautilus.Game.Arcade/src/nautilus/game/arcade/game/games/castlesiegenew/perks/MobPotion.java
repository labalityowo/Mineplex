package nautilus.game.arcade.game.games.castlesiegenew.perks;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class MobPotion
{

	private final ItemStack _itemStack;
	private final EntityType _entityType;
	private final int _amount;

	public MobPotion(ItemStack itemStack, EntityType entityType, int amount)
	{
		_itemStack = itemStack;
		_entityType = entityType;
		_amount = amount;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public EntityType getEntityType()
	{
		return _entityType;
	}

	public int getAmount()
	{
		return _amount;
	}
}
