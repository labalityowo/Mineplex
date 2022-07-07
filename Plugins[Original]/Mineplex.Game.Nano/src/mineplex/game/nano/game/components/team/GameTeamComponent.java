package mineplex.game.nano.game.components.team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.game.nano.NanoPlayer;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.components.world.GameWorldComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;

public class GameTeamComponent extends GameComponent<Game> implements TeamComponent
{

	private static final String RESPAWN_RECHARGE_KEY = "Respawn";
	private final List<GameTeam> _teams;

	private Function<Player, GameTeam> _selector;
	private boolean _adjustSpawnYaw = true;
	private long _respawnRechargeTime = TimeUnit.SECONDS.toMillis(2);

	public GameTeamComponent(Game game)
	{
		super(game);

		_teams = new ArrayList<>();
		_selector = player -> game.getTeams().stream()
				.min(Comparator.comparingInt(o -> o.getAllPlayers().size()))
				.orElse(null);
	}

	public GameTeamComponent setSelector(Function<Player, GameTeam> selector)
	{
		_selector = selector;
		return this;
	}

	public GameTeamComponent setAdjustSpawnYaw(boolean adjustSpawnYaw)
	{
		_adjustSpawnYaw = adjustSpawnYaw;
		return this;
	}

	public GameTeamComponent setRespawnRechargeTime(long respawnRechargeTime)
	{
		_respawnRechargeTime = respawnRechargeTime;
		return this;
	}

	@Override
	public void disable()
	{
		_teams.clear();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void assignTeams(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			GameTeam team = getGame().getManager().isSpectator(player) ? null : _selector.apply(player);

			if (team == null)
			{
				getGame().addSpectator(player, true, false);
			}
			else
			{
				team.setPlayerAlive(player, true);
				respawnPlayer(player, team);
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		GameTeam team = getTeam(player);

		if (team != null)
		{
			team.setPlayerAlive(player, false);
		}
	}

	@Override
	public GameTeam addTeam(GameTeam gameTeam)
	{
		_teams.add(gameTeam);
		getGame().getManager().log("Created team : " + gameTeam.getChatColour() + gameTeam.getName());
		return gameTeam;
	}

	@Override
	public List<GameTeam> getTeams()
	{
		return _teams;
	}

	@Override
	public void joinTeam(Player player, GameTeam team)
	{
		GameTeam oldTeam = getTeam(player);

		if (oldTeam != null)
		{
			oldTeam.removePlayer(player);
		}

		team.setPlayerAlive(player, true);
	}

	@Override
	public final GameTeam getTeam(Player player)
	{
		return _teams.stream()
				.filter(gameTeam -> gameTeam.hasPlayer(player))
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean isAlive(Player player)
	{
		return _teams.stream()
				.anyMatch(gameTeam -> gameTeam.isAlive(player));
	}

	@Override
	public void respawnPlayer(Player player, GameTeam team)
	{
		Location location = team.getSpawn();

		if (location == null)
		{
			location = getGame().getSpectatorLocation();
		}
		else if (_adjustSpawnYaw && location.getYaw() == 0)
		{
			Location lookAt = getGame().getMineplexWorld().getSpongeLocation("LOOK_AT");

			if (lookAt == null)
			{
				lookAt = getGame().getSpectatorLocation();
			}

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, lookAt)));
		}

		PlayerGameApplyEvent applyEvent = new PlayerGameApplyEvent(player, team, location, true);
		UtilServer.CallEvent(applyEvent);

		if (applyEvent.isClearPlayer())
		{
			NanoPlayer.clear(getGame().getManager(), player);
			NanoPlayer.setSpectating(player, false);

			GameWorldComponent worldComponent = getGame().getManager().getGameWorldManager().getHook();

			if (worldComponent != null && (worldComponent.isBlockBreak() || worldComponent.isBlockPlace()))
			{
				player.setGameMode(GameMode.SURVIVAL);
			}
		}

		player.teleport(applyEvent.getRespawnLocation());
		Recharge.Instance.useForce(player, RESPAWN_RECHARGE_KEY, _respawnRechargeTime);

		UtilServer.CallEvent(new PlayerGameRespawnEvent(player, team));
	}

	@Override
	public boolean hasRespawned(Player player)
	{
		return !Recharge.Instance.usable(player, RESPAWN_RECHARGE_KEY);
	}

	@Override
	public List<Player> getAllPlayers()
	{
		return getTeams().stream()
				.flatMap(gameTeam -> gameTeam.getAllPlayers().stream())
				.collect(Collectors.toList());
	}

	@Override
	public List<Player> getAlivePlayers()
	{
		return getTeams().stream()
				.flatMap(gameTeam -> gameTeam.getAlivePlayers().stream())
				.collect(Collectors.toList());
	}
}
