package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

public class MobaHitArrowHealEffect extends MobaItemEffect
{

	private double _factor;

	public MobaHitArrowHealEffect(double factor)
	{
		_factor = factor;
	}

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		if (!(event.GetProjectile() instanceof Arrow))
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + (event.GetDamage() * _factor)));
	}

	@Override
	public String getDescription()
	{
		return "Hitting a player with an arrow heals for " + F.greenElem(format(_factor * 100)) + "% of the damage.";
	}
}
