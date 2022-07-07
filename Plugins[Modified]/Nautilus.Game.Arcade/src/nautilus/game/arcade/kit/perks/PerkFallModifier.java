package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.Managers;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageManager;
import nautilus.game.arcade.kit.Perk;

public class PerkFallModifier extends Perk
{
	private double _fall;
	
	public PerkFallModifier(double fall)
	{
		super("Feathered Boots", new String[]{"You take " + fall * 100 + "% fall damage!"});
		
		_fall = fall;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.FALL)
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
		
		event.AddMod("Fall Modifier", "Reduce damage", _fall, false);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void handle(EntityDamageEvent event)
	{
		if (Managers.get(DamageManager.class).IsEnabled())
		{
			return;
		}
		if (event.getCause() != DamageCause.FALL)
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
				
		if (!Kit.HasKit(player))
			return;
		
		if (!Manager.IsAlive(player))
			return;
		
		event.setDamage(event.getDamage() * _fall);
	}
}