package mineplex.gemhunters.loot.rewards;

import mineplex.gemhunters.util.SlackRewardBot;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.util.Callback;
import mineplex.core.inventory.InventoryManager;

public class LootChestReward extends LootItemReward
{

	private final InventoryManager _inventory;

	private final String _chestName;
	private final int _amount;

	public LootChestReward(long cashOutDelay, ItemStack itemStack, String chestName, int amount)
	{
		super(chestName + " Chest", cashOutDelay, itemStack);

		_inventory = Managers.require(InventoryManager.class);
		_chestName = chestName;
		_amount = amount;
	}

	@Override
	public void onCollectItem()
	{

	}

	@Override
	public void onSuccessful()
	{
		_inventory.addItemToInventory(success -> SlackRewardBot.logReward(_player, this, success ? "Success" : "Failure"), _player, _chestName + " Chest", _amount);
	}

	@Override
	public void onDeath()
	{

	}

}
