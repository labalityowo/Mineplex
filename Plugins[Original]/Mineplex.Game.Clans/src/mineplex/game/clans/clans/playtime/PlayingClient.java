package mineplex.game.clans.clans.playtime;

import org.bukkit.entity.Player;

import mineplex.core.task.TaskManager;
import mineplex.game.clans.clans.ClansPlayerTasks;

public class PlayingClient
{
	public long StartTime;
	public boolean FirstSession;
	
	public PlayingClient(Player player, TaskManager taskManager)
	{
		StartTime = System.currentTimeMillis();
		FirstSession = taskManager.hasCompletedTask(player, ClansPlayerTasks.FIRST_SESSION.id());
	}
}