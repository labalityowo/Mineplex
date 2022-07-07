package mineplex.core.titles.tracks.custom;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class TableFlipTrack extends Track
{
	private final CoreClientManager _coreClientManager = Managers.require(CoreClientManager.class);

	public TableFlipTrack()
	{
		super("tableflipanim", ChatColor.AQUA, "Tableflip", "Tableflip", "(╯°□°)╯  ︵  ┻━┻", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"(╯°□°)╯  ︵  ┻━┻",
						null,
						player -> _coreClientManager.Get(player).hasPermission(TrackManager.Perm.TABLE_FLIP),
						new TrackFormat(ChatColor.AQUA, ChatColor.AQUA)
								.animated(5,
										"(\\°-°)\\ ┬┬",
										"(\\°-°)\\ ┬┬",
										"(\\°-°)\\ ┬┬",
										"(\\°□°)\\  ┬┬",
										"(-°□°)-  ┬┬",
										"(╯°□°)╯     ︵  ]",
										"(╯°□°)╯      ︵  ┻━┻",
										"(╯°□°)╯         ︵  [",
										"(╯°□°)╯            ︵  ┬┬",
										"(╯°□°)╯            ︵  ┬┬",
										"(╯°□°)╯            ︵  ┬┬",
										"(╯°□°)╯            ︵  ┬┬"
										)
				));
	}
}