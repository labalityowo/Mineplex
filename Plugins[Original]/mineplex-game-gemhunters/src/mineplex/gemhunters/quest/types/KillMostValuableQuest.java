package mineplex.gemhunters.quest.types;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.quest.Quest;

public class KillMostValuableQuest extends Quest
{

	public KillMostValuableQuest(int id, String name, String description, int startCost, int completeReward)
	{
		super(id, name, description, startCost, completeReward);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}
		
		Player player = getMostValuable();
		String display = C.cRed + "The most valuable player is " + C.cYellow + (player != null ? player.getName() : "No one");
		
		for (Player other : Bukkit.getOnlinePlayers())
		{
			if (!isActive(other) || !_quest.getItemStack(this, other, false, true, true).isSimilar(other.getItemInHand()))
			{
				continue;
			}
			
			UtilTextBottom.display(display, other);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = getMostValuable();
		
		if (player == null || !event.getEntity().equals(player))
		{
			return;
		}
		
		Player killer = player.getKiller();
		
		if (!isActive(killer))
		{
			return;
		}
		
		onReward(killer);
	}

	private Player getMostValuable()
	{
		Player mostGemsPlayer = null;
		int mostGems = 0;
		
		for (Player player : Bukkit.getOnlinePlayers())
		{
			int gems = _economy.Get(player);
			
			if (gems > mostGems)
			{
				mostGemsPlayer = player;
				mostGems = gems;
			}
		}
		
		return mostGemsPlayer;
	}
}
