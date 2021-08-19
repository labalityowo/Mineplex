package mineplex.core.treasure.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.weight.WeightSet;
import mineplex.core.reward.Reward;
import mineplex.core.reward.rewards.RankReward;
import mineplex.core.reward.rewards.TreasureShardReward;
import mineplex.core.treasure.types.Treasure;

/**
 * This manager serves as a random reward generator.
 * A lot of this was adapted from the previous reward manager, so if you've come here to
 * fix something then prepare yourself for a shock.
 */
@ReflectivelyCreateMiniPlugin
public class TreasureRewardManager extends MiniPlugin
{

	/**
	 * For all the reward algorithms, if a condition cannot be met within {@value} iterations then it gives up.
	 * {@value} is a very generous threshold and most likely, unless due to error, each one should complete in one
	 * or two iterations.
	 */
	private static final int MAX_ATTEMPTS = 40;

	private final Map<Treasure, Map<RewardRarity, WeightSet<Reward>>> _rewardPoolMap;

	private TreasureRewardManager()
	{
		super("Treasure Rewards");

		_rewardPoolMap = new HashMap<>();
	}

	/**
	 * Adds a reward into the pool for a particular chest type.
	 *
	 * @param treasure The {@link Treasure}'s pool you want to add the reward to.
	 * @param reward   The {@link Reward} you want to add to the pool.
	 * @param weight   The weight of the reward, bigger weight, more probable to be picked.
	 */
	public void addReward(Treasure treasure, Reward reward, int weight)
	{
		// Populate the map if absent
		_rewardPoolMap.computeIfAbsent(treasure, k -> new HashMap<>());

		// Populate the inner map if absent
		Map<RewardRarity, WeightSet<Reward>> map = _rewardPoolMap.get(treasure);
		map.computeIfAbsent(reward.getRarity(), k -> new WeightSet<>());
		// Add the reward to the WeightSet
		map.get(reward.getRarity()).add(weight, reward);
	}

	/**
	 * Returns whether or not a player has all the items within a chest's pool
	 *
	 * @param player   The player you want to checl.
	 * @param treasure The {@link Treasure} you want to search through the pool of.
	 * @return a boolean value of if the player has all the items within a chest's pool.
	 */
	public boolean hasAllItems(Player player, Treasure treasure)
	{
		Map<RewardRarity, WeightSet<Reward>> rewardMap = _rewardPoolMap.get(treasure);

		// Null entry, programmer error
		return rewardMap == null || getOwnedItems(player, treasure) == getTotalItems(treasure);
	}

	/**
	 * Returns the number of items a player owns within a chest's pool
	 *
	 * @param player   The player you want to check.
	 * @param treasure The {@link Treasure} you want to search through the pool of.
	 * @return an integer value of the amount of items the player has within a chest's pool.
	 */
	public int getOwnedItems(Player player, Treasure treasure)
	{
		Map<RewardRarity, WeightSet<Reward>> rewardMap = _rewardPoolMap.get(treasure);

		// Null entry, programmer error
		if (rewardMap == null)
		{
			return 0;
		}

		int owned = 0;

		// Check every reward
		for (WeightSet<Reward> weightSet : rewardMap.values())
		{
			for (Reward reward : weightSet.elements())
			{
				// If the player cannot be given that reward
				if (!reward.canGiveReward(player))
				{
					owned++;
				}
			}
		}

		return owned;
	}

	/**
	 * Returns the total number of items within a chest's pool.
	 *
	 * @param treasure The {@link Treasure} you want to search through the pool of.
	 * @return an integer value of the amount of items the chest has within it's pool.
	 */
	public int getTotalItems(Treasure treasure)
	{
		Map<RewardRarity, WeightSet<Reward>> rewardMap = _rewardPoolMap.get(treasure);

		// Null entry, programmer error
		if (rewardMap == null)
		{
			return 0;
		}

		int amount = 0;

		// Check every reward
		for (WeightSet<Reward> weightSet : rewardMap.values())
		{
			amount += weightSet.elements().size();
		}

		return amount;
	}

