package mineplex.core.donation.repository.token;

import java.util.List;

public class DonorToken
{
    public int Gems;
    public boolean Donated;
    public List<Integer> SalesPackages;
	public List<String> UnknownSalesPackages;
	public List<TransactionToken> Transactions;
	public List<CoinTransactionToken> CoinRewards;
	public int Coins;
}
