package nautilus.game.arcade.game.games.gladiators.trackers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.games.gladiators.Gladiators;
import nautilus.game.arcade.stats.StatTracker;

/**
 * Created by William (WilliamTiger).
 * 08/12/15
 */
public class BrawlerTracker extends StatTracker<Gladiators>
{
	public BrawlerTracker(Gladiators game)
	{
		super(game);
	}

	@EventHandler
	public void death(CombatDeathEvent e)
	{
		if (e.GetLog().GetKiller() == null)
			return;

		if (Bukkit.getPlayerExact(e.GetLog().GetKiller().GetName()) != null)
		{
			Player p = Bukkit.getPlayerExact(e.GetLog().GetKiller().GetName());
			if (p.getItemInHand() == null)
				return;

			if (p.getItemInHand().getType().equals(Material.AIR))
				addStat(p, "Brawler", 1, false, false);
		}
	}
}