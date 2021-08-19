package mineplex.core.scoreboard;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ScoreboardManager extends MiniPlugin
{
	private final Map<UUID, MineplexScoreboard> _scoreboards = new HashMap<>();

	private int _scoreboardTick = 0;

	public ScoreboardManager(JavaPlugin plugin)
	{
		super("Scoreboard Manager", plugin);
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent event)
	{
		MineplexScoreboard scoreboard = new MineplexScoreboard(event.getPlayer());

		this._scoreboards.put(event.getPlayer().getUniqueId(), scoreboard);

		setup(scoreboard);
		draw(scoreboard);

		event.getPlayer().setScoreboard(scoreboard.getHandle());

		handlePlayerJoin(event.getPlayer());
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event)
	{
		this._scoreboards.remove(event.getPlayer().getUniqueId());
		handlePlayerQuit(event.getPlayer());
	}

	public MineplexScoreboard get(Player player)
	{
		return _scoreboards.get(player.getUniqueId());
	}

	@EventHandler
	public void UpdateScoreboard(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		_scoreboardTick = (_scoreboardTick + 1) % 3;

		if (_scoreboardTick != 0)
			return;

		_scoreboards.values().forEach(this::draw);
	}

	public void handlePlayerJoin(Player player)
	{
		handlePlayerJoin(player.getName());
	}

	/*
	 * Used in DisguisePlayer!
	 */
	public abstract void handlePlayerJoin(String player);

	public void handlePlayerQuit(Player player)
	{
		handlePlayerQuit(player.getName());
	}

	public abstract void handlePlayerQuit(String player);

	public abstract void setup(MineplexScoreboard scoreboard);

	public abstract void draw(MineplexScoreboard scoreboard);

	public Map<UUID, MineplexScoreboard> getScoreboards()
	{
		return Collections.unmodifiableMap(this._scoreboards);
	}
}
