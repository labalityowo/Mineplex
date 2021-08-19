package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class Minestrike2017Track extends ItemizedTrack
{
	public Minestrike2017Track()
	{
		super(
				"tournament-ms-2017",
				ChatColor.GOLD,
				"MS 2017",
				"2017 Minestrike Champion",
				"This track is awarded to the winners of the 2017 Minestrike Tournament",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						"2017 Minestrike Champion",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.GOLD)
				));
	}
}
