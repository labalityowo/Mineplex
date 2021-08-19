package nautilus.game.arcade.game.games.evolution.kits.perks;

import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.event.EventHandler;

public class PerkCooldownEVO extends Perk
{
	/**
	 * @author Mysticate
	 */
	
	public PerkCooldownEVO()
	{
		super("Cooldown", new String[]
				{
				"All ability cooldowns are reduced by " + F.elem("33%") + "."
				});
	}
	
	@EventHandler
	public void onAbility(EvolutionAbilityUseEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!Manager.IsAlive(event.getPlayer()))
			return;
		
		if (!Kit.HasKit(event.getPlayer()))
			return;
		
		event.setCooldown((long) (event.getCooldown() * .666));
	}
}
