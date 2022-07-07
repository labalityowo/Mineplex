package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.gadget.set.SetEmerald;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class GemCollectorTrack extends Track
{
	public GemCollectorTrack()
	{
		super("gem-collector", "Gem Collector", "This track is unlocked by earning gems in games");
		getRequirements()
				.addTier(new TrackTier(
						"Gem Beggar",
						"Gain 1,000 Gem Points",
						this::getStat,
						1000,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Respectable Gem Miner",
						"Gain 25,000 Gem Points",
						this::getStat,
						25000,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Middle Class",
						"Gain 50,000 Gem Points",
						this::getStat,
						50000,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Gems, Gems, Gems",
						"Gain 75,000 Gem Points",
						this::getStat,
						75000,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Gem McScrooge",
						"Gain 100,000 Gem Points",
						this::getStat,
						100000,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));

		getRequirements()
				.withRequirement(1, "Gem Earned in games")
				.withSetBonus(SetEmerald.class, 2);
	}

	/**
	 * Call this method when the specified Player has earned gems
	 */
	public void earnedGems(Player player, int gems)
	{
		if (gems <= 0) return;

		if (isSetActive(player, SetEmerald.class))
			gems *= 2;

		incrementFor(player, gems);
	}
}
