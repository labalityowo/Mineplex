package mineplex.gemhunters.quest.types;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.gemhunters.economy.event.PlayerEarnGemsEvent;
import mineplex.gemhunters.loot.rewards.LootRankReward;
import mineplex.gemhunters.quest.Quest;

public class KillPlayerQuest extends Quest
{

	private final int _goal;
	private final String _chest;

	public KillPlayerQuest(int id, String name, int startCost, int completeReward, int goal, String chest)
	{
		super(id, name, "Kill " + F.count(String.valueOf(goal)) + " players", startCost, completeReward);

		_goal = goal;
		_chest = chest;
	}

	@Override
	public float getProgress(Player player)
	{
		return (float) get(player) / (float) _goal;
	}

	@Override
	public int getGoal()
	{
		return _goal;
	}

	@Override
	public String getRewardString()
	{
		return C.cAqua + _chest;
	}
	
	@EventHandler
	public void earnGems(PlayerEarnGemsEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player) || event.getReason() == null || !event.getReason().startsWith("Killing"))
		{
			return;
		}

		int amount = getAndIncrement(player, 1);

		if (amount >= _goal)
		{
			if (_chest.equals("Rank Upgrade"))
			{
				new LootRankReward(null).success();
			}
			else
			{
				_inventory.addItemToInventory(player, _chest, 1);
			}
			
			onReward(player);
		}
	}

}
