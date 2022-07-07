package nautilus.game.arcade.game.games.minecraftleague.objective;

import java.util.concurrent.ConcurrentHashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class ObjectiveManager implements Listener
{
	private MinecraftLeague _host;
	private GameObjective _main;
	private ConcurrentHashMap<Player, GameObjective> _specificObjectives = new ConcurrentHashMap<Player, GameObjective>();
	private ConcurrentHashMap<GameTeam, GameObjective> _teamObjectives = new ConcurrentHashMap<GameTeam, GameObjective>();
	
	public ObjectiveManager(MinecraftLeague host)
	{
		_host = host;
		Bukkit.getPluginManager().registerEvents(this, _host.getArcadeManager().getPlugin());
	}
	
	private void displayObjective(Player player)
	{
		GameObjective obj = _main;
		if (_teamObjectives.containsKey(_host.GetTeam(player)))
			obj = _teamObjectives.get(_host.GetTeam(player));
		if (_specificObjectives.containsKey(player))
			obj = _specificObjectives.get(player);
		
		UtilTextBottom.display(C.cGold + "Objective: " + obj.getDisplayText(), player);
	}
	
	public String getMainObjective()
	{
		return _main.getID();
	}
	
	public String getTeamObjective(GameTeam team)
	{
		if (_teamObjectives.containsKey(team))
			return _teamObjectives.get(team).getID();
		
		return getMainObjective();
	}
	
	public String getPlayerObjective(Player player)
	{
		if (_specificObjectives.containsKey(player))
			return _specificObjectives.get(player).getID();
		
		return getTeamObjective(_host.GetTeam(player));
	}
	
	public void setMainObjective(GameObjective objective)
	{
		_main = objective;
	}
	
	public void setPlayerObjective(Player player, GameObjective objective)
	{
		resetPlayerToMainObjective(player);
		_specificObjectives.put(player, objective);
	}
	
	public void resetPlayerToMainObjective(Player player)
	{
		if (!_specificObjectives.containsKey(player))
			return;
		_specificObjectives.remove(player);
	}
	
	public void setTeamObjective(GameTeam team, GameObjective objective)
	{
		if (getTeamObjective(team).equalsIgnoreCase("KILL_ENEMY"))
			return;
		
		resetTeamToMainObjective(team);
		_teamObjectives.put(team, objective);
	}
	
	public void resetTeamToMainObjective(GameTeam team)
	{
		if (!_teamObjectives.containsKey(team))
			return;
		_teamObjectives.remove(team);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!_host.IsLive())
			return;
		if (event.getType() != UpdateType.FASTEST)
			return;
		
		for (Player player : _host.GetPlayers(true))
		{
			if (_main != null || _specificObjectives.containsKey(player) || _teamObjectives.containsKey(_host.GetTeam(player)))
				displayObjective(player);
		}
	}
	
	@EventHandler
	public void handleDeregister(GameStateChangeEvent event)
	{
		if (event.GetGame() != _host)
			return;
		
		if (event.GetState() != GameState.Dead)
			return;
		
		HandlerList.unregisterAll(this);
	}
}
