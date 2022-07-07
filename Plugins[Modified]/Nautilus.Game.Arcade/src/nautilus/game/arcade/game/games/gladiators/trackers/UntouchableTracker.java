package nautilus.game.arcade.game.games.gladiators.trackers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.game.games.gladiators.events.RoundStartEvent;
import nautilus.game.arcade.stats.StatTracker;

/**
 * Created by William (WilliamTiger).
 * 10/12/15
 */
public class UntouchableTracker extends StatTracker<Gladiators>
{
	private List<String> _noWin;

	public UntouchableTracker(Gladiators game)
	{
		super(game);

		_noWin = new ArrayList<>();
	}

	@EventHandler
	public void onDmg(CustomDamageEvent e)
	{
		if (e.isCancelled())
			return;

		if (e.GetDamageeEntity() instanceof Player)
		{
			_noWin.add(((Player)e.GetDamageeEntity()).getName());
		}
	}

	@EventHandler
	public void onEnd(RoundStartEvent e)
	{
		for (Player p : getGame().GetPlayers(true))
		{
			if (_noWin.contains(p.getName()))
				continue;

			addStat(p, "Untouchable", 1, false, false);
		}

		_noWin.clear();
	}

}