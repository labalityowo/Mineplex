package nautilus.game.arcade.stats;

import mineplex.minecraft.game.core.damage.*;
import nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class DamageDealtStatTracker extends StatTracker<Game>
{
	public DamageDealtStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)
			return;

		addStat(damager, "Damage Dealt", (int) Math.round(event.GetDamage()), false, false);
		
//		if (getGame().GetKit(damager) != null)
//			addStat(damager, getGame().GetKit(damager).getName() + " Damage Dealt", (int) Math.round(event.GetDamage()), false, false);
	}
}
