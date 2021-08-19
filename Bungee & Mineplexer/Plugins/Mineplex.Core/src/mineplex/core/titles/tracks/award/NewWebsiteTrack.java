package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class NewWebsiteTrack extends ItemizedTrack
{

	public NewWebsiteTrack()
	{
		super(
				"new-website",
				ChatColor.GOLD,
				"Verified",
				"Verified Player",
				"This track is awarded to players who have linked their Mineplex forum account in-game.",
				true);

		getRequirements()
				.addTier(new TrackTier(
						"Verified " + ChatColor.WHITE + ChatColor.BOLD + "✓",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.YELLOW)
				));
	}
}
