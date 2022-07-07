package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.common.util.F;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import org.bukkit.entity.Player;

public class MobaSpeedEffect extends MobaItemEffect
{

	private double _factor;

	public MobaSpeedEffect(double factor)
	{
		_factor = factor;
	}

	@Override
	public void onRespawn(PlayerGameRespawnEvent event, boolean fake)
	{
		Player player = event.GetPlayer();

		player.setWalkSpeed((float) (player.getWalkSpeed() + (player.getWalkSpeed() * _factor)));
	}

	@Override
	public String getDescription()
	{
		return "Increases movement speed by " + F.greenElem(format(_factor * 100)) + "%.";
	}
}
