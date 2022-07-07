package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.NautHashMap;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GemData;

public class ExperienceStatTracker extends StatTracker<Game>
{

	public static final boolean DOUBLE_EXP = false;

	private long _startTime = -1;
	private final Map<String, Long> _playerQuitTime = new HashMap<>();

	public ExperienceStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void onGameStart(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			_startTime = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		_playerQuitTime.put(event.getPlayer().getName(), System.currentTimeMillis());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onGameEnd(GameStateChangeEvent event)
	{
		if (!event.GetGame().Manager.IsRewardStats() || !event.GetGame().Manager.IsRewardAchievements() || event.GetState() != GameState.End || _startTime <= 0)
		{
			return;
		}

		List<Player> winners = getGame().getWinners();

		if (winners != null)
		{
			for (Player player : event.GetGame().GetPlayers(false))
			{
				//Tally Gems
				double gemExp = 0;
				for (String reason : event.GetGame().GetGems(player).keySet())
				{
					if (reason.toLowerCase().contains("participation"))
						continue;

					GemData gem = event.GetGame().GetGems(player).get(reason);

					gemExp += (int) gem.Gems;
				}
				gemExp = Math.min(gemExp, 250) * 6;

				//Game Time = 1 Exp per 3 Seconds
				long time = System.currentTimeMillis();
				long playerJoin = getGame().getPlayerIngameTime(player);

				if (playerJoin == 0)
					playerJoin = _startTime;

				//Exp Until They Quit
				if (!player.isOnline())
				{
					time = _playerQuitTime.getOrDefault(player.getName(), _startTime);
				}

				double timeExp = (time - playerJoin) / 1500d;

				//Mult
				double mult = 1;

				if (winners.contains(player))
				{
					mult = 1.5;
				}

				if (DOUBLE_EXP)
				{
					mult *= 2;
				}

				//Exp
				int expGained = (int) (((timeExp + gemExp) * mult) * getGame().XpMult);
				// Kit Exp
				event.GetGame().getArcadeManager().getMineplexGameManager().getKitStatLog().get(player).setExperienceEarned(expGained);
				//Record Global and per Game
				addStat(player, "ExpEarned", expGained, false, true);
				addStat(player, "ExpEarned", expGained, false, false);
			}
		}

		_startTime = -1;
	}
}
