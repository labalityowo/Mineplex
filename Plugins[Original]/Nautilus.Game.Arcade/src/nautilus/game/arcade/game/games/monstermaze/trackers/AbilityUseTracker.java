package nautilus.game.arcade.game.games.monstermaze.trackers;

import java.util.ArrayList;
import java.util.List;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.monstermaze.MonsterMaze;
import nautilus.game.arcade.game.games.monstermaze.events.AbilityUseEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class AbilityUseTracker extends StatTracker<MonsterMaze>
{
	/**
	 * @author Mysticate
	 */
	
	private List<String> _out = new ArrayList<String>();
	
	public AbilityUseTracker(MonsterMaze game)
	{
		super(game);
	}

	@EventHandler
	public void onAbilityUse(AbilityUseEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		if (isOut(event.getPlayer()))
			return;
		
		setOut(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		if (getGame().getWinners() == null)
			return;
		
		for (Player player : getGame().getWinners())
		{
			if (isOut(player))
				continue;
			
			addStat(player);
		}
		
		_out.clear();
	}
	
	private boolean isOut(Player player)
	{
		for (String out : _out)
		{
			if (out.equalsIgnoreCase(player.getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	private void setOut(Player player)
	{
		if (isOut(player))
			return;
		
		_out.add(player.getName());
	}
	
	private void addStat(Player player)
	{
		addStat(player, "Hard Mode", 1, true, false);
	}
}
