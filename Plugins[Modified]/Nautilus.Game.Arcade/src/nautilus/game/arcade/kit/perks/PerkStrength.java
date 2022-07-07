package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.C;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkStrength extends Perk
{
	private int _power;
	
	public PerkStrength(int power) 
	{
		super("Strength", new String[] 
				{ 
				C.cGray + "You deal " + power + " more damage",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageDecrease(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;
		
		if (!hasPerk(damager))
			return;
		
		event.AddMod(damager.getName(), GetName(), _power, false);
	}
}
