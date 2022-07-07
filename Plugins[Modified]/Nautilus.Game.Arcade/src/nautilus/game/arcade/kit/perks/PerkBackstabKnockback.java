package nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBackstabKnockback extends Perk
{
	public PerkBackstabKnockback() 
	{
		super("Backstab", new String[] 
				{ 
				C.cGray + "+250% Knockback from behind opponents.",
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;
		
		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		Vector look = damagee.getLocation().getDirection();
		look.setY(0);
		look.normalize();

		Vector from = damager.getLocation().toVector().subtract(damagee.getLocation().toVector());
		from.setY(0);
		from.normalize();

		Vector check = new Vector(look.getX() * -1, 0, look.getZ() * -1);
		if (check.subtract(from).length() < 0.8)
		{
			//Damage
			event.AddKnockback("Backstab Knockback", 2.5);
			
			//Effect
			damagee.getWorld().playSound(damagee.getLocation(), Sound.PIG_DEATH, 1f, 2f);	
			return;
		}
	}
}
