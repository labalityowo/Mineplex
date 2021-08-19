package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class NewHub2018Track extends ItemizedTrack
{

	public NewHub2018Track()
	{
		super(
				"new-hub-2018",
				ChatColor.GREEN,
				"Hub Explorer",
				"2018 Hub Explorer",
				"This track is awarded to players who found all the treasures in the new 2018 Hub.",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						"Hub Explorer",
						null,
						this::owns,
						new TrackFormat(ChatColor.AQUA, ChatColor.GREEN)
				));	}
}
