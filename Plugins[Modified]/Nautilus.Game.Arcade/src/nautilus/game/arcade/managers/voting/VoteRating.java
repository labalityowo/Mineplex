package nautilus.game.arcade.managers.voting;

public class VoteRating
{

	private final int _gameId;
	private final String _mapName;
	private final int _rating;

	public VoteRating(int gameId, String mapName, int rating)
	{
		_gameId = gameId;
		_mapName = mapName;
		_rating = rating;
	}

	public int getGameId()
	{
		return _gameId;
	}

	public String getMapName()
	{
		return _mapName;
	}

	public int getRating()
	{
		return _rating;
	}
}
