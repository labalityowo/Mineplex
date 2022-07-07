package mineplex.core.titles.tracks.custom;

import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import com.google.common.collect.Sets;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class SnekTrack extends Track
{
	private static final Set<String> OWNERS = Sets.newHashSet(
			"b86b54da-93dd-46f9-be33-27bd92aa36d7"
	);

	public SnekTrack()
	{
		super("snek", ChatColor.DARK_GREEN, "Snek", "Snek", "oh you have do me a frighten", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"Snek",
						"hiss",
						player -> OWNERS.contains(player.getUniqueId().toString().toLowerCase()),
						new TrackFormat(ChatColor.DARK_GREEN, ChatColor.GREEN)
				));
	}
}