package nautilus.game.arcade.game.games.gladiators.trackers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.stats.StatTracker;

/**
 * Created by William (WilliamTiger).
 * 08/12/15
 */
public class FlawlessTracker extends StatTracker<Gladiators>
{

	private List<String> _noWin;

	public FlawlessTracker(Gladiators game)
	{
		super(game);

		_noWin = new ArrayList<>();
	}

	@EventHandler
	public void onDmg(CustomDamageEvent e)
	{
		if (e.isCancelled())
			return;

		//System.out.println(e.getEventName() + " took damage by " + e.GetCause().toString());

		if (e.GetDamageeEntity() instanceof Player)
		{
			_noWin.add(((Player)e.GetDamageeEntity()).getName());
		}
	}

	@EventHandler
	public void end(GameStateChangeEvent e)
	{
		if (e.GetState() != Game.GameState.End)
			return;

		if (getGame().getWinners() == null)
			return;

		for (Player p : getGame().getWinners())
		{
			if (_noWin.contains(p.getName()))
				continue;

			addStat(p, "Flawless", 1, true, false);
		}
	}

}