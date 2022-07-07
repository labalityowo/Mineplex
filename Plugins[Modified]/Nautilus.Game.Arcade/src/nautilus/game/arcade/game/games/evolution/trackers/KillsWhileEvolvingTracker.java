package nautilus.game.arcade.game.games.evolution.trackers;

import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.games.evolution.Evolution;
import nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class KillsWhileEvolvingTracker extends StatTracker<Evolution>
{	
	/**
	 * @author Mysticate
	 */
	
	public KillsWhileEvolvingTracker(Evolution game)
	{
		super(game);
	}
	
	@EventHandler
	public void onDamage(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		if(event.GetLog().GetKiller() == null) return;
		
		Player damagee = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		Player damager = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (damagee == null || damager == null)
			return;
		
		if (!getGame().IsAlive(damagee) || !getGame().IsAlive(damager))
			return;
		
		if (getGame().isAttemptingEvolve(damagee))
			addStat(damager, "EvolveKill", 1, false, false);
	}
}
