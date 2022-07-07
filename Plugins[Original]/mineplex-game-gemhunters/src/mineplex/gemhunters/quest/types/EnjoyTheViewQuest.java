package mineplex.gemhunters.quest.types;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.quest.Quest;

public class EnjoyTheViewQuest extends Quest
{

	private static final int HEIGHT_GOAL = 120;
	private static final int TIME_MIN = 12500;
	private static final int TIME_MAX = 13000;

	public EnjoyTheViewQuest(int id, int startCost, int completeReward)
	{
		super(id, "Enjoy The View", "Climb to the roof of a skyscraper and watch the sun set.", startCost, completeReward);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		long time = _worldData.World.getTime();
		
		if (time < TIME_MIN || time > TIME_MAX)
		{
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			// active and above 120 and they are on the top block (on the roof)
			if (!isActive(player) || player.getLocation().getBlockY() < HEIGHT_GOAL || UtilBlock.getHighest(_worldData.World, player.getLocation().getBlock()).getLocation().getBlockY() > player.getLocation().getBlockY())
			{
				continue;
			}
			
			onReward(player);
		}
	}

}
