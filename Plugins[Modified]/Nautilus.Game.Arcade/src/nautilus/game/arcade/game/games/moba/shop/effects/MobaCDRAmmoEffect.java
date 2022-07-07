package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.moba.kit.CooldownCalculateEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;

public class MobaCDRAmmoEffect extends MobaItemEffect
{

	private double _factor;

	public MobaCDRAmmoEffect(double factor)
	{
		_factor = factor;
	}

	@Override
	public void onCooldownCheck(CooldownCalculateEvent event)
	{
		if (!event.getAbility().equals(MobaConstants.AMMO))
		{
			return;
		}

		event.decreaseCooldown(_factor);
	}

	@Override
	public String getDescription()
	{
		return "Decreases ammo reload time by " + F.greenElem(format(_factor * 100)) + "%.";
	}
}
