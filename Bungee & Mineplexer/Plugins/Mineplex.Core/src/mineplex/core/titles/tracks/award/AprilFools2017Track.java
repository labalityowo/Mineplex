package mineplex.core.titles.tracks.award;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import net.md_5.bungee.api.ChatColor;

public class AprilFools2017Track extends ItemizedTrack
{

	public AprilFools2017Track()
	{
		super(
				"aprilfools-2017",
				ChatColor.LIGHT_PURPLE,
				"Fools 2017",
				"2017 April Fools Treasure Hunt",
				"This track is awarded to players who found all the chests in the 2017 April Fools Event",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						"2017 April Fools",
						null,
						this::owns,
						new TrackFormat(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE)
				));	}
}
