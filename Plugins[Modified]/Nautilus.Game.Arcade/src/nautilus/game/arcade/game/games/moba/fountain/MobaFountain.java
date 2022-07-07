package nautilus.game.arcade.game.games.moba.fountain;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MobaFountain implements Listener
{

	private static final int FOUNTAIN_SIZE_SQUARED = 50;

	private final Moba _host;
	private final Map<GameTeam, Location> _average;

	public MobaFountain(Moba host)
	{
		_host = host;
		_average = new HashMap<>();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (GameTeam team : _host.GetTeamList())
		{
			_average.put(team, UtilAlg.getAverageLocation(team.GetSpawns()));
		}
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.GetPlayer();

		_host.getArcadeManager().GetCondition().Factory().Regen("Fountain", player, null, 3, 9, false, true, false);
	}

	@EventHandler
	public void updateInFountain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_host.IsLive())
		{
			return;
		}

		for (Player player : _host.GetPlayers(true))
		{
			GameTeam playerTeam = _host.GetTeam(player);

			for (Entry<GameTeam, Location> entry : _average.entrySet())
			{
				GameTeam team = entry.getKey();
				Location location = entry.getValue();

				if (UtilMath.offsetSquared(player.getLocation(), location) > FOUNTAIN_SIZE_SQUARED)
				{
					continue;
				}

				if (playerTeam.equals(team))
				{
					player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 6));
				}
				else
				{
					_host.getArcadeManager().GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 10, false, true, true, "Fountain", "Fountain");
				}
			}
		}
	}
}
