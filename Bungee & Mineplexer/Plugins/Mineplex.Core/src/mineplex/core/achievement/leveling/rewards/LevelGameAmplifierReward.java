package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.boosters.BoosterManager;
import mineplex.core.common.util.C;

public class LevelGameAmplifierReward implements ScalableLevelReward
{

	private final int _amount;

	public LevelGameAmplifierReward(int amount)
	{
		_amount = amount;
	}

	@Override
	public void claim(Player player)
	{
		INVENTORY.addItemToInventory(player, BoosterManager.BOOSTER_ITEM, _amount);
	}

	@Override
	public String getDescription()
	{
		return C.cGreen + _amount +  " Game Amplifer" + (_amount != 1 ? "s" : "");
	}

	@Override
	public ScalableLevelReward cloneScalable(double scale)
	{
		return new LevelGameAmplifierReward((int) (scale * _amount));
	}
}
