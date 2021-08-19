package nautilus.game.arcade.stats;

import mineplex.minecraft.game.core.damage.*;
import nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class DamageTakenStatTracker extends StatTracker<Game>
{
	public DamageTakenStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null)
			return;
		
		addStat(damagee, "Damage Taken", (int) Math.round(event.GetDamage()), false, false);
		if (event.GetDamagerPlayer(true) != null)
			addStat(damagee, "Damage Taken PvP ", (int) Math.round(event.GetDamage()), false, false);
		
//		if (getGame().GetKit(damagee) != null)
//		{
//			addStat(damagee, getGame().GetKit(damagee).getName() + " Damage Taken", (int) Math.round(event.GetDamage()), false, false);
//			
//			if (event.GetDamagerPlayer(true) != null)
//				addStat(damagee, getGame().GetKit(damagee).getName() + " Damage Taken PvP ", (int) Math.round(event.GetDamage()), false, false);
//		}
	}
}
