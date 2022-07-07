package mineplex.core.donation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.google.gson.Gson;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.event.ClientUnloadEvent;
import mineplex.core.account.event.ClientWebResponseEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.donation.command.CrownCommand;
import mineplex.core.donation.command.GemCommand;
import mineplex.core.donation.command.ShardCommand;
import mineplex.core.donation.crown.CrownRepository;
import mineplex.core.donation.gold.GoldRepository;
import mineplex.core.donation.repository.DonationRepository;
import mineplex.core.donation.repository.token.DonorTokenWrapper;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;

/**
 * This manager handles the rewarding of transactions in the form of sales packages and currency
 */
@ReflectivelyCreateMiniPlugin
public class DonationManager extends MiniClientPlugin<Donor>
{
	public enum Perm implements Permission
	{
		CROWN_COMMAND,
		GEM_COMMAND,
		SHARD_COMMAND,
	}

	/**
	 * The maximum number of attempts that will be made to perform a transaction created by {@link DonationManager#rewardCurrencyUntilSuccess}
	 */
	public static final int MAX_GIVE_ATTEMPTS = 10;

	private static final Gson GSON = new Gson();

	private final Map<GlobalCurrency, LinkedList<CurrencyRewardData>> _attemptUntilSuccess = new HashMap<>();
	
	private final Map<UUID, Integer> _crownBalance = new ConcurrentHashMap<>();
	private final CrownRepository _crownRepository;
	
	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private final DonationRepository _repository;
	private final GoldRepository _goldRepository;

