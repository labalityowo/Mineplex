package nautilus.game.arcade.game.games.speedbuilders.stattrackers;

import java.util.Map.Entry;

import mineplex.core.common.util.NautHashMap;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PerfectionistTracker extends StatTracker<SpeedBuilders>
{
	private SpeedBuilders _game;

	private NautHashMap<Player, Integer> _perfectBuilds = new NautHashMap<Player, Integer>();

	public PerfectionistTracker(SpeedBuilders game)
	{
		super(game);
		
		_game = game;
	}

	@EventHandler
	public void onPerfectBuild(PerfectBuildEvent event)
	{
		int previousPerfectBuilds = _perfectBuilds.containsKey(event.getPlayer()) ? _perfectBuilds.get(event.getPlayer()) : 0;
		
		_perfectBuilds.put(event.getPlayer(), previousPerfectBuilds + 1);
	}

	@EventHandler
	public void onEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;
		
		for (Entry<Player, Integer> entry : _perfectBuilds.entrySet())
		{
			if (entry.getValue() == _game.getRoundsPlayed())
				addStat(entry.getKey(), "PerfectWins", 1, true, false);
		}
	}
}