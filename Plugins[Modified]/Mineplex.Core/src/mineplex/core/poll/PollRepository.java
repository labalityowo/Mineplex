package mineplex.core.poll;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

/**
 * Created by Shaun on 8/16/2014.
 */
public class PollRepository extends RepositoryBase
{
	private static String CREATE_POLL_TABLE = "CREATE TABLE IF NOT EXISTS polls (id INT NOT NULL AUTO_INCREMENT, enabled BIT(1), question VARCHAR(256) NOT NULL, answerA VARCHAR(256) NOT NULL, answerB VARCHAR(256), answerC VARCHAR(256), answerD VARCHAR(256), coinReward INT NOT NULL, displayType INT DEFAULT 0 NOT NULL, PRIMARY KEY (id));";
	private static String CREATE_RELATION_TABLE = "CREATE TABLE IF NOT EXISTS accountPolls (id INT NOT NULL AUTO_INCREMENT, accountId INT NOT NULL, pollId INT NOT NULL, value TINYINT(1) NOT NULL, PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (pollId) REFERENCES polls(id), UNIQUE INDEX accountPollIndex (accountId, pollId));";

	private static String RETRIEVE_POLLS = "SELECT id, enabled, question, answerA, answerB, answerC, answerD, coinReward, displayType FROM polls ORDER BY coinReward DESC";
	private static String RETRIEVE_PLAYER_DATA = "SELECT pollId, value FROM accountPolls INNER JOIN accounts ON accountPolls.accountId = accounts.id WHERE accounts.uuid = ?;";
	private static String INSERT_POLL_ANSWER = "INSERT INTO accountPolls (accountId, pollId, value) SELECT accounts.id, ?, ? FROM accounts WHERE accounts.uuid = ?;";
	private static String RETRIEVE_POLL_STATS = "SELECT value, COUNT(*) FROM accountPolls WHERE pollId=? GROUP BY value;";

	public PollRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public List<Poll> retrievePolls()
	{
		final List<Poll> polls = new ArrayList<>();

		executeQuery(RETRIEVE_POLLS, resultSet ->
		{
			while (resultSet.next())
			{
				int pollId = resultSet.getInt(1);
				boolean enabled = resultSet.getBoolean(2);
				String question = resultSet.getString(3);
				String answerA = resultSet.getString(4);
				String answerB = resultSet.getString(5);
				String answerC = resultSet.getString(6);
				String answerD = resultSet.getString(7);
				int coinReward = resultSet.getInt(8);
				DisplayType displayType;

				switch(resultSet.getInt(9))
				{
					case 1:
						displayType = DisplayType.RANKED;
						break;
					case 2:
						displayType = DisplayType.NOT_RANKED;
						break;
					default:
						displayType = DisplayType.ALL;
				}

				Poll poll = new Poll(pollId, enabled, coinReward, question, answerA, answerB, answerC, answerD, displayType);

				polls.add(poll);
			}
		});

		return polls;
	}

	public PlayerPollData loadPollData(ResultSet resultSet) throws SQLException
	{
		PlayerPollData pollData = new PlayerPollData();

		while (resultSet.next())
		{
			pollData.addAnswer(resultSet.getInt(1), resultSet.getInt(2));
		}

		pollData.Loaded = true;

		return pollData;
	}

//	public PlayerPollData loadPollData(UUID uuid)
//	{
//		final PlayerPollData pollData = new PlayerPollData();
//
//		executeQuery(RETRIEVE_PLAYER_DATA, new ResultSetCallable()
//		{
//			public void processResultSet(ResultSet resultSet) throws SQLException
//			{
//				while (resultSet.next())
//				{
//					pollData.addAnswer(resultSet.getInt(1), resultSet.getInt(2));
//				}
//			}
//		}, new ColumnVarChar("uuid", 100, uuid.toString()));
//
//		pollData.Loaded = true;
//
//		return pollData;
//	}

	public boolean addPollAnswer(UUID uuid, int pollId, int answer)
	{
		int update = executeUpdate(INSERT_POLL_ANSWER, new ColumnInt("pollId", pollId), new ColumnInt("answer", answer), new ColumnVarChar("uuid", 100, uuid.toString()));
		return update == 1;
	}

	public PollStats getPollStats(final int pollId)
	{
		final PollStats stats = new PollStats();

		executeQuery(RETRIEVE_POLL_STATS, resultSet ->
		{
			int aCount = 0;
			int bCount = 0;
			int cCount = 0;
			int dCount = 0;

			while (resultSet.next())
			{
				int responseCount = resultSet.getInt(2);
				switch (resultSet.getInt(1))
				{
					case 1:
						aCount = responseCount;
						break;
					case 2:
						bCount = responseCount;
						break;
					case 3:
						cCount = responseCount;
						break;
					case 4:
						dCount = responseCount;
						break;
				}
			}

			stats.setACount(aCount);
			stats.setBCount(bCount);
			stats.setCCount(cCount);
			stats.setDCount(dCount);
		}, new ColumnInt("pollId", pollId));

		return stats;
	}
}