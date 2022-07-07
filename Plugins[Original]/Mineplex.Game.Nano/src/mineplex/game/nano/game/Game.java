package mineplex.game.nano.game;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.mineplex.anticheat.checks.move.Glide;

import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.UtilServer;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.lifetimes.PhasedLifetime;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.components.compass.GameCompassComponent;
import mineplex.game.nano.game.components.currency.CurrencyComponent;
import mineplex.game.nano.game.components.damage.GameDamageComponent;
import mineplex.game.nano.game.components.end.GameEndComponent;
import mineplex.game.nano.game.components.player.GamePlayerComponent;
import mineplex.game.nano.game.components.prepare.GamePrepareComponent;
import mineplex.game.nano.game.components.scoreboard.GameScoreboardComponent;
import mineplex.game.nano.game.components.spectator.GameSpectatorComponent;
import mineplex.game.nano.game.components.spectator.SpectatorComponent;
import mineplex.game.nano.game.components.stats.GameStatsComponent;
import mineplex.game.nano.game.components.stats.GeneralStatsTracker;
import mineplex.game.nano.game.components.stats.StatsComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.components.team.GameTeamComponent;
import mineplex.game.nano.game.components.team.TeamComponent;
import mineplex.game.nano.game.components.world.GameWaterComponent;
import mineplex.game.nano.game.components.world.GameWorldComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.world.GameWorld;

public abstract class Game extends ListenerComponent implements Lifetimed, TeamComponent, SpectatorComponent, CurrencyComponent, StatsComponent
{

	public enum GameState
	{
		Loading, Prepare, Live, End, Dead
	}

	private final PhasedLifetime<GameState> _lifetime;

	protected final NanoManager _manager;
	private final GameType _gameType;
	private final String[] _description;

	// World Data
	protected GameWorld _gameWorld;
	protected MineplexWorld _mineplexWorld;

	// Game State
	private long _stateTime;

	// Standard Components
	protected final GamePrepareComponent _prepareComponent;
	protected final GameTeamComponent _teamComponent;
	protected final GameSpectatorComponent _spectatorComponent;
	protected final GameScoreboardComponent _scoreboardComponent;
	protected final GameDamageComponent _damageComponent;
	protected final GameWorldComponent _worldComponent;
	protected final GamePlayerComponent _playerComponent;
	protected final GameWaterComponent _waterComponent;
	protected final GameCompassComponent _compassComponent;
	protected final GameStatsComponent _statsComponent;
	protected final GameEndComponent _endComponent;

	// Winners
	private GamePlacements _placements;
	private GameTeam _winningTeam;

	public Game(NanoManager manager, GameType gameType, String[] description)
	{
		_lifetime = new PhasedLifetime<>();
		_lifetime.register(this);
		_lifetime.start(GameState.Loading);

		_manager = manager;
		_gameType = gameType;
		_description = description;

		_prepareComponent = new GamePrepareComponent(this);
		_teamComponent = new GameTeamComponent(this);
		_spectatorComponent = new GameSpectatorComponent(this);
		_scoreboardComponent = new GameScoreboardComponent(this);
		_damageComponent = new GameDamageComponent(this);
		_worldComponent = new GameWorldComponent(this);
		_playerComponent = new GamePlayerComponent(this);
		_waterComponent = new GameWaterComponent(this);
		_compassComponent = new GameCompassComponent(this);
		_statsComponent = new GameStatsComponent(this);
		_endComponent = new GameEndComponent(this);

		new GeneralStatsTracker(this);
		
		manager.getAntiHack().addIgnoredCheck(Glide.class);
	}

	public final void setupGameWorld(File mapZip)
	{
		_gameWorld = new GameWorld(this, mapZip);
		_gameWorld.loadWorld();
	}

	public final void setupMineplexWorld(MineplexWorld mineplexWorld)
	{
		_mineplexWorld = mineplexWorld;

		createTeams();
		parseData();

		setState(GameState.Prepare);
	}

	protected abstract void createTeams();

