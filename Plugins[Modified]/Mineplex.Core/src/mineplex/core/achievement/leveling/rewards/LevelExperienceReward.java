package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.achievement.Achievement;
import mineplex.core.common.util.C;
import mineplex.core.stats.StatsManager;

public class LevelExperienceReward implements ScalableLevelReward
{

	private static final StatsManager STATS_MANAGER = Managers.require(StatsManager.class);

	private final int _amount;

	public LevelExperienceReward(int amount)
	{
		_amount = amount;
	}

	@Override
	public void claim(Player player)
	{
		STATS_MANAGER.incrementStat(player, Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0], _amount);
	}

	@Override
	public String getDescription()
	{
		return C.cYellow + _amount + " XP";
	}

	@Override
	public ScalableLevelReward cloneScalable(double scale)
	{
		return new LevelExperienceReward((int) (scale * _amount));
	}
}
