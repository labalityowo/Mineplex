package mineplex.gemhunters.quest.types;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.gemhunters.loot.event.PlayerChestOpenEvent;
import mineplex.gemhunters.quest.Quest;

public class ChestOpenerQuest extends Quest
{

	private final int _goal;

	public ChestOpenerQuest(int id, String name, int startCost, int completeReward, int goal)
	{
		super(id, name, "Open " + F.count(String.valueOf(goal)) + " Chests.", startCost, completeReward);

		_goal = goal;
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

	@EventHandler
	public void chestOpen(PlayerChestOpenEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
		{
			return;
		}
		
		int amount = getAndIncrement(player, 1);
		
		if (amount >= _goal)
		{
			onReward(player);
		}
	}

}
