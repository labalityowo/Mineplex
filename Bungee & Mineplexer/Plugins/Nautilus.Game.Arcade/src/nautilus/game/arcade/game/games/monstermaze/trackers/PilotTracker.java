package nautilus.game.arcade.game.games.monstermaze.trackers;

import java.util.HashMap;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.monstermaze.MonsterMaze;
import nautilus.game.arcade.game.games.monstermaze.events.MonsterBumpPlayerEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PilotTracker extends StatTracker<MonsterMaze>
{
	/**
	 * @author Mysticate
	 */
	
	private HashMap<Player, Long> _launched = new HashMap<Player, Long>();
	
	public PilotTracker(MonsterMaze game)
	{
		super(game);
	}

	@EventHandler
	public void onSnowmanHit(MonsterBumpPlayerEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		if (isLaunched(event.getPlayer()))
			return;
		
		setLaunched(event.getPlayer());
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!getGame().IsLive())
			return;
		
		HashMap<Player, Long> copy = new HashMap<Player, Long>();
		copy.putAll(_launched);
		
		for (Player player : copy.keySet())
		{
			if (!isLaunched(player))
				continue;
			
			//Make sure the player isn't still on the ground after getting hit.
			if (!UtilTime.elapsed(_launched.get(player), 250))
					continue;
			
			if (player == null || !player.isOnline() || !getGame().IsAlive(player))
			{
				_launched.remove(player);				
				continue;
			}
			
			if (UtilEnt.isGrounded(player))
			{				
				_launched.remove(player);
								
				if (getGame().getMaze().isOnPad(player, false))
				{			
					addStat(player);
				}
				
				continue;
			}

			if (UtilTime.elapsed(_launched.get(player), 2000))
			{	
				_launched.remove(player);
				continue;
			}
		}
	}
	
	@EventHandler
	public void clearLaunched(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		_launched.clear();
	}
	
	private boolean isLaunched(Player player)
	{
		return _launched.containsKey(player);
	}
	
	private void setLaunched(Player player)
	{
		_launched.put(player, System.currentTimeMillis());
	}
	
	private void addStat(Player player)
	{
		addStat(player, "Pilot", 1, true, false);
	}
}
