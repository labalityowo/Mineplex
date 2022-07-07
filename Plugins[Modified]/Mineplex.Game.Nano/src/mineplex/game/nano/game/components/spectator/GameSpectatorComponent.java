package mineplex.game.nano.game.components.spectator;

import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.game.nano.NanoPlayer;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.PlayerDeathOutEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public class GameSpectatorComponent extends GameComponent<Game> implements SpectatorComponent
{

	private boolean _deathOut = true;
	private Location _spectatorLocation;

	public GameSpectatorComponent(Game game)
	{
		super(game);
	}

	public GameSpectatorComponent setDeathOut(boolean deathOut)
	{
		_deathOut = deathOut;
		return this;
	}

	@Override
	public void disable()
	{
		_spectatorLocation = null;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		event.getDrops().clear();

		GameTeam team = getGame().getTeam(player);

		if (team == null)
		{
			return;
		}

		// Prevent death
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());

		if (_deathOut || !getGame().isLive())
		{
			PlayerDeathOutEvent deathOutEvent = new PlayerDeathOutEvent(player);
			UtilServer.CallEvent(deathOutEvent);

			if (deathOutEvent.isCancelled())
			{
				if (deathOutEvent.shouldRespawn())
				{
					getGame().respawnPlayer(player, team);
				}

				return;
			}

			Location location = player.getLocation();
			boolean teleport = location.getY() < getGame().getMineplexWorld().getMin().getY() || location.getBlock().isLiquid();

			addSpectator(player, teleport, true);

			UtilTextMiddle.display(C.cRed + "You Died", "Don't quit! A new game will start shortly.", 0, 60, 20, player);
		}
		else
		{
			getGame().respawnPlayer(player, team);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		getGame().addSpectator(player, true, false);
		UtilTextMiddle.display(C.cYellow + "Spectator", "You will join the next game!", 0, 60, 0, player);
	}

	@Override
	public void addSpectator(Player player, boolean teleport, boolean out)
	{
		if (out)
		{
			GameTeam team = getGame().getTeam(player);

			if (team != null)
			{
				team.setPlayerAlive(player, false);
			}
			else
			{
				UtilServer.CallEvent(new PlayerStateChangeEvent(player, null, false));
			}
		}

		NanoPlayer.clear(getGame().getManager(), player);

		// Make them invisible
		NanoPlayer.setSpectating(player, true);
		getGame().getManager().getConditionManager().Factory().Cloak("Spectator", player, player, Integer.MAX_VALUE, true, true);

		// Flight
		player.setAllowFlight(true);
		player.setFlying(true);

		if (teleport)
		{
			player.teleport(getSpectatorLocation());
		}
	}

	@Override
	public Location getSpectatorLocation()
	{
		if (_spectatorLocation == null)
		{
			if (getGame().getTeams().isEmpty())
			{
				_spectatorLocation = new Location(getGame().getMineplexWorld().getWorld(), 0, 100, 0);
			}
			else
			{
				_spectatorLocation = UtilAlg.getAverageLocation(getGame().getTeams().stream()
						.flatMap(team -> team.getSpawns().stream())
						.collect(Collectors.toList())).add(0, 3, 0);
			}
		}

		return _spectatorLocation;
	}
}