	/**
	 * Returns a list of random rewards that can be awarded to a player.
	 *
	 * @param player   The player you want to give the rewards to.
	 * @param treasure The {@link Treasure} you want to search through the pool of.
	 * @return A {@link List} the size of {@link Treasure#getRewardsPerChest()} and with the contents of random rewards
	 * that can be given to the player or null if an error has occurred.
	 */
	public List<Reward> getRewards(Player player, Treasure treasure)
	{
		Map<RewardRarity, WeightSet<Reward>> rewardMap = _rewardPoolMap.get(treasure);

		// Null entry, programmer error
		if (rewardMap == null)
		{
			return null;
		}

		// The list of rewards that will be returned
		List<Reward> rewards = new ArrayList<>(treasure.getRewardsPerChest());
		// These variables here are used to ensure that each chest opening will always contain at least one uncommon
		// but never more than one mythical.
		boolean hasUncommon = false;
		boolean hasMythical = false;
		int attempts = 0;

		for (int i = 0; i < treasure.getRewardsPerChest(); i++)
		{
			attempts++;
			Reward reward = nextReward(player, treasure, rewardMap, i, hasUncommon, hasMythical);

			// If the reward was null then the reward could not have been given to the player
			if (reward == null)
			{
				// At this point if it has taken 20+ attempts to find a reward and it's still null, then there seems to
				// be a problem with the chest's pool or a programmer error.
				if (attempts > MAX_ATTEMPTS)
				{
					return null;
				}
				// We run the loop again
				i--;
				continue;
			}

			RewardRarity rarity = reward.getRarity();

			// If the reward is as or more uncommon than an uncommon
			if (rarity.ordinal() >= RewardRarity.UNCOMMON.ordinal())
			{
				hasUncommon = true;
			}

			// If the reward is as more uncommon than an mythical
			if (rarity.ordinal() >= RewardRarity.MYTHICAL.ordinal())
			{
				hasMythical = true;
			}

			// Add the reward to our list of rewards
			rewards.add(reward);
		}

		// Due to the last reward always being an uncommon or better, we swap it randomly with another element as to
		// appear more random.
		Collections.swap(rewards, rewards.size() - 1, UtilMath.r(rewards.size()));

		return rewards;
	}

	/**
	 * This determines the rarity of the reward.
	 *
	 * @param player      The player you want to give the reward to.
	 * @param treasure    The {@link Treasure} you want to search through the pool of.
	 * @param rewardMap   The map of rarities and weights.
	 * @param index       The current index of this reward, for example the first chest to open will have an index of 0, second 1 etc...
	 * @param hasUncommon Does the current list of rewards contain an uncommon or better?
	 * @param hasMythical Does the current list of rewards contain a mythical or better?
	 * @return A random reward that can be given to the player or null if not or an error has occurred.
	 */
	private Reward nextReward(Player player, Treasure treasure, Map<RewardRarity, WeightSet<Reward>> rewardMap, int index, boolean hasUncommon, boolean hasMythical)
	{
		RewardRarity rarity = treasure.getRewardType().generateRarity();
		int attempts = 0;

		// If the reward list already contains a mythical, keep trying until it isn't one
		while (attempts++ < MAX_ATTEMPTS && hasMythical && rarity == RewardRarity.MYTHICAL)
		{
			rarity = treasure.getRewardType().generateRarity();
		}

		// If we are on the last reward and there hasn't been an uncommon then make sure this reward is uncommon
		// Additional duplicates check is needed as chests without duplicates can only contain rare or above.
		if (!hasUncommon && treasure.isDuplicates() && treasure.getRewardsPerChest() > 1 && index == treasure.getRewardsPerChest() - 1)
		{
			rarity = RewardRarity.UNCOMMON;
		}

		WeightSet<Reward> rewardSet = rewardMap.get(rarity);

		// Null entry, programmer error
		if (rewardSet == null || rewardSet.elements().isEmpty())
		{
			return null;
		}

		// If the chest doesn't give duplicates
		if (!treasure.isDuplicates())
		{
			// Clone the set as we modify it below
			rewardSet = new WeightSet<>(rewardSet);
			// Remove all elements if they cannot be given to a player
			rewardSet.removeIf(rewardWeight -> !rewardWeight.getValue().canGiveReward(player));
		}

		// Nothing for this rarity
		if (rewardSet.elements().isEmpty())
		{
			return null;
		}

		return nextReward(player, treasure, rewardSet);
	}

	/**
	 * This determines the actual reward.
	 *
	 * @param player    The player you want to give the reward to.
	 * @param treasure  The {@link Treasure} you want to search through the pool of.
	 * @param rewardSet The set of reward weights.
	 * @return A random reward that can be given to the player or null if not or an error has occurred.
	 */
	private Reward nextReward(Player player, Treasure treasure, WeightSet<Reward> rewardSet)
	{
		// Get a random reward
		Reward reward = rewardSet.generateRandom();

		// If the chest can award duplicates and the player cannot be given the reward, give a shard reward instead.
		if (!reward.canGiveReward(player))
		{
			if (reward instanceof RankReward)
			{
				return null;
			}
			else if (treasure.isDuplicates())
			{
				return new TreasureShardReward(reward, reward.getRarity());
			}
		}

		return reward;
	}
}
