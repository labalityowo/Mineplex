package mineplex.core.titles.tracks.custom;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.TrackTier;

public class HappyGaryTrack extends Track
{
	private final CoreClientManager _coreClientManager = Managers.require(CoreClientManager.class);

	public HappyGaryTrack()
	{
		super("happygary", ChatColor.GOLD, "Happy Gary", "Happy Gary", "ᐛ", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"☆ﾟ°˖* ᕕ(ᐛ)ᕗ",
						null,
						player -> _coreClientManager.Get(player).hasPermission(TrackManager.Perm.HAPPY_GARY),
						new TrackFormat(ChatColor.GOLD, ChatColor.GOLD)
								.animated(1,
										"☆ﾟ°˖* ᕕ(ᐛ)ᕗ",
										"*☆ﾟ°˖ ᕕ(ᐛ)ᕗ",
										"˖*☆ﾟ° ᕕ(ᐛ)ᕗ",
										"°˖*☆ﾟ ᕕ(ᐛ)ᕗ",
										"ﾟ°˖*☆ ᕕ(ᐛ)ᕗ"
								)
				));
	}
}