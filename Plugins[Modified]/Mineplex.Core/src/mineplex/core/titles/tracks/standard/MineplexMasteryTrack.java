package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.game.GameDisplay;
import mineplex.core.stats.StatsManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MineplexMasteryTrack extends Track
{
	private final StatsManager _statsManager;

	public MineplexMasteryTrack()
	{
		super("mineplex-mastery", "Mineplex Mastery", "This track is unlocked by winning different games on Mineplex");
		getRequirements()
				.addTier(new TrackTier(
						"Mineplex Initiate",
						"Win at least one game in 10 different Mineplex Games",
						this::getGames,
						10,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Mineplex Veteran",
						"Win at least one game in 20 different Mineplex Games",
						this::getGames,
						20,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Mineplex Master",
						"Win at least one game in 30 different Mineplex Games",
						this::getGames,
						30,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));
		_statsManager = Managers.require(StatsManager.class);
	}

	private long getGames(Player player)
	{
		int count = 0;
		for (GameDisplay display : GameDisplay.values())
		{
			if (_statsManager.Get(player).getStat(display.getName() + ".TrackWins") > 0)
			{
				count++;
			}
		}

		return count;
	}
}
