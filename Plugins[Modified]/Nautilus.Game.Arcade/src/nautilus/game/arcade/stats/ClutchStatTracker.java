package nautilus.game.arcade.stats;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.common.CaptureTheFlag;
import nautilus.game.arcade.game.games.common.ctf_data.CarrierCombatDeathEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class ClutchStatTracker extends StatTracker<CaptureTheFlag>
{
	private final String _stat;

	public ClutchStatTracker(CaptureTheFlag game, String stat)
	{
		super(game);
		_stat = stat;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CarrierCombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;
		
		if (!getGame().isSuddenDeath())
			return;

		addStat(event.GetPlayer(true), getStat(), 1, false, false);
	}
	
	public String getStat()
	{
		return _stat;
	}

}
