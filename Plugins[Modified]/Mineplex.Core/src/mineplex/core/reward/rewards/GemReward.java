package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.UtilMath;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class GemReward extends Reward
{

	private final int _minGemCount;
	private final int _maxGemCount;

	public GemReward(int gems)
	{
		this(gems, gems, 0, RewardRarity.LEGENDARY);
	}

	public GemReward(int minGemCount, int maxGemCount, int shardValue, RewardRarity rarity)
	{
		super(rarity, shardValue);

		_minGemCount = minGemCount;
		_maxGemCount = maxGemCount;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		int gems;

		if (_minGemCount == _maxGemCount)
		{
			gems = _minGemCount;
		}
		else
		{
			gems = UtilMath.rRange(_minGemCount, _maxGemCount);
		}
		DONATION_MANAGER.rewardCurrency(GlobalCurrency.GEM, player, "Treasure Chest", gems);

		return new RewardData(null, getRarity().getColor() + gems + " Gems", new ItemStack(Material.EMERALD), getRarity());
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(null, getRarity().getColor() + "Gems", new ItemStack(Material.EMERALD), getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof GemReward;
	}
}
