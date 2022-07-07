package mineplex.core.titles.tracks.award;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import net.md_5.bungee.api.ChatColor;

public class AlienInvasionTrack extends ItemizedTrack
{

	public AlienInvasionTrack()
	{
		super(
				"alien-invasion",
				ChatColor.GREEN,
				"Alien",
				"Alien Invasion",
				"This track is awarded to players who survived the Alien Invasion Event.",
				true);

		special();

		getRequirements()
				.addTier(new TrackTier(
						"Alien Invasion",
						null,
						this::owns,
						new TrackFormat(ChatColor.GREEN, ChatColor.DARK_GREEN)
				));	}
}
