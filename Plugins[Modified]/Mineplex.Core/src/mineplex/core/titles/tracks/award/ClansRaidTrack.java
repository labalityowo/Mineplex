package mineplex.core.titles.tracks.award;

import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;
import net.md_5.bungee.api.ChatColor;

public class ClansRaidTrack extends ItemizedTrack
{
	public ClansRaidTrack()
	{
		super(
				"clans-raid",
				ChatColor.GOLD,
				"Fallen Lord",
				"The Fallen Lord",
				"Among the first players to defeat the Charles Witherton raid!",
				true);

		getRequirements()
				.addTier(new TrackTier(
						"The Fallen Lord",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.GOLD)
				));
	}
}
