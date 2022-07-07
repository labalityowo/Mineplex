package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.Managers;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class PowerPlayTrack extends Track
{

	public PowerPlayTrack()
	{
		super("power-play", ChatColor.AQUA, "Power Play", "Power Play VIP", "This track is unlocked by subscribing to the Power Play Club");
		getRequirements()
				.addTier(new TrackTier(
						"Power Play Club",
						null,
						// We have to get BonusManager here to prevent a big problem. Replace with a global variable if it can be created reflectively.
						player -> Managers.get(BonusManager.class).getPowerPlayClubRepository().getCachedData(player).isSubscribed() ? 1L : 0L,
						1,
						new TrackFormat(ChatColor.AQUA, ChatColor.AQUA)
				));
	}
}
