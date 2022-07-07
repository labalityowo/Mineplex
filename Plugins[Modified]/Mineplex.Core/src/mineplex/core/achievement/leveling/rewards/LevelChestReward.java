package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.treasure.types.TreasureType;

public class LevelChestReward implements ScalableLevelReward
{

	private final TreasureType _chest;
	private final int _amount;

	public LevelChestReward(TreasureType chest, int amount)
	{
		_chest = chest;
		_amount = amount;
	}

	@Override
	public void claim(Player player)
	{
		INVENTORY.addItemToInventory(null, player, _chest.getItemName(), _amount);
	}

	@Override
	public String getDescription()
	{
		return _amount + " " + _chest.getName();
	}

	@Override
	public ScalableLevelReward cloneScalable(double scale)
	{
		return new LevelChestReward(_chest, (int) (scale * _amount));
	}
}
