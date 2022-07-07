package nautilus.game.arcade.kit.perks;

import java.util.Map.Entry;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PerkKnockbackTaken extends Perk
{
	private double _knockback;
	
	public PerkKnockbackTaken(double knockback)
	{
		super("Knockback", new String[]{"You take " + knockback * 100 + "% knockback!"});
		
		_knockback = knockback;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (!(event.GetDamageeEntity() instanceof Player))
			return;
		
		Player player = event.GetDamageePlayer();
		
		if (player == null)	
			return;
				
		if (!Kit.HasKit(player))
			return;
		
		if (!Manager.IsAlive(player))
			return;
		
		//Multiply all knockback
		for (Entry<String, Double> entry : event.GetKnockback().entrySet())
			entry.setValue(entry.getValue() * _knockback);
	}
}
