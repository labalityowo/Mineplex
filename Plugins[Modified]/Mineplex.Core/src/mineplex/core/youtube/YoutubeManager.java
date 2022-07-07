package mineplex.core.youtube;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;

public class YoutubeManager extends MiniDbClientPlugin<YoutubeClient>
{
	private static final int REWARD_MESSAGE_DELAY_SECONDS = 30;
	private final YoutubeRepository _repository;
	private final DonationManager _donationManager;

	public YoutubeManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("YoutubeManager", plugin, clientManager);
		_donationManager = donationManager;
		_repository = new YoutubeRepository(this);
		
		clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "specific-youtuber-click";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				boolean hasRow = resultSet.next();
				if (hasRow)
				{
					YoutubeManager.this.Get(uuid).setSpecificDate(resultSet.getDate(1).toLocalDate());
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT clicktime FROM specificYoutube WHERE accountId=" + accountId + ";";
			}
		});
	}

	public boolean canYoutube(Player player)
	{
		YoutubeClient client = Get(player);
		LocalDate date = client.getClickDate();

		if (date == null)
		{
			return true;
		}

		ZonedDateTime utcZoned = ZonedDateTime.now(ZoneOffset.UTC);
		LocalDate utc = utcZoned.toLocalDate();

		return !date.equals(utc);
	}
	
	public boolean canSpecificYoutube(Player player)
	{
		YoutubeClient client = Get(player);
		LocalDate date = client.getSpecificDate();

		if (date == null)
		{
			return true;
		}

		ZonedDateTime utcZoned = ZonedDateTime.now(ZoneOffset.UTC);
		LocalDate utc = utcZoned.toLocalDate();

		return !date.equals(utc);
	}

	public void attemptYoutube(Player player, boolean clans, final int clansServerId)
	{
		if (!canYoutube(player))
		{
			return;
		}
		YoutubeClient client = Get(player);
		final int accountId = getClientManager().getAccountId(player);
		client.setClickDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDate());
		_repository.attemptYoutube(player, client, () ->
		{
			if (clans && clansServerId != -1)
			{
				_donationManager.getGoldRepository().rewardGold(success ->
				{
					Bukkit.getScheduler().runTaskLater(getClientManager().getPlugin(), () -> UtilPlayer.message(player, F.main("Carl", "Rewarded " + F.elem("250 Gold") + " on your home server for watching the YouTube video")), REWARD_MESSAGE_DELAY_SECONDS * 20L);
				}, clansServerId, accountId, 250);
			}
			else
			{
				_donationManager.rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, "YouTube", 250);
				Bukkit.getScheduler().runTaskLater(getClientManager().getPlugin(), () -> UtilPlayer.message(player, F.main("Carl", "Rewarded " + F.elem("250 Treasure Shards") + " for watching the YouTube video")), REWARD_MESSAGE_DELAY_SECONDS * 20L);
			}
		});
	}
	
	public void attemptSpecificYoutube(Player player, final boolean clans, final int clansServerId)
	{
		if (!canYoutube(player))
		{
			return;
		}
		YoutubeClient client = Get(player);
		final int accountId = getClientManager().getAccountId(player);
		client.setSpecificDate(ZonedDateTime.now(ZoneOffset.UTC).toLocalDate());
		_repository.attemptSpecificYoutube(player, client, () ->
		{
			if (clans && clansServerId != -1)
			{
				_donationManager.getGoldRepository().rewardGold(success ->
				{
					Bukkit.getScheduler().runTaskLater(getClientManager().getPlugin(), () -> UtilPlayer.message(player, F.main("Carl", "Rewarded " + F.elem("250 Gold") + " on your home server for watching the YouTube video")), REWARD_MESSAGE_DELAY_SECONDS * 20L);
				}, clansServerId, accountId, 250);
			}
			else
			{
				_donationManager.rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, "YouTube", 250);
				Bukkit.getScheduler().runTaskLater(getClientManager().getPlugin(), () -> UtilPlayer.message(player, F.main("Carl", "Rewarded " + F.elem("250 Treasure Shards") + " for watching the YouTube video")), REWARD_MESSAGE_DELAY_SECONDS * 20L);
			}
		});
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		boolean hasRow = resultSet.next();
		if (hasRow)
			Set(uuid, new YoutubeClient(resultSet.getDate(1).toLocalDate(), null));
		else
			Set(uuid, new YoutubeClient(null, null));
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT clicktime FROM youtube WHERE accountId=" + accountId + ";";
	}

	@Override
	protected YoutubeClient addPlayer(UUID uuid)
	{
		return new YoutubeClient(null, null);
	}
}