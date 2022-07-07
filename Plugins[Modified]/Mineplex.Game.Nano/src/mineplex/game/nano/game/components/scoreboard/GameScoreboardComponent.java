package mineplex.game.nano.game.components.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.TriConsumer;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class GameScoreboardComponent extends GameComponent<Game>
{

	private final Map<Player, NanoScoreboard> _scoreboard;

	private BiConsumer<Player, NanoScoreboard> _scoreboardConsumer;
	private BiFunction<Player, GameTeam, String> _prefixFunction;
	private BiFunction<Player, GameTeam, String> _suffixFunction;
	private TriConsumer<Player, GameTeam, Team> _setupSettingsConsumer;
	private BiFunction<Player, Player, Integer> _tabListFunction;
	private String _underNameObjective;
	private BiFunction<Player, Player, Integer> _underNameFunction;

	public GameScoreboardComponent(Game game)
	{
		super(game);

		_scoreboard = new HashMap<>();
		setPrefix((viewer, team) -> team.getChatColour().toString());
	}

	@Override
	public void disable()
	{
		_scoreboard.clear();
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		setupScoreboard(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			setupScoreboard(player, false);
		}

		for (Player player : getGame().getManager().getSpectators())
		{
			_scoreboard.values().forEach(scoreboard ->
			{
				scoreboard.setPlayerTeam(player, null);
				scoreboard.refreshAsSubject(player);
			});
		}
	}

	private void setupScoreboard(Player player, boolean forceUpdate)
	{
		NanoScoreboard scoreboard = _scoreboard.computeIfAbsent(player, k -> new NanoScoreboard(this, player));
		player.setScoreboard(scoreboard.getHandle());

		if (forceUpdate)
		{
			for (Player other : UtilServer.getPlayersCollection())
			{
				scoreboard.setPlayerTeam(other, getGame().getTeam(other));
				scoreboard.refreshAsSubject(other);
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		String entry = player.getName();

		_scoreboard.remove(player);
		_scoreboard.values().forEach(scoreboard ->
		{
			Team team = scoreboard.getHandle().getEntryTeam(entry);

			if (team != null)
			{
				team.removeEntry(entry);
			}
		});
	}

	@EventHandler
	public void dead(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Dead)
		{
			return;
		}

		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.setScoreboard(main);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateSidebar(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _scoreboardConsumer == null)
		{
			return;
		}

		_scoreboard.forEach((player, scoreboard) -> _scoreboardConsumer.accept(player, scoreboard));
	}

	@EventHandler
	public void updateTitle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		_scoreboard.values().forEach(NanoScoreboard::updateTitle);
	}

	@EventHandler
	public void playerStateChange(PlayerStateChangeEvent event)
	{
		setPlayerTeam(event.getPlayer(), event.getTeam());
	}

	private void setPlayerTeam(Player subject, GameTeam team)
	{
		_scoreboard.values().forEach(scoreboard -> scoreboard.setPlayerTeam(subject, team));
	}

	public void refreshAsSubject(Player subject)
	{
		_scoreboard.values().forEach(scoreboard -> scoreboard.refreshAsSubject(subject));
	}

	public GameScoreboardComponent setSidebar(BiConsumer<Player, NanoScoreboard> consumer)
	{
		_scoreboardConsumer = consumer;
		return this;
	}

	public GameScoreboardComponent setPrefix(BiFunction<Player, GameTeam, String> function)
	{
		_prefixFunction = function;
		return this;
	}

	public GameScoreboardComponent setSuffix(BiFunction<Player, GameTeam, String> function)
	{
		_suffixFunction = function;
		return this;
	}

	public GameScoreboardComponent setTabList(BiFunction<Player, Player, Integer> function)
	{
		_tabListFunction = function;
		return this;
	}

	public GameScoreboardComponent setUnderNameObjective(String name)
	{
		_underNameObjective = name;
		return this;
	}

	public GameScoreboardComponent setUnderName(BiFunction<Player, Player, Integer> function)
	{
		_underNameFunction = function;
		return this;
	}

	public GameScoreboardComponent setSetupSettingsConsumer(TriConsumer<Player, GameTeam, Team> setupSettingsConsumer)
	{
		_setupSettingsConsumer = setupSettingsConsumer;
		return this;
	}

	public String getPrefix(Player viewer, GameTeam gameTeam)
	{
		return _prefixFunction == null ? C.Reset : _prefixFunction.apply(viewer, gameTeam);
	}

	public String getSuffix(Player viewer, GameTeam gameTeam)
	{
		return _suffixFunction == null ? C.Reset : _suffixFunction.apply(viewer, gameTeam);
	}

	public Integer getTabList(Player viewer, Player subject)
	{
		return _tabListFunction == null ? null : _tabListFunction.apply(viewer, subject);
	}

	public String getUnderNameObjective()
	{
		return _underNameObjective;
	}

	public Integer getUnderName(Player viewer, Player subject)
	{
		return _underNameFunction == null ? null : _underNameFunction.apply(viewer, subject);
	}

	public TriConsumer<Player, GameTeam, Team> getSetupSettingsConsumer()
	{
		return _setupSettingsConsumer;
	}
}
