package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.common.util.C;
import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class CCIIPublicTrack extends ItemizedTrack
{

	public CCIIPublicTrack()
	{
		super(
				"christmas-chaos",
				ChatColor.RED,
				"Santa's Helper",
				"Santa's Helper",
				"This track is awarded to players who won the Christmas Chaos II.",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						C.cWhiteB + "Santa's Helper",
						null,
						this::owns,
						new TrackFormat(ChatColor.WHITE, ChatColor.RED)
				));
	}
}
