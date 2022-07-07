package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;

public class MobaMeleeDamageEffect extends MobaItemEffect
{

	private String _reason;
	private double _increase;

	public MobaMeleeDamageEffect(String reason, double increase)
	{
		_reason = reason;
		_increase = increase;
	}

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		event.AddMod(_reason, _increase);
	}

	@Override
	public String getDescription()
	{
		return "All your melee attacks deal " + F.greenElem("+" + format(_increase / 2)) + C.cRed + "❤" + C.cGray + ".";
	}
}
