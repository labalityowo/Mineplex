package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkKnockbackFire extends Perk
{
	private double _power;

	public PerkKnockbackFire()
	{
		this(0);
	}

	public PerkKnockbackFire(double power) 
	{
		super("Flaming Knockback", new String[] 
				{
				C.cGray + "You deal " + (int)(power*100) + "% Knockback to burning enemies.",
				});
		
		_power = power;
	}

	@Override
	public void setupValues()
	{
		_power = getPerkDouble("Power");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getFireTicks() <= 0)
			return;
		
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;
				
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		event.AddKnockback("Knockback Fire", _power);
	}
}
