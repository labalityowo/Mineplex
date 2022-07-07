package nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBackstab extends Perk
{
	public PerkBackstab() 
	{
		super("Backstab", new String[] 
				{ 
				C.cGray + "Deal +2 damage from behind opponents.",
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		if (event.GetDamageInitial() <= 1)
			return;

		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;

		if (!Kit.HasKit(damager) || !Recharge.Instance.usable(damager, GetName()))
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
			event.AddMod(damager.getName(), GetName(), 2, true);
			
			//Effect
			damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 2f);	
			return;
		}
	}
}
