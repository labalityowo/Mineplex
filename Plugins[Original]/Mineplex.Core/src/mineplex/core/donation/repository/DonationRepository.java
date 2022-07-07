package mineplex.core.donation.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import mineplex.core.common.currency.Currency;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.UtilTasks;
import mineplex.core.database.MinecraftRepository;
import mineplex.core.donation.repository.token.GemRewardToken;
import mineplex.core.donation.repository.token.PurchaseToken;
import mineplex.core.donation.repository.token.UnknownPurchaseToken;
import mineplex.core.server.util.TransactionResponse;
import mineplex.serverdata.database.DBPool;

public class DonationRepository extends MinecraftRepository
{
	private static Map<GlobalCurrency, String> WEB_ADDRESSES = new HashMap<>();

	static
	{
		WEB_ADDRESSES.put(GlobalCurrency.GEM, "PlayerAccount/GemReward");
		WEB_ADDRESSES.put(GlobalCurrency.TREASURE_SHARD, "PlayerAccount/CoinReward");
	}

	public DonationRepository()
	{
		super(DBPool.getAccount());
	}

	/**
	 * Purchases a known sales package
	 *
	 * @param playerName     The player name
	 * @param salesPackageId The package id
	 * @param callback       The callback, may be null, will be run on the main thread
	 */
	public void purchaseKnownSalesPackage(String playerName, int salesPackageId, Consumer<TransactionResponse> callback)
	{
		PurchaseToken token = new PurchaseToken();
		token.AccountName = playerName;
		token.UsingCredits = false;
		token.SalesPackageId = salesPackageId;

		handleMSSQLCall(
				"PlayerAccount/PurchaseKnownSalesPackage",
				String.format("Error purchasing known sales package %s for %s: ", salesPackageId, playerName),
				token,
				TransactionResponse.class,
				UtilTasks.onMainThread(callback)
		);
	}

	/**
	 * Purchases an unknown sales package
	 *
	 * @param playerName   The name of the player
	 * @param packageName  The name of the unknown package
	 * @param currencyType The type of currency
	 * @param cost         The cost
	 * @param callback     The callback, may be null. Will be run on the main thread
	 */
	public void purchaseUnknownSalesPackage(String playerName, String packageName, Currency currencyType, int cost, Consumer<TransactionResponse> callback)
	{
		UnknownPurchaseToken token = new UnknownPurchaseToken();
		token.AccountName = playerName;
		token.SalesPackageName = packageName;
		token.CoinPurchase = currencyType == GlobalCurrency.TREASURE_SHARD;
		token.Cost = cost;
		token.Premium = false;

		handleMSSQLCall(
				"PlayerAccount/PurchaseUnknownSalesPackage",
				String.format("Error purchasing unknown sales package %s for %s: ", packageName, playerName),
				token,
				TransactionResponse.class,
				UtilTasks.onMainThread(callback)
		);
	}

	/**
	 * Update a player's currency on the MSSQL server.
	 *
	 * @param currency   The type of currency
	 * @param playerName The name of the player
	 * @param reason     The reason of rewarding currency
	 * @param amount     The amount (positive or negative)
	 * @param callback   The callback, can be null. This will be run on the main thread
	 */
	public void reward(GlobalCurrency currency, String playerName, String reason, int amount, Consumer<Boolean> callback)
	{
		GemRewardToken token = new GemRewardToken();
		token.Source = reason;
		token.Name = playerName;
		token.Amount = amount;

		handleMSSQLCall(
				WEB_ADDRESSES.get(currency),
				String.format("Error updating %s for %s: ", currency.getString(2), playerName),
				token,
				Boolean.class,
				UtilTasks.onMainThread(callback)
		);
	}

	public void applyKits(String playerName)
	{
		handleAsyncMSSQLCall("PlayerAccount/ApplyKits", playerName);
	}
}