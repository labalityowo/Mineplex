package mineplex.core.titles.tracks.award;

import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import com.google.common.collect.Sets;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.titles.tracks.custom.ScrollAnimation;

public class CCIITrack extends Track
{

	private static final String TITLE = "❄ Christmas Tester ❄";

	private static String[] buildAnimation()
	{
		return new ScrollAnimation(TITLE)
				.withPrimaryColour(ChatColor.AQUA)
				.withSecondaryColour(ChatColor.WHITE)
				.withTertiaryColour(ChatColor.BLUE)
				.bold()
				.build();
	}

	private static final Set<String> OWNERS = Sets.newHashSet(
			"ca871a3f-349c-474c-9c45-c36f2e679ab3", // Moppletop
			"852a8acf-7337-40d7-99ec-b08fd99650b5", // KingCrazy_
			"eee121e6-2453-4801-8f44-d406e6f521f0", // DeanTM
			"e873e1c7-8e7d-4489-84e1-74b86e1b4ba7", // Dutty
			"c76f8ca3-21a2-4c0c-9217-4ded7ddd798f", // Intoxicating
			"b59ed7e5-9305-41c6-a0bd-3362ea643ac5" // SimplyBrandon1
	);

	public CCIITrack()
	{
		super(
				"cc2-tester",
				ChatColor.GREEN,
				"CC2 Tester",
				"Christmas Tester",
				"This track is awarded to the players who helped with Christmas Chaos II :)",
				true);

		special();
		getRequirements()
				.addTier(new TrackTier(
						TITLE,
						null,
						player -> OWNERS.contains(player.getUniqueId().toString()),
						new TrackFormat(ChatColor.AQUA)
								.animated(2, buildAnimation())
				));
	}
}
