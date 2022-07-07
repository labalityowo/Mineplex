package mineplex.core.titles.tracks.award;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.common.util.C;
import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class MapSub2018Track extends ItemizedTrack
{

	private static final String[] LINES =
			{
					C.cAquaB + "Master " + C.cWhiteB + "of" + C.cBlueB + " Maps",
					C.cBlueB + "Master " + C.cWhiteB + "of" + C.cAquaB + " Maps"
			};

	public MapSub2018Track()
	{
		super(
				"map-sub-2018",
				ChatColor.AQUA,
				"Master of Maps",
				"Master of Maps",
				"This track is awarded to players who scored highly in the 2018 Map Submission Giveaway.",
				true);

		getRequirements()
				.addTier(new TrackTier(
						"Master of Maps",
						null,
						this::owns,
						new TrackFormat(ChatColor.AQUA, ChatColor.WHITE)
								.animated(10, LINES)
				));
	}
}
