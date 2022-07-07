package mineplex.game.clans.economy;

public class GoldData
{
	private int balance;

	public void addBalance(int amount)
	{
		balance += amount;
	}

	public void setBalance(int amount)
	{
		balance = amount;
	}

	public int getBalance()
	{
		return balance;
	}
}