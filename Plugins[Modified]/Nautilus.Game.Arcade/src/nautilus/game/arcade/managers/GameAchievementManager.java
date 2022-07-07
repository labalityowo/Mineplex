package nautilus.game.arcade.managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nautilus.game.arcade.ArcadeManager;

public class GameAchievementManager implements Listener
{
	ArcadeManager Manager;

	public GameAchievementManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}	

	//Ensure that past achievement progress is ignored
	@EventHandler
	public void clearAchievementLog(PlayerJoinEvent event)
	{
		Manager.GetAchievement().clearLog(event.getPlayer());
	}
}