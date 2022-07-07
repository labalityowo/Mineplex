package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkKnockbackGive extends Perk
{
	private double _power;
	
	public PerkKnockbackGive(double power) 
	{
		super("Knockback", new String[] 
				{
				C.cGray + "You deal " + (int)(power*100) + "% Knockback.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!hasPerk(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		event.AddKnockback("Knockback Multiplier", _power);
	}
}
