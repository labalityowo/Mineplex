package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import org.bukkit.entity.Player;

public class MobaTotalHealthEffect extends MobaItemEffect
{

	private int _health;

	public MobaTotalHealthEffect(int health)
	{
		_health = health;
	}

	@Override
	public void onRespawn(PlayerGameRespawnEvent event, boolean fake)
	{
		Player player = event.GetPlayer();

		player.setMaxHealth(player.getMaxHealth() + _health);

		if (!fake)
		{
			player.setHealth(player.getMaxHealth());
		}
	}

	@Override
	public String getDescription()
	{
		return "Increases total hearts by " + F.greenElem(format(_health / 2)) + C.cRed + "❤" + C.cGray + ".";
	}
}
