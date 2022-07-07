package nautilus.game.arcade.kit.perks;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkKnockback extends Perk
{

	private final double _power;

	public PerkKnockback(double power)
	{
		super("Knockback", new String[]
				{
						C.cGray + "Attacks gives knockback with " + C.cGreen + power + C.cGray + " power.",
				});

		_power = power;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null) return;

		if (!hasPerk(damager))
			return;

		if (!Manager.IsAlive(damager))
			return;

		event.SetKnockback(false);

		if (!Recharge.Instance.use(damager, "KB " + UtilEnt.getName(event.GetDamageeEntity()), 400, false, false))
			return;

		event.GetDamageeEntity().playEffect(EntityEffect.HURT);

		UtilAction.velocity(event.GetDamageeEntity(),
				UtilAlg.getTrajectory(damager, event.GetDamageeEntity()),
				_power, false, 0, 0.1, 10, true);
	}
}
