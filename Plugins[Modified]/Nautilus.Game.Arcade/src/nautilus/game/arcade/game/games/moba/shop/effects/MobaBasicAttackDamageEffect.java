package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;

public class MobaBasicAttackDamageEffect extends MobaItemEffect
{

	private String _reason;
	private double _factor;

	public MobaBasicAttackDamageEffect(String reason, double factor)
	{
		_reason = reason;
		_factor = factor;
	}

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(MobaConstants.BASIC_ATTACK))
		{
			return;
		}

		event.AddMod(_reason, event.GetDamage() * _factor);
	}

	@Override
	public String getDescription()
	{
		return "Increases basic attack damage by " + F.greenElem(format(_factor * 100)) + "%.";
	}
}
