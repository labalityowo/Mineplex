package mineplex.core.donation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.donation.repository.token.CoinTransactionToken;
import mineplex.core.donation.repository.token.DonorToken;
import mineplex.core.donation.repository.token.TransactionToken;

/**
 * Represents a player's donation information
 */
public class Donor
{
	private final Map<GlobalCurrency, Integer> _balances = new HashMap<>();
	private List<Integer> _salesPackagesOwned = new ArrayList<>();
	private List<String> _unknownSalesPackagesOwned = new ArrayList<>();
	private List<TransactionToken> _transactions = new ArrayList<>();
	private List<CoinTransactionToken> _coinTransactions = new ArrayList<>();

	void loadToken(DonorToken token)
	{
		_balances.put(GlobalCurrency.GEM, token.Gems);
		_balances.put(GlobalCurrency.TREASURE_SHARD, token.Coins);

		_salesPackagesOwned = token.SalesPackages;
		_unknownSalesPackagesOwned = token.UnknownSalesPackages;
		_transactions = token.Transactions;
		_coinTransactions = token.CoinRewards;
	}

	/**
	 * Get the known sales packages that this donor owns, local to this server
	 */
	public List<Integer> getOwnedKnownSalesPackages()
	{
		return _salesPackagesOwned;
	}

	/**
	 * Get the unknown sales packages that this donor owns, local to this server
	 */
	public List<String> getOwnedUnknownSalesPackages()
	{
		return _unknownSalesPackagesOwned;
	}

	/**
	 * Checks whether this donor owns the specified known sales package, local to this server
	 *
	 * @return True if this donor owns the specified package, or if the id specified is -1
	 */
	public boolean ownsKnownSalesPackage(int id)
	{
		return id == -1 || _salesPackagesOwned.contains(id);
	}

	/**
	 * Checks whether this donor owns the specified unknown sales package, local to this server
	 *
	 * @param packageName The package name, case sensitive
	 */
	public boolean ownsUnknownSalesPackage(String packageName)
	{
		return _unknownSalesPackagesOwned.contains(packageName);
	}

	/**
	 * Adds a sales package to this donor, local to this server
	 */
	public void addOwnedKnownSalesPackage(int salesPackageId)
	{
		_salesPackagesOwned.add(salesPackageId);
	}

	/**
	 * Adds an unknown sales package to this donor, local to this server
	 *
	 * @param packageName The package name, case sensitive
	 */
	public void addOwnedUnknownSalesPackage(String packageName)
	{
		_unknownSalesPackagesOwned.add(packageName);
	}

	/**
	 * Removes a known sales package from this donor, local to this server
	 */
	public void removeOwnedKnownSalesPackage(int id)
	{
		_salesPackagesOwned.remove(id);
	}

	/**
	 * Removes an unknown sales package from this donor, local to this server
	 *
	 * @param packageName The package name, case sensitive
	 */
	public void removeOwnedUnknownSalesPackage(String packageName)
	{
		_unknownSalesPackagesOwned.remove(packageName);
	}

	/**
	 * Gets the balance of the specified currency for this donor, local to this server
	 */
	public int getBalance(GlobalCurrency currencyType)
	{
		return _balances.getOrDefault(currencyType, 0);
	}

	/**
	 * Modifies the balance of the specified currency by the given amount, local to this server
	 *
	 * @param amount The amount to modify the balance by. Can be positive or negative
	 */
	public void addBalance(GlobalCurrency currencyType, int amount)
	{
		_balances.merge(currencyType, amount, Integer::sum);
	}

	/**
	 * Get the transactions associated to this player, local to this server
	 */
	public List<TransactionToken> getTransactions()
	{
		return _transactions;
	}

	/**
	 * Get the coin transactions associated to this player, local to this server
	 */
	public List<CoinTransactionToken> getCoinTransactions()
	{
		return _coinTransactions;
	}

	/**
	 * Clears all owned unknown packages with the given name, because it's a List
	 *
	 * @param packageName The name
	 */
	public void removeAllOwnedUnknownSalesPackages(String packageName)
	{
		_unknownSalesPackagesOwned.removeIf(pack -> pack.equals(packageName));
	}
}
