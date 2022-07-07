package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;

public class MobaConditionImmunityEffect extends MobaItemEffect
{

	private ConditionType _conditionType;

	public MobaConditionImmunityEffect(ConditionType conditionType)
	{
		_conditionType = conditionType;
	}

	@Override
	protected void onCondition(ConditionApplyEvent event)
	{
		if (event.GetCondition().GetType() == _conditionType)
		{
			event.setCancelled(true);
		}
	}

	@Override
	public String getDescription()
	{
		return "Grants immunity to " + F.greenElem(format(_conditionType, -1)) + " effects.";
	}
}
