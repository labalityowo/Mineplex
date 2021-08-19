package mineplex.core.game.kit;

import java.util.List;

public class KitOperations
{

	private static final String INSERT_KIT_DATA = "INSERT INTO accountKits VALUES ({0},{1},{2});";
	private static final String SET_ACTIVE_KIT = "UPDATE accountKits SET active=0 WHERE accountId={0}{2}; UPDATE accountKits SET active=1 WHERE accountId={0} AND kitId={1};";
	private static final String INCREMENT_KIT_STAT = "INSERT INTO accountKitStats VALUES({0},{1},{2},{3}) ON DUPLICATE KEY UPDATE value=value+{3};";
	private static final String SET_KIT_STAT = "INSERT INTO accountKitStats VALUES({0},{1},{2},{3}) ON DUPLICATE KEY UPDATE value={3};";

	private final KitRepository _repository;
	private final int _accountId;
	private final String _accountIdString;
	private final StringBuilder _query;

	public KitOperations(KitRepository repository, int accountId)
	{
		_repository = repository;
		_accountId = accountId;
		_accountIdString = String.valueOf(_accountId);
		_query = new StringBuilder(1000);
	}

	public KitOperations unlockKit(int kitId, boolean setActive)
	{
		_query.append(INSERT_KIT_DATA
				.replace("{0}", _accountIdString)
				.replace("{1}", String.valueOf(kitId))
				.replace("{2}", String.valueOf(setActive)));

		return this;
	}

	public KitOperations setActiveKit(int kitId, List<Integer> kitIdsToDisable)
	{
		StringBuilder builder = new StringBuilder();

		if (!kitIdsToDisable.isEmpty())
		{
			builder.append(" AND (");
			int index = 0;
			for (int kitIdDisable : kitIdsToDisable)
			{
				builder.append((index++ != 0 ? " OR " : "")).append("kitId=").append(kitIdDisable);
			}

			builder.append(")");
		}

		_query.append(SET_ACTIVE_KIT
				.replace("{0}", _accountIdString)
				.replace("{1}", String.valueOf(kitId))
				.replace("{2}", builder.toString()));

		return this;
	}

	public KitOperations incrementStat(int kitId, int statId, int value)
	{
		_query.append(INCREMENT_KIT_STAT
				.replace("{0}", _accountIdString)
				.replace("{1}", String.valueOf(kitId))
				.replace("{2}", String.valueOf(statId))
				.replace("{3}", String.valueOf(value)));

		return this;
	}

	public KitOperations setStat(int kitId, int statId, int value)
	{
		_query.append(SET_KIT_STAT
				.replace("{0}", _accountIdString)
				.replace("{1}", String.valueOf(kitId))
				.replace("{2}", String.valueOf(statId))
				.replace("{3}", String.valueOf(value)));

		return this;
	}

	public int getAccountId()
	{
		return _accountId;
	}

	public String getQuery()
	{
		return _query.toString();
	}

	public boolean execute()
	{
		return _repository.executeKitOperation(this);
	}
}
