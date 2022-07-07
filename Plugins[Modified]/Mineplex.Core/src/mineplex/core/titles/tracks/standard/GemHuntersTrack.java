package mineplex.core.titles.tracks.standard;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import net.md_5.bungee.api.ChatColor;

public class GemHuntersTrack extends Track
{

	public GemHuntersTrack()
	{
		super("gem-hunters-gems", "GH Millionaire", "This track is unlocked by earning gems in Gem Hunters");

		getRequirements()
				.addTier(new TrackTier(
						"Beggar",
						"Gain 1,000 Gems in Gem Hunters",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Poor",
						"Gain 5,000 Gems in Gem Hunters",
						this::getStat,
						5000,
						new TrackFormat(ChatColor.AQUA)
				))
				.addTier(new TrackTier(
						"Middle Class",
						"Gain 7,500 Gems in Gem Hunters",
						this::getStat,
						7500,
						new TrackFormat(ChatColor.GREEN)
				))
				.addTier(new TrackTier(
						"Wealthy",
						"Gain 10,000 Gems in Gem Hunters",
						this::getStat,
						10000,
						new TrackFormat(ChatColor.DARK_GREEN)
				))
				.addTier(new TrackTier(
						"Loaded",
						"Gain 25,000 Gems in Gem Hunters",
						this::getStat,
						25000,
						new TrackFormat(ChatColor.GOLD)
				))
				.addTier(new TrackTier(
						"Millionaire",
						"Gain 50,000 Gems in Gem Hunters",
						this::getStat,
						50000,
						new TrackFormat(ChatColor.GOLD, ChatColor.YELLOW)
				));

		getRequirements()
				.withRequirement(1, "Gem Earned in Gem Hunters");
	}

	/**
	 * Overriding this means we can hit two birds with one stat.
	 */
	@Override
	public String getStatName()
	{
		return "Gem Hunters.GemsEarned";
	}
}
