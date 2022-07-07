
package nautilus.game.arcade.kit.perks;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PerkExplosionModifier extends Perk
{
	private double _explosion;
	
	public PerkExplosionModifier(double explosion)
	{
		super("Enforced Armor", new String[]{"You take " + explosion * 100 + "% explosion damage!"});
		
		_explosion = explosion;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION && event.GetCause() != DamageCause.BLOCK_EXPLOSION)
			return;
		
		if (!(event.GetDamageeEntity() instanceof Player))
			return;
		
		Player player = event.GetDamageePlayer();
		
		if (player == null)	
			return;
				
		if (!Kit.HasKit(player))
			return;
		
		if (!Manager.IsAlive(player))
			return;
		
		event.AddMod("Explosion Modifier", "Reduce damage", (-event.GetDamage()) + event.GetDamageInitial() * _explosion, false);
	}
}
