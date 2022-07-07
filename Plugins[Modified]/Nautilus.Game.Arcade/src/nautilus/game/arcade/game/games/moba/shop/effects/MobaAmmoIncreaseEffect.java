package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.moba.kit.AmmoGiveEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;

public class MobaAmmoIncreaseEffect extends MobaItemEffect
{

	private int _maxAmmoIncrease;

	public MobaAmmoIncreaseEffect(int maxAmmoIncrease)
	{
		_maxAmmoIncrease = maxAmmoIncrease;
	}

	@Override
	protected void onAmmoGive(AmmoGiveEvent event)
	{
		event.setMaxAmmo(event.getMaxAmmo() + _maxAmmoIncrease);
	}

	@Override
	public String getDescription()
	{
		return "Increases max ammo by " + F.greenElem(format(_maxAmmoIncrease)) + ".";
	}
}
