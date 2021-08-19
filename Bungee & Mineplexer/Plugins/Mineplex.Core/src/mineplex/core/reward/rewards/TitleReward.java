package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.treasure.reward.RewardRarity;

public class TitleReward extends Reward
{

	private final ItemStack ITEM_STACK = new ItemStack(Material.BOOK);
	private final TrackManager _trackManager = Managers.require(TrackManager.class);
	private final Track _track;

	public TitleReward(Track track, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		if (track == null)
		{
			throw new IllegalStateException();
		}

		_track = track;
	}

	@Override
	protected RewardData giveRewardCustom(Player player)
	{
		_trackManager.unlockTrack(player, _track);
		return getFakeRewardData(player);
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + "Title", getRarity().getColor() + _track.getLongName(), ITEM_STACK, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return !_trackManager.hasTrack(player, _track);
	}
}
