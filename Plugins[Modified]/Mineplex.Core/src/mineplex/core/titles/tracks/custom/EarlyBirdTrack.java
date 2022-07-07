package mineplex.core.titles.tracks.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Sets;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class EarlyBirdTrack extends Track
{
	private final Set<UUID> _wonEternal = Sets.newConcurrentHashSet();

	public EarlyBirdTrack()
	{
		super("early-bird", ChatColor.AQUA, "Early Bird", "Early Bird", "This track is unlocked by receiving the Eternal rank from chickens in the 2016 Thanksgiving Event", true);
		special();
		getRequirements()
				.addTier(new TrackTier(
						"Early Bird",
						null,
						player -> _wonEternal.contains(player.getUniqueId()),
						new TrackFormat(ChatColor.AQUA, ChatColor.AQUA)
				));

		Managers.require(CoreClientManager.class).addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "eternal-fetcher";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				if (resultSet.next())
				{
					_wonEternal.add(uuid);
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT * FROM eternalGiveaway WHERE accountId = '" + accountId + "';";
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_wonEternal.remove(event.getPlayer().getUniqueId());
	}
}