package mineplex.gemhunters.quest.types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.quest.Quest;

public class LocationQuest extends Quest
{

	private static final int DISTANCE_SQUARED = 100;
	
	private final Location _goal;
	
	public LocationQuest(int id, String name, String description, int startCost, int completeReward, String locationKey)
	{
		super(id, name, description, startCost, completeReward);
		
		_goal = _worldData.getCustomLocation(locationKey).get(0);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!isActive(player) || UtilMath.offsetSquared(player.getLocation(), _goal) > DISTANCE_SQUARED)
			{
				continue;
			}
			
			onReward(player);
		}
	}

}
