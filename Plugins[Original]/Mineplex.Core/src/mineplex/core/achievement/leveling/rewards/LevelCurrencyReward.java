package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;

public class LevelCurrencyReward implements ScalableLevelReward
{

	private final GlobalCurrency _type;
	private final int _amount;

	public LevelCurrencyReward(GlobalCurrency type, int amount)
	{
		_type = type;
		_amount = amount;
	}

	@Override
	public void claim(Player player)
	{
		DONATION.rewardCurrencyUntilSuccess(_type, player, "Level Reward", _amount);
	}

	@Override
	public String getDescription()
	{
		return F.currency(_type, _amount);
	}

	@Override
	public ScalableLevelReward cloneScalable(double scale)
	{
		return new LevelCurrencyReward(_type, (int) (scale * _amount));
	}

	public GlobalCurrency getType()
	{
		return _type;
	}
}
