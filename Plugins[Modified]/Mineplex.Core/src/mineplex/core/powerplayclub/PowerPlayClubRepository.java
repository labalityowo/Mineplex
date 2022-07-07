package mineplex.core.powerplayclub;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.serverdata.database.DBPool;

public class PowerPlayClubRepository implements Listener {
	// Data loaded by the first ILoginProcessor, waiting for the second
	private final Map<UUID, List<YearMonth>> _stageOneDataClaims = new HashMap<>();

	// Cached data produced by combining the first and second ILoginProcessors.
	// This data is not guaranteed to be fresh, and should be recalculated before
	// giving a player his/her rewards.
	private final Map<UUID, PowerPlayData> _cachedPlayerData = new HashMap<>();

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;

	public PowerPlayClubRepository(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager) {
		_clientManager = clientManager;
		_donationManager = donationManager;

		Bukkit.getPluginManager().registerEvents(this, plugin);

		clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor() {
			@Override
			public String getName() {
				return "PPC Claim Grabber";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException {
				List<YearMonth> claims = new ArrayList<>();
				while (resultSet.next())
				{
					claims.add(YearMonth.of(resultSet.getInt("claimYear"), resultSet.getInt("claimMonth")));
				}
				_stageOneDataClaims.put(uuid, claims);
			}

			@Override
			public String getQuery(int accountId, String uuid, String name) {
				return "SELECT * FROM powerPlayClaims WHERE accountId = " + accountId + ";";
			}
		});

		clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor() {
			@Override
			public String getName() {
				return "PPC Subscription Grabber";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException {
				List<PowerPlayData.Subscription> subscriptions = new ArrayList<>();
				while (resultSet.next())
				{
					LocalDate date = resultSet.getDate("startDate").toLocalDate();
					PowerPlayData.SubscriptionDuration duration = PowerPlayData.SubscriptionDuration.valueOf(resultSet.getString("duration").toUpperCase());
					subscriptions.add(new PowerPlayData.Subscription(date, duration));
				}

				// Now that we have the claims from the first processor and subscriptions from this one, combine them
				_cachedPlayerData.put(uuid, PowerPlayData.fromSubsAndClaims(subscriptions, _stageOneDataClaims.remove(uuid)));
			}

			@Override
			public String getQuery(int accountId, String uuid, String name) {
				return "SELECT * FROM powerPlaySubs WHERE accountId = " + accountId + ";";
			}
		});
	}

	// Add usable cosmetics to player on join
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		PowerPlayData cached = getCachedData(player);

		PowerPlayClubRewards.rewardsForMonths(cached.getUsableCosmeticMonths()).forEach(item -> item.reward(player));

		// Gives Metal Man for anyone subscribed
		if (cached.getUsableCosmeticMonths().size() > 0)
		{
			_donationManager.Get(player).addOwnedUnknownSalesPackage("Metal Man Morph");
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_stageOneDataClaims.remove(event.getPlayer().getUniqueId()); // Just in case.
		_cachedPlayerData.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onDataRequested(PPCDataRequestEvent event)
	{
		event.setData(getCachedData(event.getPlayer()));
	}

	public CompletableFuture<Void> addSubscription(int accountId, LocalDate date, String duration)
	{
		UtilServer.CallEvent(new SubscriptionAddEvent(accountId, duration));
		
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement("INSERT INTO powerPlaySubs (accountId, startDate, duration) VALUES (?, ?, ?)");
				statement.setInt(1, accountId);
				statement.setDate(2, Date.valueOf(date));
				statement.setString(3, duration);
				statement.executeUpdate();

			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}

			return null;
		});
	}

	public CompletableFuture<Boolean> attemptClaim(Player player)
	{
		int accountId = _clientManager.Get(player).getAccountId();

		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				LocalDate date = LocalDate.now();
				PreparedStatement statement = connection.prepareStatement("INSERT IGNORE INTO powerPlayClaims (accountId, claimMonth, claimYear) VALUES (?, ?, ?)");
				statement.setInt(1, accountId);
				statement.setInt(2, date.getMonthValue());
				statement.setInt(3, date.getYear());

				return statement.executeUpdate() == 1;

			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return false;
		});
	}

	public CompletableFuture<PowerPlayData> loadData(Player player)
	{
		return loadData(_clientManager.Get(player).getAccountId());
	}

	public CompletableFuture<PowerPlayData> loadData(int accountId)
	{
		return loadSubscriptions(accountId).thenCombine(loadClaimMonths(accountId), PowerPlayData::fromSubsAndClaims);
	}

	public CompletableFuture<List<YearMonth>> loadClaimMonths(int accountId)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement("SELECT * FROM powerPlayClaims WHERE accountId = ?");
				statement.setInt(1, accountId);
				ResultSet resultSet = statement.executeQuery();

				List<YearMonth> claims = new ArrayList<>();
				while (resultSet.next())
				{
					claims.add(YearMonth.of(resultSet.getInt("claimYear"), resultSet.getInt("claimMonth")));
				}
				return claims;

			} catch (SQLException e)
			{
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<List<PowerPlayData.Subscription>> loadSubscriptions(int accountId)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try (Connection connection = DBPool.getAccount().getConnection())
			{
				PreparedStatement statement = connection.prepareStatement("SELECT * FROM powerPlaySubs WHERE accountId = ?");
				statement.setInt(1, accountId);
				ResultSet resultSet = statement.executeQuery();

				List<PowerPlayData.Subscription> subscriptions = new ArrayList<>();
				while (resultSet.next())
				{
					LocalDate date = resultSet.getDate("startDate").toLocalDate();
					PowerPlayData.SubscriptionDuration duration = PowerPlayData.SubscriptionDuration.valueOf(resultSet.getString("duration").toUpperCase());
					subscriptions.add(new PowerPlayData.Subscription(date, duration));
				}
				return subscriptions;

			} catch (SQLException e)
			{
				throw new CompletionException(e);
			}
		});
	}

	public PowerPlayData getCachedData(Player player)
	{
		return _cachedPlayerData.get(player.getUniqueId());
	}

	public void putCachedData(Player player, PowerPlayData data)
	{
		if (player.isOnline())
		{
			_cachedPlayerData.put(player.getUniqueId(), data);
		}
	}
}
