package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.kit.hp.MobaHPRegenEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class MobaAbilityDamageEffect extends MobaItemEffect
{

	private String _reason;
	private double _factor;

	public MobaAbilityDamageEffect(String reason, double factor)
	{
		_reason = reason;
		_factor = factor;
	}

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.CUSTOM || event.GetReason().contains(MobaConstants.BASIC_ATTACK))
		{
			return;
		}

		event.AddMod(_reason, event.GetDamage() * _factor);
	}

	@Override
	protected void onHPRegenOthers(MobaHPRegenEvent event)
	{
		if (event.isNatural())
		{
			return;
		}

		event.increaseHealth(_factor);
	}

	@Override
	public String getDescription()
	{
		return "Increases ability damage/healing by " + F.greenElem(format(_factor * 100)) + "%.";
	}
}
