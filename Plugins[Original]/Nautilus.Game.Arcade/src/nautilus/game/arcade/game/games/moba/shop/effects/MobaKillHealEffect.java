package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.entity.Player;

public class MobaKillHealEffect extends MobaItemEffect
{

	private double _health;

	public MobaKillHealEffect(double health)
	{
		_health = health;
	}

	@Override
	public void onDeath(Player killed, Player killer)
	{
		MobaUtil.heal(killer, killer, _health);
	}

	@Override
	public String getDescription()
	{
		return "Killing a player heals for an additional " + F.greenElem(format(_health / 2)) + C.cRed + "❤" + C.cGray + ".";
	}
}
