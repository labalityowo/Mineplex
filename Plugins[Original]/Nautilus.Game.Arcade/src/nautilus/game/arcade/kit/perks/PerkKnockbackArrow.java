package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.C;
import nautilus.game.arcade.kit.Perk;

public class PerkKnockbackArrow extends Perk
{
	private double _power;

	public PerkKnockbackArrow()
	{
		this(0);
	}

	public PerkKnockbackArrow(double power) 
	{
		super("Knockback Arrow");
		
		_power = power;
	}

	@Override
	public void setupValues()
	{
		_power = getPerkPercentage("Power", _power);

		setDesc(C.cGray + "Arrows deal " + (int)(_power * 100) + "% Knockback.");
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;

		if (!(event.GetProjectile() instanceof Arrow))
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;

		event.AddKnockback(GetName(), _power);
	}
}
