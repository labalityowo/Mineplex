package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.achievement.Achievement;
import mineplex.core.common.util.UtilMath;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.stats.StatsManager;
import mineplex.core.treasure.reward.RewardRarity;

public class ExperienceReward extends Reward
{

	private static final StatsManager STATS_MANAGER = Managers.require(StatsManager.class);
	private static final ItemStack ITEM_STACK = new ItemStack(Material.EXP_BOTTLE);

	private final int _minExperience;
	private final int _maxExperience;

	public ExperienceReward(int experience)
	{
		this(experience, experience, 0, RewardRarity.RARE);
	}

	public ExperienceReward(int minExperience, int maxExperience, int shardValue, RewardRarity rarity)
	{
		super(rarity, shardValue);

		_minExperience = minExperience;
		_maxExperience = maxExperience;
	}

	@Override
	protected RewardData giveRewardCustom(Player player)
	{
		int experience;

		if (_minExperience == _maxExperience)
		{
			experience = _minExperience;
		}
		else
		{
			experience = UtilMath.rRange(_minExperience, _maxExperience);
		}

		STATS_MANAGER.incrementStat(player, Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0], experience);

		return new RewardData(null, getRarity().getColor() + experience + " Experience", ITEM_STACK, getRarity());
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(null, getRarity().getColor() + "Experience", ITEM_STACK, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}
}
