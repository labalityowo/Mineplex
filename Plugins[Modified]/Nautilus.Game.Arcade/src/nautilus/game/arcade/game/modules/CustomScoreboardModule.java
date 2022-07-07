package nautilus.game.arcade.game.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.scoreboard.GameScoreboard;

/**
 * CustomScoreboardModule allows for the use of per-player scoreboards in an Arcade game.
 * Including sidebars, tab-list prefixes, suffixes, undernames and scores.<br>
 * These scoreboard system is backed by {@link mineplex.core.scoreboard.ScoreboardManager} and is thus optimised
 * extremely efficiently, so don't be afraid to call the <i>set</i> methods often as they will not be updated then and there.<br>
 * Scoreboards are refreshed for all players upon the prepare, live and end state changes. While sidebars are updated every 10 ticks/0.5 seconds.<br>
 * If you wish to update the scoreboard more frequently than these, the methods:
 * <ul>
 * <li>{@link #refresh()}</li>
 * <li>{@link #refreshAsPerspective(Player)}</li>
 * <li{@link #refreshAsSubject(Player)}</li>
 * </ul>
 *
 * @see mineplex.core.scoreboard.MineplexScoreboard
 * @see Moba
 */
public class CustomScoreboardModule extends Module
{

	private final Map<UUID, CustomArcadeScoreboard> _scoreboard;

	private BiConsumer<Player, GameScoreboard> _scoreboardConsumer;
	private BiFunction<Player, Player, String> _prefixFunction;
	private BiFunction<Player, Player, String> _suffixFunction;
	private BiFunction<Player, Player, NameTagVisibility> _nameTagVisibilityFunction;
	private BiFunction<Player, Player, Integer> _tabListFunction;
	private String _underNameObjective;
	private BiFunction<Player, Player, Integer> _underNameFunction;

	public CustomScoreboardModule()
	{
		_scoreboard = new HashMap<>();
	}

	/**
	 * The use of the boolean UseCustomScoreboard in {@link nautilus.game.arcade.game.Game} is required so the Arcade doesn't
	 * try and create the scoreboard itself or reference the standard {@link GameScoreboard}, which it does (a lot).
	 */
	@Override
	protected void setup()
	{
		getGame().UseCustomScoreboard = true;
	}

	/**
	 * Calls {@link #setupScoreboard(Player)} when the players joins, if the game is not in a Lobby state.
	 */
	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (getGame().inLobby())
		{
			return;
		}

