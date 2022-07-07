package nautilus.game.arcade.game.games.typewars.stats;

import nautilus.game.arcade.game.games.typewars.ActivateNukeSpellEvent;
import nautilus.game.arcade.game.games.typewars.TypeWars;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class WaitForItStatTracker extends StatTracker<TypeWars>
{

	public WaitForItStatTracker(TypeWars game)
	{
		super(game);
	}
	
	@EventHandler
	public void nuke(ActivateNukeSpellEvent event)
	{
		if(event.getMinions().size() >= 30)
			addStat(event.getPlayer(), "Nuke", 1, true, false);
	}

}
