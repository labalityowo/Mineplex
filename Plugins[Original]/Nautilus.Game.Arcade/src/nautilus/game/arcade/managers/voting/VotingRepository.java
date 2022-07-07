package nautilus.game.arcade.managers.voting;

import java.util.ArrayList;
import java.util.List;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class VotingRepository extends RepositoryBase
{

	private static final String ADD_MAP = "INSERT INTO mapVotes VALUES (?,?,?);";
	private static final String INCREMENT_MAP = "UPDATE mapVotes SET rating=rating+? WHERE gameId=? AND mapName=?;";
	private static final String GET_MAPS = "SELECT mapName, rating FROM mapVotes WHERE gameId=?";

	VotingRepository()
	{
		super(DBPool.getAccount());
	}

	public void updateRatings(List<VoteRating> ratings)
	{
		ratings.forEach(voteRating ->
		{
			ColumnInt rating = new ColumnInt("rating", voteRating.getRating());
			ColumnInt gameId = new ColumnInt("gameId", voteRating.getGameId());
			ColumnVarChar mapName = new ColumnVarChar("mapName", 64, voteRating.getMapName());

			if (executeUpdate(INCREMENT_MAP, rating, gameId, mapName) == 0)
			{
				executeInsert(ADD_MAP, null, gameId, mapName, rating);
			}
		});
	}

	public List<VoteRating> getRatings(int gameId)
	{
		List<VoteRating> ratings = new ArrayList<>();

		executeQuery(GET_MAPS, resultSet ->
		{
			while (resultSet.next())
			{
				ratings.add(new VoteRating(gameId, resultSet.getString("mapName"), resultSet.getInt("rating")));
			}
		}, new ColumnInt("gameId", gameId));

		return ratings;
	}
}
