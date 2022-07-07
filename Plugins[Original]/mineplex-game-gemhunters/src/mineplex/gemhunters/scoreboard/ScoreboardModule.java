package mineplex.gemhunters.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.gemhunters.economy.EconomyModule;

@ReflectivelyCreateMiniPlugin
public class ScoreboardModule extends MiniPlugin
{

	private static final String PRIMARY_COLOUR = C.cGreenB;
	private static final String SECONDARY_COLOUR = C.cWhiteB;
	private static final String TRANSITION_COLOUR = C.cDGreenB;
	private static final String SCOREBOARD_TITLE = " GEM HUNTERS ";

	private final EconomyModule _economy;

	private final Map<UUID, GemHuntersScoreboard> _scoreboards;

	private int _shineIndex;
	private boolean _shineDirection = true;

	public ScoreboardModule()
	{
		super("Scoreboard");

		_economy = require(EconomyModule.class);
		_scoreboards = new HashMap<>();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
		{
			for (UUID key : _scoreboards.keySet())
			{
				GemHuntersScoreboard scoreboard = _scoreboards.get(key);
				Player player = UtilPlayer.searchExact(key);

				scoreboard.writeContent(player);
				scoreboard.draw();
			}
		}
		else if (event.getType() == UpdateType.FASTEST)
		{
			updateTitles();
		}
		else if (event.getType() == UpdateType.SEC_08)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				int gems = _economy.getGems(player);

				for (GemHuntersScoreboard scoreboard : _scoreboards.values())
				{
					Objective objective = scoreboard.getHandle().getObjective(DisplaySlot.BELOW_NAME);
					Score score = objective.getScore(player.getName());

					if (score.getScore() == gems)
					{
						continue;
					}

					score.setScore(gems);
				}
			}
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		createPlayerScoreboard(event.getPlayer());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		for (GemHuntersScoreboard scoreboard : _scoreboards.values())
		{
			scoreboard.getHandle().getTeam(player.getName()).unregister();
		}

		_scoreboards.remove(player.getUniqueId());
	}

	@EventHandler
	public void updateScoreboard(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_20)
		{
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			createPlayerScoreboard(player);
		}
	}

	public void createPlayerScoreboard(Player player)
	{
		GemHuntersScoreboard scoreboard;

		if (_scoreboards.containsKey(player.getUniqueId()))
		{
			scoreboard = _scoreboards.get(player.getUniqueId());
		}
		else
		{
			scoreboard = new GemHuntersScoreboard(player);
			_scoreboards.put(player.getUniqueId(), scoreboard);

			// Gem Counter Undername
			Objective gemCounter = scoreboard.getHandle().registerNewObjective("Gems", "Gems");
			gemCounter.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}

		Scoreboard handle = scoreboard.getHandle();

		for (GemHuntersScoreboard other : _scoreboards.values())
		{
			// Set the other player's name tag for the player joining
			Player otherPlayer = other.getOwner();
			Team team = handle.getTeam(otherPlayer.getName());

			if (team == null)
			{
				team = handle.registerNewTeam(otherPlayer.getName());
			}

			team.setPrefix(scoreboard.getPrefix(player, otherPlayer));
			//team.setSuffix(scoreboard.getSuffix(player, otherPlayer));
			team.addEntry(otherPlayer.getName());

			if (player.equals(otherPlayer))
			{
				continue;
			}

			// Set the player that is joining
			Scoreboard otherHandle = other.getHandle();
			Team otherTeam = otherHandle.getTeam(player.getName());

			if (otherTeam == null)
			{
				otherTeam = otherHandle.registerNewTeam(player.getName());
			}

			otherTeam.setPrefix(other.getPrefix(other.getOwner(), player));
			//otherTeam.setSuffix(other.getSuffix(other.getOwner(), player));
			otherTeam.addEntry(player.getName());
		}

		player.setScoreboard(handle);
	}

	public void updateTitles()
	{
		String out = (_shineDirection ? PRIMARY_COLOUR : SECONDARY_COLOUR);

		for (int i = 0; i < SCOREBOARD_TITLE.length(); i++)
		{
			char c = SCOREBOARD_TITLE.charAt(i);

			if (_shineDirection)
			{
				if (i == _shineIndex)
				{
					out += TRANSITION_COLOUR;
				}
				else if (i == _shineIndex + 1)
				{
					out += SECONDARY_COLOUR;
				}
			}
			else
			{
				if (i == _shineIndex)
				{
					out += TRANSITION_COLOUR;
				}
				else if (i == _shineIndex + 1)
				{
					out += PRIMARY_COLOUR;
				}
			}

			out += c;
		}

		for (GemHuntersScoreboard scoreboard : _scoreboards.values())
		{
			scoreboard.setSidebarName(out);
		}

		_shineIndex++;

		if (_shineIndex == SCOREBOARD_TITLE.length() * 2)
		{
			_shineIndex = 0;
			_shineDirection = !_shineDirection;
		}
	}
}
