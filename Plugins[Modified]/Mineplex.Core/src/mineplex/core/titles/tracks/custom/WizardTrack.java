package mineplex.core.titles.tracks.custom;

import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import com.google.common.collect.Sets;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

// hmu t3
public class WizardTrack extends Track
{
	private static final Set<String> OWNERS = Sets.newHashSet(
			"b86b54da-93dd-46f9-be33-27bd92aa36d7",
			"2016b565-0a63-4a2d-800b-b786ac256288"
	);

	public WizardTrack()
	{
		super("wizard", ChatColor.DARK_PURPLE, "Wizard", "Wizard", "(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧",
						null,
						player -> OWNERS.contains(player.getUniqueId().toString().toLowerCase()),
						new TrackFormat(ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE)
								.animated(5,
										"(ﾉ◕ヮ◕)ﾉ*",
										"(ﾉ◕ヮ◕)ﾉ*:・ﾟ",
										"(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧",
										"(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧",
										"(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧"
								)
				));
	}
}