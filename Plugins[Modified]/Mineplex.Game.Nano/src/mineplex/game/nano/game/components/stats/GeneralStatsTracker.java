package mineplex.game.nano.game.components.stats;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilPlayer;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class GeneralStatsTracker extends StatTracker<Game>
{

	private long _gameStartTime;

	public GeneralStatsTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		_gameStartTime = System.currentTimeMillis();
	}

	@EventHandler
	public void killsAndDeaths(CombatDeathEvent event)
	{
		Player player = event.GetEvent().getEntity();

		addStat(player, "Deaths", 1, false, false);

		CombatComponent killerComponent = event.GetLog().GetKiller();

		if (killerComponent != null)
		{
			Player killer = UtilPlayer.searchExact(killerComponent.getUniqueIdOfEntity());

			if (killer != null)
			{
				addStat(killer, "Kills", 1, false, false);
			}
		}
	}

	@EventHandler
	public void winsAndPlayed(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.End)
		{
			return;
		}

		GamePlacements placements = _game.getGamePlacements();

		if (placements == null)
		{
			return;
		}

		List<Player> allPlayers = _game.getAllPlayers();
		List<Player> winners = placements.getWinners();

		// Time In Game
		int timeInGame = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - _gameStartTime);
		allPlayers.forEach(player -> addStat(player, "TimeInGame", timeInGame, true, true));

		if (winners != null)
		{
			// Wins & Losses
			winners.forEach(player -> addStat(player, "Wins", 1, true, false));

			if (_game.getTeams().size() == 1 && winners.size() > 1)
			{
				winners.forEach(player -> addStat(player, "ShareFirst", 1, true, false));
			}

			allPlayers.removeAll(winners);
			allPlayers.forEach(player -> addStat(player, "Losses", 1, true, false));
		}

		// Second & Third
		List<Player> second = placements.getPlayersAtPlace(1), third = placements.getPlayersAtPlace(2);

		if (second != null)
		{
			second.forEach(player -> addStat(player, "SecondPlace", 1, true, false));
		}

		if (third != null)
		{
			third.forEach(player -> addStat(player, "ThirdPlace", 1, true, false));
		}
	}
}
