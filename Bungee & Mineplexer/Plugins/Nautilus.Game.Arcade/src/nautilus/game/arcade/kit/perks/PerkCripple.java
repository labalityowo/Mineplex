package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkCripple extends Perk
{
	private int _power;
	private double _time;
	
	public PerkCripple(int power, double time) 
	{
		super("Knockback", new String[] 
				{
				C.cGray + "Attacks give Slow " + power + " for " + time + " seconds.",
				});
		
		_power = power;
		_time = time;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		event.SetKnockback(false);
		
		Manager.GetCondition().Factory().Slow("Cripple", event.GetDamageeEntity(), damager, _time, _power, false, false, false, false);
	}
}
