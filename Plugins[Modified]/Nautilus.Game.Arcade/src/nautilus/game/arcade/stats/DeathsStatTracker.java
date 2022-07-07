package nautilus.game.arcade.stats;

import org.bukkit.event.EventHandler;

import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.game.Game;

public class DeathsStatTracker extends StatTracker<Game>
{

	public DeathsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		addStat(event.GetEvent().getEntity(), "Deaths", 1, false, false);
	}
}