	private DonationManager()
	{
		super("Donation");

		_repository = new DonationRepository();
		_goldRepository = new GoldRepository();
		
		_crownRepository = new CrownRepository();
		_clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "crown-balance-loader";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				boolean hasRow = resultSet.next();
				if (hasRow)
				{
					_crownBalance.put(uuid, resultSet.getInt(1));
				}
				else
				{
					_crownBalance.put(uuid, 0);
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT crownCount FROM accountCrowns WHERE accountId=" + accountId + ";";
			}
		});

		UtilScheduler.runEvery(UpdateType.FAST, this::processCoinAttemptQueue);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.CROWN_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.GEM_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SHARD_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GemCommand(this));
		addCommand(new ShardCommand(this));
		addCommand(new CrownCommand(this));
	}

	@EventHandler
	public void OnClientWebResponse(ClientWebResponseEvent event)
	{
		DonorTokenWrapper token = GSON.fromJson(event.GetResponse(), DonorTokenWrapper.class);

		Get(event.getUniqueId()).loadToken(token.DonorToken);
	}
	
	public GoldRepository getGoldRepository()
	{
		return _goldRepository;
	}
	
	public int getCrowns(Player player)
	{
		return getCrowns(player.getUniqueId());
	}
	
	public int getCrowns(UUID uuid)
	{
		return _crownBalance.computeIfAbsent(uuid, key -> 0);
	}
	
	/**
	 * Adds an unknown sales package to the specified {@link Player}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseUnknownSalesPackageCrown(Player player, String packageName, int cost, boolean oneTimePurchase, Consumer<TransactionResponse> callback)
	{
		purchaseUnknownSalesPackageCrown(_clientManager.Get(player), packageName, cost, oneTimePurchase, callback);
	}

	/**
	 * Adds an unknown sales package to the specified {@link CoreClient}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseUnknownSalesPackageCrown(CoreClient client, String packageName, int cost, boolean oneTimePurchase, Consumer<TransactionResponse> callback)
	{
		Donor donor = Get(client.getUniqueId());

		if (donor != null)
		{
			if (oneTimePurchase && donor.ownsUnknownSalesPackage(packageName))
			{
				if (callback != null)
				{
					callback.accept(TransactionResponse.AlreadyOwns);
				}

				return;
			}
		}
		
		_crownRepository.consumeCrowns(result ->
		{
			if (result == TransactionResponse.Success)
			{
				if (_crownBalance.containsKey(client.getUniqueId()))
				{
					_crownBalance.put(client.getUniqueId(), _crownBalance.get(client.getUniqueId()) - cost);
				}
				_repository.purchaseUnknownSalesPackage(client.getName(), packageName, GlobalCurrency.GEM, 0, response ->
				{
					if (response == TransactionResponse.Success)
					{
						if (donor != null)
						{
							donor.addOwnedUnknownSalesPackage(packageName);
							donor.addBalance(GlobalCurrency.GEM, 0);
						}
					}

					if (callback != null)
					{
						callback.accept(response);
					}
				});
			}
			else if (callback != null)
			{
				callback.accept(result);
			}
		}, client.getAccountId(), cost);
	}

	/**
	 * Adds an unknown sales package to the specified {@link Player}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseUnknownSalesPackage(Player player, String packageName, GlobalCurrency currencyType, int cost, boolean oneTimePurchase, Consumer<TransactionResponse> callback)
	{
		purchaseUnknownSalesPackage(_clientManager.Get(player), packageName, currencyType, cost, oneTimePurchase, callback);
	}

	/**
	 * Adds an unknown sales package to the specified {@link CoreClient}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseUnknownSalesPackage(CoreClient client, String packageName, GlobalCurrency currencyType, int cost, boolean oneTimePurchase, Consumer<TransactionResponse> callback)
	{
		Donor donor = Get(client.getUniqueId());

		if (donor != null)
		{
			if (oneTimePurchase && donor.ownsUnknownSalesPackage(packageName))
			{
				if (callback != null)
					callback.accept(TransactionResponse.AlreadyOwns);

				return;
			}
		}

		_repository.purchaseUnknownSalesPackage(client.getName(), packageName, currencyType, cost, response ->
		{
			if (response == TransactionResponse.Success)
			{
				if (donor != null)
				{
					donor.addOwnedUnknownSalesPackage(packageName);
					donor.addBalance(currencyType, -cost);
				}
			}

			if (callback != null)
				callback.accept(response);
		});
	}

	/**
	 * Adds a known sales package to the {@link Player}
	 */
	public void purchaseKnownSalesPackage(Player player, int salesPackageId)
	{
		purchaseKnownSalesPackage(player, salesPackageId, null);
	}

	/**
	 * Adds a known sales package to the {@link CoreClient}
	 */
	public void purchaseKnownSalesPackage(CoreClient client, int salesPackageId)
	{
		purchaseKnownSalesPackage(client, salesPackageId, null);
	}

	/**
	 * Adds a known sales package to the {@link Player}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseKnownSalesPackage(Player player, int salesPackageId, Consumer<TransactionResponse> callback)
	{
		purchaseKnownSalesPackage(_clientManager.Get(player), salesPackageId, callback);
	}

	/**
	 * Adds a known sales package to the {@link CoreClient}
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link TransactionResponse#InsufficientFunds}, when the player does not have enough of the currency
	 *                 {@link TransactionResponse#Success}, when everything worked fine
	 *                 {@link TransactionResponse#Failed}, when an known exception occured
	 *                 {@link TransactionResponse#AlreadyOwns}, when the player already owns the package
	 */
	public void purchaseKnownSalesPackage(CoreClient client, int salesPackageId, Consumer<TransactionResponse> callback)
	{
		_repository.purchaseKnownSalesPackage(client.getName(), salesPackageId, response ->
		{
			if (response == TransactionResponse.Success)
			{
				Donor donor = Get(client.getUniqueId());

				if (donor != null)
				{
					donor.addOwnedKnownSalesPackage(salesPackageId);
				}
			}

			if (callback != null)
				callback.accept(response);
		});
	}
	
	public void rewardCrowns(int crowns, Player player)
	{
		rewardCrowns(crowns, player, null);
	}
	
	public void rewardCrowns(int crowns, Player player, Consumer<Boolean> completed)
	{
		_crownRepository.rewardCrowns(success ->
		{
			if (success)
			{
				_crownBalance.merge(player.getUniqueId(), crowns, Integer::sum);
			}
			if (completed != null)
			{
				completed.accept(success);
			}
		}, _clientManager.Get(player).getAccountId(), crowns);
	}

	/**
	 * Rewards the specified {@link Player} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 */
	public void rewardCurrency(GlobalCurrency currency, Player player, String reason, int amount)
	{
		rewardCurrency(currency, player, reason, amount, true, null);
	}

	/**
	 * Rewards the specified {@link Player} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link Boolean#TRUE} if the transaction succeeded
	 *                 {@link Boolean#FALSE} if the transaction failed, either during incrementation or sanity-checking
	 */
	public void rewardCurrency(GlobalCurrency currency, Player player, String reason, int amount, Consumer<Boolean> callback)
	{
		rewardCurrency(currency, player, reason, amount, true, callback);
	}

	/**
	 * Rewards the specified {@link Player} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 *
	 * @param updateTotal Whether to update the local value for {@code currency}
	 * @param callback    The callback which will be called on the main thread. Possible responses are:
	 *                    {@link Boolean#TRUE} if the transaction succeeded
	 *                    {@link Boolean#FALSE} if the transaction failed, either during incrementation or sanity-checking
	 */
	public void rewardCurrency(GlobalCurrency currency, Player player, String reason, int amount, boolean updateTotal, Consumer<Boolean> callback)
	{
		rewardCurrency(currency, _clientManager.Get(player), reason, amount, updateTotal, callback);
	}

	/**
	 * Rewards the specified {@link CoreClient} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 */
	public void rewardCurrency(GlobalCurrency currency, CoreClient client, String reason, int amount)
	{
		rewardCurrency(currency, client.getName(), client.getUniqueId(), reason, amount, true, null);
	}

	/**
	 * Rewards the specified {@link CoreClient} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 *
	 * @param callback The callback which will be called on the main thread. Possible responses are:
	 *                 {@link Boolean#TRUE} if the transaction succeeded
	 *                 {@link Boolean#FALSE} if the transaction failed, either during incrementation or sanity-checking
	 */
	public void rewardCurrency(GlobalCurrency currency, CoreClient client, String reason, int amount, Consumer<Boolean> callback)
	{
		rewardCurrency(currency, client.getName(), client.getUniqueId(), reason, amount, true, callback);
	}

	/**
	 * Rewards the specified {@link CoreClient} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will not retry the query if it fails
	 *
	 * @param updateTotal Whether to update the local value for {@code currency}
	 * @param callback    The callback which will be called on the main thread. Possible responses are:
	 *                    {@link Boolean#TRUE} if the transaction succeeded
	 *                    {@link Boolean#FALSE} if the transaction failed, either during incrementation or sanity-checking
	 */
	public void rewardCurrency(GlobalCurrency currency, CoreClient client, String reason, int amount, boolean updateTotal, Consumer<Boolean> callback)
	{
		rewardCurrency(currency, client.getName(), client.getUniqueId(), reason, amount, updateTotal, callback);
	}
	
	/**
	 * Rewards a player a specific amount of a currency type - used by Votifier ONLY due to volatile playerName and playerUUID
	 * @param currency The type of currency to reward
	 * @param playerName The name of the player being rewarded
	 * @param playerUUID The UUID of the player being rewarded
	 * @param reason The reason for the currency being awarded
	 * @param amount The amount of currency being rewarded
	 * @param callback A callback to retrieve the success of the award
	 */
	public void rewardCurrency(GlobalCurrency currency, String playerName, UUID playerUUID, String reason, int amount, Consumer<Boolean> callback)
	{
		rewardCurrency(currency, playerName, playerUUID, reason, amount, true, callback);
	}

	// Private because volatile with playerName and playerUUID
	private void rewardCurrency(GlobalCurrency currency, String playerName, UUID playerUUID, String reason, int amount, boolean updateTotal, Consumer<Boolean> callback)
	{
		_repository.reward(currency, playerName, reason, amount, success ->
		{
			if (success)
			{
				if (updateTotal)
				{
					Donor donor = Get(playerUUID);
					if (donor != null)
					{
						donor.addBalance(currency, amount);
					}
				}
			}

			if (callback != null)
			{
				callback.accept(success);
			}
		});
	}

	/**
	 * Rewards the {@link Player} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will retry the transaction for up to {@link DonationManager#MAX_GIVE_ATTEMPTS} attempts.
	 *
	 * This method is <b>not</b> thread safe, and should be called on the main thread
	 */
	public void rewardCurrencyUntilSuccess(GlobalCurrency currency, Player player, String reason, int amount)
	{
		rewardCurrencyUntilSuccess(currency, _clientManager.Get(player), reason, amount);
	}

	/**
	 * Rewards the {@link CoreClient} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will retry the transaction for up to {@link DonationManager#MAX_GIVE_ATTEMPTS} attempts.
	 *
	 * This method is <b>not</b> thread safe, and should be called on the main thread
	 */
	public void rewardCurrencyUntilSuccess(GlobalCurrency currency, CoreClient client, String reason, int amount)
	{
		rewardCurrencyUntilSuccess(currency, client.getName(), client.getUniqueId(), reason, amount, null);
	}

	/**
	 * Rewards the {@link Player} with {@code amount} of {@code currency} because of {@code reason}
	 * This method will retry the transaction for up to {@link DonationManager#MAX_GIVE_ATTEMPTS} attempts
	 *
	 * This method is <b>not</b> thread safe, and should be called on the main thread
	 *
	 * @param callback    The callback which will be called on the main thread. Possible responses are:
	 *                    {@link Boolean#TRUE} if the transaction succeeded
	 *                    {@link Boolean#FALSE} if the transaction failed, either during incrementation or sanity-checking
	 */
	public void rewardCurrencyUntilSuccess(GlobalCurrency currency, Player player, String reason, int amount, Consumer<Boolean> callback)
	{
		CoreClient client = _clientManager.Get(player);
		rewardCurrencyUntilSuccess(currency, client.getName(), client.getUniqueId(), reason, amount, callback);
	}

	// private because volatile with name and accountId
	private void rewardCurrencyUntilSuccess(GlobalCurrency currency, String name, UUID playerUUID, String reason, int amount, Consumer<Boolean> callback)
	{
		_attemptUntilSuccess.computeIfAbsent(currency, key -> new LinkedList<>())
				.add(new CurrencyRewardData(name, playerUUID, reason, amount, callback));
	}

	public void applyKits(String playerName)
	{
		_repository.applyKits(playerName);
	}

	@Override
	protected Donor addPlayer(UUID uuid)
	{
		return new Donor();
	}

	private void processCoinAttemptQueue()
	{
		_attemptUntilSuccess.forEach((currency, playerMap) ->
		{
			CurrencyRewardData data = playerMap.poll();

			if (data != null)
			{
				_repository.reward(currency, data.getPlayerName(), data.getReason(), data.getAmount(), success ->
				{
					if (success)
					{
						Donor donor = Get(data.getPlayerUUID());

						if (donor != null)
						{
							donor.addBalance(currency, data.getAmount());
						}

						if (data.getCallback() != null)
						{
							data.getCallback().accept(true);
						}

						System.out.println("Successfully rewarded " + currency.getPrefix() + " to player " + data.getPlayerName());
					}
					else
					{
						data.incrementAttempts();

						if (data.getAttempts() >= MAX_GIVE_ATTEMPTS)
						{
							// Admit Defeat!
							if (data.getCallback() != null) data.getCallback().accept(false);
							System.out.println("Gave up giving " + currency.getPrefix() + " to player " + data.getPlayerName());
						}
						else
						{
							playerMap.add(data);
							System.out.println("Failed to reward " + currency.getPrefix() + " to player " + data.getPlayerName() + ". Attempts: " + data.getAttempts());
						}
					}
				});
			}
		});
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
	
	@EventHandler
	public void unloadCrownBalance(ClientUnloadEvent event)
	{
		_crownBalance.remove(event.getUniqueId());
	}
}
