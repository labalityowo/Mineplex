package mineplex.core.titles.tracks.custom;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class LeaderTrack extends Track
{
	private final CoreClientManager _coreClientManager = Managers.require(CoreClientManager.class);

	public LeaderTrack()
	{
		super("leader", ChatColor.DARK_RED, "Leader", "What's a Leader?", "also wat does dev mean", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"What's a Leader?",
						null,
						player -> _coreClientManager.Get(player).hasPermission(TrackManager.Perm.LEADER),
						new TrackFormat(ChatColor.DARK_RED, ChatColor.RED)
				));
	}
}