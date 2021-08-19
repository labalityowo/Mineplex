package nautilus.game.arcade.game.games.minecraftleague.variation;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;
import nautilus.game.arcade.world.WorldData;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class GameVariation implements Listener
{
	public MinecraftLeague Host;
	public ArcadeManager Manager;
	public WorldData WorldData;
	
	public GameVariation(MinecraftLeague host)
	{
		Host = host;
		Manager = host.getArcadeManager();
		WorldData = host.WorldData;
	}
	
	public void ParseData()
	{
		
	}
	
	public String[] getTeamScoreboardAdditions(GameTeam team)
	{
		return new String[]{};
	}
	
	public void deregister()
	{
		HandlerList.unregisterAll(this);
	}
	
	public abstract void customDeregister();
}
