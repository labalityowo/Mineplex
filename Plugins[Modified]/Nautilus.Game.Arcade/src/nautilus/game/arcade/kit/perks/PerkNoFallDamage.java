package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkNoFallDamage extends Perk
{
	public PerkNoFallDamage()
	{
		super("Feathered Boots", new String[]{"You take no fall damage!"});
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!(event.GetDamageeEntity() instanceof Player))
			return;
		
		if (!Kit.HasKit(event.GetDamageePlayer()))
			return;
			
		if (event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Feathered Boots");
		}
	}
}
