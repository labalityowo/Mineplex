package mineplex.core.reward.rewards;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.util.Callback;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.serverdata.database.DBPool;

public class SpinTicketReward extends Reward
{

	private static final ItemStack ITEM_STACK = new ItemStack(Material.PAPER);

	private int _max, _min;

	public SpinTicketReward(int min, int max, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_max = max;
		_min = min;
	}

	@Override
	public RewardData giveRewardCustom(Player player)
	{
		if (CLIENT_MANAGER.getAccountId(player) == -1)
		{
			return getFakeRewardData(player);
		}
		
		final int amountToGive;

		if (_min != _max)
		{
			amountToGive = RANDOM.nextInt(_max - _min) + _min;
		}
		else
		{
			amountToGive = _min;
		}
		
		final int accountId = CLIENT_MANAGER.getAccountId(player);
		final Callback<Integer> ticketCallback = newTickets -> Managers.get(BonusManager.class).Get(player).setTickets(newTickets);
		CLIENT_MANAGER.runAsync(() ->
		{
			try (Connection c = DBPool.getAccount().getConnection(); Statement statement = c.createStatement())
			{
				final String query = "UPDATE bonus SET tickets = tickets + " + amountToGive + " WHERE accountId = " + accountId + ";SELECT tickets FROM bonus WHERE accountId = " + accountId;
				
				statement.execute(query);
				statement.getUpdateCount();
				statement.getMoreResults();
				
				ResultSet rs = statement.getResultSet();
				if (rs.next())
				{
					final int newTickets = rs.getInt(1);
					CLIENT_MANAGER.runSync(() -> ticketCallback.run(newTickets));
				}
			}
			catch (Exception e)
			{
				System.out.println("Failed to award ticket to player: " + player);
				e.printStackTrace();
			}
		});

		return new RewardData(getRarity().getDarkColor() + "Carl Spin Ticket", getRarity().getColor() + amountToGive + " Carl Spin Ticket", ITEM_STACK, getRarity());
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + "Carl Spin Ticket", getRarity().getColor() + "Carl Spin ticket", ITEM_STACK, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		return true;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof SpinTicketReward;
	}
	
	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}
}