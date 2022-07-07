package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class PerfectionistTrack extends Track
{
	private final TrackManager _trackManager = Managers.require(TrackManager.class);

	public PerfectionistTrack()
	{
		super("perfectionist", "Perfectionist", "This track is unlocked by completing other title lines");
		getRequirements()
				.addTier(new TrackTier(
						"A Bold Journey",
						"Complete 2 title track",
						this::getCompletedTracks,
						2,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Endurer",
						"Complete 5 title tracks",
						this::getCompletedTracks,
						5,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"You've Probably Heard of Me",
						"Complete 10 title tracks",
						this::getCompletedTracks,
						10,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Doer of Things",
						"Complete 15 title tracks",
						this::getCompletedTracks,
						15,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));
	}

	private long getCompletedTracks(Player player)
	{
		int completed = 0;

		for (Track track : _trackManager.getAllTracks())
		{
			if (track == this)
				continue;
			if (track.isSpecial())
				continue;
			if (track.getRequirements().getNextTier(player) == null)
			{
				completed++;
			}
		}

		return completed;
	}
}
