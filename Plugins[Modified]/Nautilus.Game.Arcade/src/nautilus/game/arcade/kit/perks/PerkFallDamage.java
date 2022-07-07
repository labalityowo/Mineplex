package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkFallDamage extends Perk
{
	private int _mod;
	
	public PerkFallDamage(int mod) 
	{
		super("Feather Falling", new String[] 
				{ 
				C.cGray + "You take " + mod + " damage from falling",
				});
		
		_mod = mod;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageDecrease(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.FALL)
			return;
		
		Player damagee = event.GetDamageePlayer();
		if (damagee == null)	return;
		
		if (!Kit.HasKit(damagee))
			return;
		
		int decrease = 0;
		if (_mod < 0)
		{
			decrease = (int) -Math.min(Math.abs(_mod), event.GetDamageInitial());
		}
		
		event.AddMod(damagee.getName(), GetName(), decrease, false);
	}
}
