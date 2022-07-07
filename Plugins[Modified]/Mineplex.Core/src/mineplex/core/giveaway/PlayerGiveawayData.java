package mineplex.core.giveaway;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PlayerGiveawayData
{
	private Queue<GiveawayReward> _giveawayRewards;

	public PlayerGiveawayData()
	{
		_giveawayRewards = new LinkedList<GiveawayReward>();
	}

	public void addGiveawayReward(GiveawayReward reward)
	{
		_giveawayRewards.add(reward);
	}

	public Queue<GiveawayReward> getGiveawayRewards()
	{
		return _giveawayRewards;
	}
}
