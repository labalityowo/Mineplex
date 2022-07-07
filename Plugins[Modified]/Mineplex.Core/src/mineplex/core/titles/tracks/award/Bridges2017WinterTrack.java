package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.titles.tracks.custom.ScrollAnimation;

public class Bridges2017WinterTrack extends ItemizedTrack
{

	private static final String TITLE = "❄ 2017 Haven Champion ❄";
	private static String[] buildAnimation()
	{
		return new ScrollAnimation(TITLE)
				.withPrimaryColour(ChatColor.AQUA)
				.withSecondaryColour(ChatColor.WHITE)
				.withTertiaryColour(ChatColor.BLUE)
				.bold()
				.build();
	}

	public Bridges2017WinterTrack()
	{
		super(
				"tournament-br-2017-winter",
				ChatColor.AQUA,
				"BotB 2017 Winter",
				"2017 Haven Champion",
				"This track is awarded to the winners of the 2017 BotB Winter Haven Tournament",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						TITLE,
						null,
						this::owns,
						new TrackFormat(ChatColor.AQUA)
							.animated(2, buildAnimation())
				));
	}

}
