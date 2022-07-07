package mineplex.core.titles.tracks.staff;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.ItemizedTrack;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class SeniorModeratorTrack extends ItemizedTrack
{
	private final CoreClientManager _clientManager = Managers.get(CoreClientManager.class);

	public SeniorModeratorTrack()
	{
		super("staff-srmod", ChatColor.GOLD, "Sr.Mod", "My Team's the Best Team", "Team loyalty at its finest", true);
		getRequirements()
				.addTier(new TrackTier(
						"My Team's the Best Team",
						null,
						this::owns,
						new TrackFormat(ChatColor.GOLD, ChatColor.GOLD)
				));
	}

	@Override
	public boolean owns(Player player)
	{
		return _clientManager.Get(player).hasPermission(TrackManager.Perm.SR_MOD);
	}
}