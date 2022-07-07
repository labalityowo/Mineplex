package mineplex.core.thank;

import mineplex.core.database.MinecraftRepository;
import mineplex.database.routines.AddThank;
import mineplex.database.routines.ClaimThank;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class ThankRepository extends RepositoryBase
{
	private static final String GET_THANK_DATA = "SELECT SUM(thankAmount) FROM accountThankTransactions WHERE receiverId = ? AND claimed = FALSE";

	public ThankRepository()
	{
		super(DBPool.getAccount());
	}

	public boolean thank(int receiverAccountId, int senderAccountId, int thankAmount, String reason, boolean ignoreCooldown)
	{
		AddThank addThank = new AddThank();
		addThank.setInReceiverAccountId(receiverAccountId);
		addThank.setInSenderAccountId(senderAccountId);
		addThank.setInThankAmount(thankAmount);
		addThank.setInReason(reason);
		addThank.setInIgnoreCooldown(ignoreCooldown ? (byte) 1 : 0);

		addThank.execute(jooq().configuration());
		return addThank.getSuccess() == 1;
	}

	public ClaimThankResult claimThank(int accountId)
	{
		ClaimThank claimThank = new ClaimThank();
		claimThank.setInAccountId(accountId);
		claimThank.execute(jooq().configuration());
		return new ClaimThankResult(claimThank.getAmountClaimed(), claimThank.getUniqueThank());
	}

	/**
	 * Get ThankData for an accountId. This is used to reload ThankData when a player is thanked on live servers
	 * @param accountId The account id of the player
	 * @return {@link ThankData} for that player
	 */
	public ThankData getThankData(int accountId) throws SQLException
	{
		ThankData thankData = new ThankData(0);

		executeQuery(GET_THANK_DATA, resultSet ->
		{
			if (resultSet != null && resultSet.next())
			{
				thankData.setThankToClaim(resultSet.getInt(1));
			}
		}, new ColumnInt("receiverId", accountId));

		return thankData;
	}
}
