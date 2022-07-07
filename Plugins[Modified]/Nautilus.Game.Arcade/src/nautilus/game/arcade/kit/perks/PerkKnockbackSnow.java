package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkKnockbackSnow extends Perk
{
	private double _power;
	
	public PerkKnockbackSnow(double power) 
	{
		super("Frosty Knockback", new String[] 
				{
				C.cGray + "You deal " + (int)(power*100) + "% Knockback to enemies on snow.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getLocation().getBlock().getTypeId() != 78)
			return;
		
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;
				
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		event.AddKnockback("Knockback Snow", _power);
	}
}
