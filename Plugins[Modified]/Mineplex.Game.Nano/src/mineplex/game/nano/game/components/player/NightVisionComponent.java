package mineplex.game.nano.game.components.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;

public class NightVisionComponent extends GameComponent<Game>
{

	public NightVisionComponent(Game game)
	{
		super(game, GameState.Prepare, GameState.Live, GameState.End);
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void giveNightVision(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : getGame().getMineplexWorld().getWorld().getPlayers())
		{
			if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
			}
		}
	}
}
