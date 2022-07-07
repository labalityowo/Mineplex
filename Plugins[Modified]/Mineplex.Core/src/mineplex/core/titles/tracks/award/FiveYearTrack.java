package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class FiveYearTrack extends ItemizedTrack
{

	public FiveYearTrack()
	{
		super(
				"five-years",
				ChatColor.GOLD,
				"Five Year",
				"Mineplex 5th Year Anniversary",
				"This track is awarded to players who participated in the Mineplex 5th Year Anniversary Event.",
				true
		);

		getRequirements()
				.addTier(new TrackTier(
						"5 Year Anniversary",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.BLACK)
				));
	}
}
