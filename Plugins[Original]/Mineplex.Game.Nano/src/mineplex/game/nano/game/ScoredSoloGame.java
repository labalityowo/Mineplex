package mineplex.game.nano.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public abstract class ScoredSoloGame extends SoloGame
{

	private final Map<Player, Integer> _scores;
	private List<Entry<Player, Integer>> _sortedPlaces;

	public ScoredSoloGame(NanoManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, description);

		_scores = new HashMap<>();

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Players");

			if (getState() == GameState.Prepare || _sortedPlaces == null)
			{
				scoreboard.write(getAllPlayers().size() + " Players");
			}
			else
			{
				scoreboard.writeNewLine();

				List<Entry<Player, Integer>> sorted = _sortedPlaces;
				boolean hasShownPlayer = false;

				for (int i = 0; i < Math.min(sorted.size(), hasShownPlayer ? 11 : 9); i++)
				{
					Entry<Player, Integer> entry = sorted.get(i);
					Player other = entry.getKey();

					if (player.equals(other))
					{
						hasShownPlayer = true;
					}

					scoreboard.write(entry.getValue() + " " + (player.equals(other) ? C.cGreen : (UtilPlayer.isSpectator(other) ? C.cGray + C.Strike : C.cYellow)) + other.getName());
				}

				if (!hasShownPlayer)
				{
					Entry<Player, Integer> entry = null;

					for (Entry<Player, Integer> other : sorted)
					{
						if (player.equals(other.getKey()))
						{
							entry = other;
							break;
						}
					}

					if (entry != null)
					{
						scoreboard.writeNewLine();

						scoreboard.write(entry.getValue() + " " + C.cGreen + player.getName());
					}
				}
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	public void disable()
	{
		_scores.clear();
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		_scores.putIfAbsent(event.getPlayer(), 0);
	}

	@EventHandler
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			_scores.remove(event.getPlayer());
		}
	}

	public void incrementScore(Player player, int score)
	{
		if (player == null)
		{
			return;
		}

		_scores.put(player, Math.max(0, _scores.getOrDefault(player, 0) + score));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void updateScores(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive())
		{
			return;
		}

		_sortedPlaces = _scores.entrySet().stream()
				.sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
				.collect(Collectors.toList());
	}

	public Map<Player, Integer> getScores()
	{
		return _scores;
	}

	@Override
	protected GamePlacements createPlacements()
	{
		return GamePlacements.fromPlayerScore(_scores);
	}
}