	protected abstract void parseData();

	public abstract boolean endGame();

	public abstract void disable();

	@Override
	public final PhasedLifetime<GameState> getLifetime()
	{
		return _lifetime;
	}

	@Override
	public final void deactivate()
	{
		disable();

		super.deactivate();

		AntiHack antiHack = _manager.getAntiHack();
		antiHack.resetIgnoredChecks();
	}

	public final NanoManager getManager()
	{
		return _manager;
	}

	public final GameType getGameType()
	{
		return _gameType;
	}

	public final String[] getDescription()
	{
		return _description;
	}

	public GameWorld getGameWorld()
	{
		return _gameWorld;
	}

	public final MineplexWorld getMineplexWorld()
	{
		return _mineplexWorld;
	}

	public final void setState(GameState state)
	{
		_lifetime.setPhase(state);

		_manager.log("Game State set to " + state);
		UtilServer.CallEvent(new GameStateChangeEvent(this, state));

		_stateTime = System.currentTimeMillis();
	}

	public final GameState getState()
	{
		return _lifetime.getPhase();
	}

	public final boolean isLive()
	{
		return getState() == GameState.Live;
	}

	public final boolean inProgress()
	{
		return getState() == GameState.Prepare || isLive();
	}

	public final long getStateTime()
	{
		return _stateTime;
	}

	public void announce(String message)
	{
		announce(message, Sound.NOTE_PLING);
	}

	public void announce(String message, Sound sound)
	{
		Bukkit.broadcastMessage(message);

		if (sound != null)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				player.playSound(player.getLocation(), sound, 1, 1);
			}
		}
	}

	/*
		Teams
	 */

	@Override
	public GameTeam addTeam(GameTeam gameTeam)
	{
		return _teamComponent.addTeam(gameTeam);
	}

	@Override
	public List<GameTeam> getTeams()
	{
		return _teamComponent.getTeams();
	}

	@Override
	public void joinTeam(Player player, GameTeam team)
	{
		_teamComponent.joinTeam(player, team);
	}

	@Override
	public GameTeam getTeam(Player player)
	{
		return _teamComponent.getTeam(player);
	}

	@Override
	public boolean isAlive(Player player)
	{
		return _teamComponent.isAlive(player);
	}

	@Override
	public void respawnPlayer(Player player, GameTeam team)
	{
		_teamComponent.respawnPlayer(player, team);
	}

	@Override
	public boolean hasRespawned(Player player)
	{
		return _teamComponent.hasRespawned(player);
	}

	@Override
	public List<Player> getAllPlayers()
	{
		return _teamComponent.getAllPlayers();
	}

	@Override
	public List<Player> getAlivePlayers()
	{
		return _teamComponent.getAlivePlayers();
	}

	/*
		Spectator
	 */

	@Override
	public void addSpectator(Player player, boolean teleport, boolean out)
	{
		_spectatorComponent.addSpectator(player, teleport, out);
	}

	@Override
	public Location getSpectatorLocation()
	{
		return _spectatorComponent.getSpectatorLocation();
	}

	/*
		Winners
	 */

	public void setWinningTeam(GameTeam winningTeam)
	{
		_winningTeam = winningTeam;
	}

	public GameTeam getWinningTeam()
	{
		return _winningTeam;
	}

	public GamePlacements getGamePlacements()
	{
		if (_placements == null)
		{
			_placements = createPlacements();
		}

		return _placements;
	}

	protected abstract GamePlacements createPlacements();

	/*
		Currency
	 */

	@Override
	public void addGems(Player player, int amount)
	{
		_manager.getCurrencyManager().addGems(player, amount);
	}

	/*
		Stats
	 */

	@Override
	public void addStat(Player player, String stat, int amount, boolean limitTo1, boolean global)
	{
		_statsComponent.addStat(player, stat, amount, limitTo1, global);
	}

	/*
		Component Getters
	 */

	public GameWorldComponent getWorldComponent()
	{
		return _worldComponent;
	}
}
