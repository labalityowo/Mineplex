package mineplex.core.thank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.thank.command.ThankCommand;

/**
 * This class handles /thank and amplifier thanks, along with allowing players to claim the rewards they earn
 */
public class ThankManager extends MiniDbClientPlugin<ThankData>
{
	public static final int DEFAULT_RECEIVER_REWARD = 5;
	public static final int DEFAULT_SENDER_REWARD = 5;

	public enum Perm implements Permission
	{
		THANK_COMMAND,
	}

	private DonationManager _donationManager;

	private ThankRepository _thankRepository;

	public ThankManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super("Thanks", plugin, clientManager);

		_donationManager = donationManager;

		_thankRepository = new ThankRepository();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.THANK_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new ThankCommand(this));
	}

	/**
	 * Attempt to thank a player. This can be used to distribute rewards to players, give players rewards for using
	 * amplifiers, and allow players to thank anyone inside a game.
	 *
	 * @param receiver       The player who is being thanked
	 * @param sender         The player thanking receiver
	 * @param receiverReward The Treasure Shard reward for the receiver
	 * @param senderReward   The Treasure Shard reward for the sender
	 * @param reason         The reason that player is being thanked
	 * @param ignoreCooldown Should we ignore all thank cooldowns
	 * @param callback       Callback for processing the result
	 */
	public void thankPlayer(Player receiver, Player sender, int receiverReward, int senderReward, String reason, boolean ignoreCooldown, Callback<ThankResult> callback)
	{
		if (!Recharge.Instance.use(sender, "Thank Player", 1000 * 5, false, false))
		{
			UtilPlayer.message(sender, F.main("Thank", "Please wait before trying that again"));
			callback.run(ThankResult.COOLDOWN_RECHARGE);
			return;
		}

		int receiverAccountId = ClientManager.getAccountId(receiver);
		thankPlayer(receiver.getName(), receiverAccountId, sender, receiverReward, senderReward, reason, ignoreCooldown, result ->
		{
			if (result == ThankResult.SUCCESS)
			{
				// Reload their thank data if the player is online!
				runAsync(() ->
				{
					try
					{
						Set(receiver, _thankRepository.getThankData(receiverAccountId));
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				});
			}

			callback.run(result);
		});
	}

	/**
	 * Called when a player wants to "claim" all pending rewards that they haven't claimed yet
	 *
	 * @param player   The player claiming their thank rewards
	 * @param callback Callback with the result of the claim
	 */
	public void claimThanks(Player player, Callback<ClaimThankResult> callback)
	{
		final String playerName = player.getName();
		final int accountId = ClientManager.getAccountId(player);

		if (accountId == -1)
		{
			callback.run(null);
			return;
		}

		runAsync(() ->
		{
			try
			{
				ClaimThankResult result = _thankRepository.claimThank(accountId);
				runSync(() ->
				{
					if (result != null && result.getClaimed() > 0)
					{
						Set(player, new ThankData(0));
						_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, player, "Thank", result.getClaimed());
					}
					callback.run(result);
				});
			}
			catch (RuntimeException ex)
			{
				ex.printStackTrace();
				runSync(() -> callback.run(null));
			}
		});
	}

	/**
	 * Attempt to thank a player. This can be used to distribute rewards to players, give players rewards for using
	 * amplifiers, and allow players to thank anyone inside a game.
	 *
	 * @param receiverName      Name of the player being thanked
	 * @param receiverAccountId Account id of the player being thanked
	 * @param sender            The player sending the thanks
	 * @param receiverReward    The Treasure Shard reward for the receiver
	 * @param senderReward      The Treasure Shard reward for the sender
	 * @param reason            The reason that player is being thanked
	 * @param ignoreCooldown    Should we ignore all thank cooldowns
	 * @param callback          Callback for processing the result
	 */
	public void thankPlayer(String receiverName, int receiverAccountId, Player sender, int receiverReward, int senderReward, String reason, boolean ignoreCooldown, Callback<ThankResult> callback)
	{
		int senderAccountId = getClientManager().getAccountId(sender);

		// Break out on bad account id
		if (senderAccountId == -1 || receiverAccountId == -1)
		{
			callback.run(ThankResult.INVALID_ACCOUNT_ID);
			return;
		}

		// You can't thank yourself, silly!
		if (receiverAccountId == senderAccountId)
		{
			callback.run(ThankResult.CANNOT_THANK_SELF);
			return;
		}

		runAsync(() ->
		{
			try
			{
				boolean success = _thankRepository.thank(receiverAccountId, senderAccountId, receiverReward, reason, ignoreCooldown);
				runSync(() ->
				{
					if (success)
					{
						// Reward Shards for the sender now. The player being thanked can claim their shards at Carl.
						_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, sender, "Thank", senderReward);
					}

					callback.run(success ? ThankResult.SUCCESS : ThankResult.COOLDOWN_DATABASE);
				});
			}
			catch (RuntimeException ex)
			{
				ex.printStackTrace();
				runSync(() -> callback.run(ThankResult.DATABASE_ERROR));
			}
		});
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT SUM(thankAmount) FROM accountThankTransactions WHERE receiverId = " + accountId + " AND claimed = FALSE;";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID playerUUID, int accountId, ResultSet resultSet) throws SQLException
	{
		if (resultSet.next())
		{
			int thankToClaim = resultSet.getInt(1);
			Set(playerUUID, new ThankData(thankToClaim));
		}
	}

	@Override
	protected ThankData addPlayer(UUID player)
	{
		return new ThankData(0);
	}
}
