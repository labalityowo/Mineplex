package mineplex.core.titles.tracks.custom;

import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import com.google.common.collect.Sets;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class DongerTrack extends Track
{
	private static final Set<String> OWNERS = Sets.newHashSet(
			"b86b54da-93dd-46f9-be33-27bd92aa36d7",
			"a20d59d1-cfd8-4116-ac27-45d9c7eb4a97"
	);

	public DongerTrack()
	{
		super("donger", ChatColor.AQUA, "Donger", "Donger", "ヽ༼ຈل͜ຈ༽ﾉ", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"ヽ༼ຈل͜ຈ༽ﾉ",
						null,
						player -> OWNERS.contains(player.getUniqueId().toString().toLowerCase()),
						new TrackFormat(ChatColor.AQUA, ChatColor.BLUE)
				));
	}
}