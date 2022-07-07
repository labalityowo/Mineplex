package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class SurvivorTrack extends Track
{
	public SurvivorTrack()
	{
		super("survivor", "Survivor", "This track is unlocked by playing consecutive games without dying");
		getRequirements()
				.addTier(new TrackTier(
						"Survivor",
						"Survive 5 consecutive games without dying",
						this::getStat,
						5,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Endurer",
						"Survive 10 consecutive games without dying",
						this::getStat,
						10,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Unkillable",
						"Survive 20 consecutive games without dying",
						this::getStat,
						20,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));
	}
}
