package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.moba.kit.hp.MobaHPRegenEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;

public class MobaHPRegenEffect extends MobaItemEffect
{

	private double _factor;

	public MobaHPRegenEffect(double factor)
	{
		_factor = factor;
	}

	@Override
	public void onHPRegen(MobaHPRegenEvent event)
	{
		event.increaseHealth(_factor);
	}

	@Override
	public String getDescription()
	{
		return "Increases HP regeneration by " + F.greenElem(format(_factor * 100)) + "%.";
	}
}
