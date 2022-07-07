package mineplex.core.titles.tracks.award;

import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import com.google.common.collect.Sets;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import mineplex.core.titles.tracks.custom.ScrollAnimation;

public class CastleSiegeTesterTrack extends Track
{

	private static final String TITLE = "Castle Sieger";
	private static String[] buildAnimation()
	{
		return new ScrollAnimation(TITLE)
				.withPrimaryColour(ChatColor.AQUA)
				.withSecondaryColour(ChatColor.RED)
				.withTertiaryColour(ChatColor.WHITE)
				.bold()
				.build();
	}

	private static final Set<String> OWNERS = Sets.newHashSet(
			"ca871a3f-349c-474c-9c45-c36f2e679ab3", // Moppletop
			"852a8acf-7337-40d7-99ec-b08fd99650b5", // KingCrazy_
			"a20d59d1-cfd8-4116-ac27-45d9c7eb4a97", // Artix
			"3d4b8e73-bc2f-4c62-baaf-78600141794a", // hazeae24
			"7b133339-2e02-48ef-9dd4-692415933dc5", // Kreshyy
			"4e941b23-6e36-48cb-97c4-24f56ea128c1", // DooDooBug25
			"0a4b6d83-8eb0-46aa-bc0f-9b7f04046d52", // Livicus
			"4f8f5380-ffe1-418d-97a4-0737c6edf352", // Hils
			"e873e1c7-8e7d-4489-84e1-74b86e1b4ba7" // Dutty
	);

	public CastleSiegeTesterTrack()
	{
		super(
				"cs-tester",
				ChatColor.RED,
				"CS Tester",
				"Castle Siege Tester",
				"This track is awarded to the players who helped test the Castle Siege update. :)",
				true);

		special();
		getRequirements()
				.addTier(new TrackTier(
						TITLE,
						null,
						player -> OWNERS.contains(player.getUniqueId().toString()),
						new TrackFormat(ChatColor.RED, ChatColor.RED)
								.animated(2, buildAnimation())
				));
	}
}
