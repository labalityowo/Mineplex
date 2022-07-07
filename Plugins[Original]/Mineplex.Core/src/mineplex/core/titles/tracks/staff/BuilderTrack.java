package mineplex.core.titles.tracks.staff;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class BuilderTrack extends ItemizedTrack
{
	private final CoreClientManager _clientManager = Managers.get(CoreClientManager.class);

	public BuilderTrack()
	{
		super("staff-builder", ChatColor.BLUE, "Builder", "What's a Happer?", "What's a leader?", true);
		getRequirements()
				.addTier(new TrackTier(
						"What's a Happer?",
						null,
						this::owns,
						new TrackFormat(ChatColor.BLUE, ChatColor.BLUE)
				));
	}

	@Override
	public boolean owns(Player player)
	{
		return _clientManager.Get(player).hasPermission(TrackManager.Perm.BUILDER);
	}
}