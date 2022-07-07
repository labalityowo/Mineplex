package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.titles.tracks.ItemizedTrack;

public class Bridges2017Track extends ItemizedTrack
{
	public Bridges2017Track()
	{
		super(
				"tournament-br-2017",
				ChatColor.GOLD,
				"BotB 2017",
				"2017 Bridges Champion",
				"This track is awarded to the winners of the 2017 BotB Tournament",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						"2017 Bridges Champion",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.GOLD)
				));
	}
}