		setupScoreboard(player);
		refreshAsPerspective(player);
		refreshAsSubject(player);
	}

	/**
	 * Removes the player's scoreboard from the {@link #_scoreboard} map.
	 * <br>
	 * Unregisters the quitting player's entries from all other scoreboards.
	 */
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_scoreboard.remove(player.getUniqueId());

		for (CustomArcadeScoreboard scoreboard : _scoreboard.values())
		{
			Team team = scoreboard.getHandle().getTeam(player.getName());

			if (team != null)
			{
				team.unregister();
			}
		}
	}

	/**
	 * Calls {@link #setupScoreboard(Player)} when the game switches to the prepare state.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			setupScoreboard(player);
		}
	}

	/**
	 * Refreshes all player scoreboards upon an in progress state change.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare && event.GetState() != GameState.Live && event.GetState() != GameState.End)
		{
			return;
		}

		refresh();
	}

	/**
	 * Calls the draw method every 10 ticks/0.5 seconds.
	 */
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (CustomArcadeScoreboard scoreboard : _scoreboard.values())
		{
			scoreboard.draw();
		}
	}

	/**
	 * Updates the title animation of the scoreboard
	 */
	@EventHandler
	public void updateTitle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for (CustomArcadeScoreboard scoreboard : _scoreboard.values())
		{
			scoreboard.updateTitle();
		}
	}

	private void setupScoreboard(Player player)
	{
		UUID key = player.getUniqueId();
		CustomArcadeScoreboard scoreboard = _scoreboard.get(key);

		if (scoreboard == null)
		{
			scoreboard = new CustomArcadeScoreboard(player);
			_scoreboard.put(player.getUniqueId(), scoreboard);
		}

		player.setScoreboard(scoreboard.getHandle());
	}

	public CustomScoreboardModule setSidebar(BiConsumer<Player, GameScoreboard> consumer)
	{
		_scoreboardConsumer = consumer;
		return this;
	}

	public CustomScoreboardModule setPrefix(BiFunction<Player, Player, String> function)
	{
		_prefixFunction = function;
		return this;
	}

	public CustomScoreboardModule setSuffix(BiFunction<Player, Player, String> function)
	{
		_suffixFunction = function;
		return this;
	}

	public CustomScoreboardModule setTabList(BiFunction<Player, Player, Integer> function)
	{
		_tabListFunction = function;
		return this;
	}

	public CustomScoreboardModule setUnderNameObjective(String name)
	{
		_underNameObjective = name;
		return this;
	}

	public CustomScoreboardModule setUnderName(BiFunction<Player, Player, Integer> function)
	{
		_underNameFunction = function;
		return this;
	}

	public CustomScoreboardModule setNameTagVisibility(BiFunction<Player, Player, NameTagVisibility> function)
	{
		_nameTagVisibilityFunction = function;
		return this;
	}

	/**
	 * Refreshes all player's scoreboards.
	 */
	public void refresh()
	{
		for (CustomArcadeScoreboard scoreboard : _scoreboard.values())
		{
			scoreboard.draw(false);
		}
	}

	/**
	 * Refreshes an individual player's scoreboard.
	 *
	 * @param perspective The player that has the perspective of the scoreboard to be refreshed.
	 */
	public void refreshAsPerspective(Player perspective)
	{
		CustomArcadeScoreboard scoreboard = _scoreboard.get(perspective.getUniqueId());

		if (scoreboard == null)
		{
			return;
		}

		scoreboard.draw(false);
	}

	/**
	 * Refreshes all scoreboards but only where the subject of said scoreboard is the player passed in as the subject parameter.
	 */
	public void refreshAsSubject(Player subject)
	{
		for (CustomArcadeScoreboard scoreboard : _scoreboard.values())
		{
			scoreboard.draw(subject);
		}
	}

	@Override
	public void cleanup()
	{
		_scoreboard.clear();
	}

	/**
	 * An internal class that simply implements the actions set out by {@link CustomScoreboardModule}.
	 */
	class CustomArcadeScoreboard extends GameScoreboard
	{

		CustomArcadeScoreboard(Player owner)
		{
			super(getGame(), owner);
		}

		private void updateTag(Player subject)
		{
			Scoreboard handle = getHandle();
			String teamName = subject.getName();
			Team team = handle.getTeam(teamName);

			String prefix = _prefixFunction == null ? null : _prefixFunction.apply(getOwner(), subject);
			String suffix = _suffixFunction == null ? null : _suffixFunction.apply(getOwner(), subject);
			NameTagVisibility visibility = _nameTagVisibilityFunction == null ? null : _nameTagVisibilityFunction.apply(getOwner(), subject);

			if (team == null)
			{
				team = handle.registerNewTeam(teamName);
				team.addEntry(subject.getName());
			}

			if (prefix != null)
			{
				if (team.getPrefix() == null || !team.getPrefix().equals(prefix))
				{
					team.setPrefix(prefix);
				}
			}
			if (suffix != null)
			{
				if (team.getSuffix() == null || !team.getSuffix().equals(suffix))
				{
					team.setSuffix(suffix);
				}
			}
			if (visibility != null)
			{
				if (!visibility.equals(team.getNameTagVisibility()))
				{
					team.setNameTagVisibility(visibility);
				}
			}
		}

		private void updateTabList(Player subject)
		{
			if (_tabListFunction == null)
			{
				return;
			}

			Scoreboard handle = getHandle();
			Objective objective = handle.getObjective(DisplaySlot.PLAYER_LIST);
			int value = _tabListFunction.apply(getOwner(), subject);

			if (objective == null)
			{
				objective = handle.registerNewObjective("TabList", "dummy");
				objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
			}

			Score score = objective.getScore(subject.getName());

			if (score.getScore() != value)
			{
				score.setScore(value);
			}
		}

		private void updateUnderName(Player subject)
		{
			if (_underNameFunction == null || _underNameObjective == null)
			{
				return;
			}

			Scoreboard handle = getHandle();
			Objective objective = handle.getObjective(DisplaySlot.BELOW_NAME);
			int value = _underNameFunction.apply(getOwner(), subject);

			if (objective == null)
			{
				objective = handle.registerNewObjective(_underNameObjective, "dummy");
				objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
			}

			Score score = objective.getScore(subject.getName());

			if (score.getScore() != value)
			{
				score.setScore(value);
			}
		}

		@Override
		public void draw()
		{
			draw(true);
		}

		public void draw(boolean sidebarOnly)
		{
			if (_scoreboardConsumer != null)
			{
				_scoreboardConsumer.accept(getOwner(), this);
			}
			if (!sidebarOnly)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					draw(player);
				}
			}
			super.draw();
		}

		private void draw(Player player)
		{
			updateTag(player);
			updateTabList(player);
			updateUnderName(player);
		}
	}


}
