package nautilus.game.arcade.game.games.evolution.kits.perks;

import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAttemptingTickEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.event.EventHandler;

public class PerkEvolveSpeedEVO extends Perk
{
	/**
	 * @author Mysticate
	 */
	
	public PerkEvolveSpeedEVO()
	{
		super("Speedy", new String[]
				{
				"You evolve " + F.elem("25%") + " faster."
				});
	}
	
	@EventHandler
	public void onAbility(EvolutionAttemptingTickEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!Manager.IsAlive(event.getPlayer()))
			return;
		
		if (!Kit.HasKit(event.getPlayer()))
			return;
		
		event.setProgress((float) (event.getProgress() * 1.25));
	}
}
