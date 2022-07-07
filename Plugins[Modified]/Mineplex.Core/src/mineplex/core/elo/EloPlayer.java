package mineplex.core.elo;

import org.bukkit.entity.Player;

public class EloPlayer
{
	private Player _player;
	private int _accountId;
	private int _rating;
	
	public EloPlayer(Player player, int accountId, int rating)
	{
		_player = player;
		_accountId = accountId;
		_rating = rating;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public int getRating()
	{
		return _rating;
	}
	
	public int getAccountId()
	{
		return _accountId;
	}
	
	public void printInfo()
	{
		System.out.println(_player.getName() + "'s elo is " + _rating);
	}
}
