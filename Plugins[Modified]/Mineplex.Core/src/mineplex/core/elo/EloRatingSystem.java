package mineplex.core.elo;

public class EloRatingSystem
{
	private final static int DEFAULT_KFACTOR = 25;

	public final static double WIN = 1.0;
	public final static double DRAW = 0.5;
	public final static double LOSS = 0.0;

	private KFactor[] _kFactors = {};

	public EloRatingSystem(KFactor...kFactors)
	{
		_kFactors = kFactors;
	}

	public int getNewRating(int rating, int opponentRating, GameResult result)
	{
		switch (result)
		{
			case Win:
				return getNewRating(rating, opponentRating, WIN);
			case Loss:
				return getNewRating(rating, opponentRating, LOSS);
			case Draw:
				return getNewRating(rating, opponentRating, DRAW);
		}
		
		return -1;
	}

	/**
	 * Get new rating.
	 * 
	 * @param rating
	 *            Rating of either the current player or the average of the
	 *            current team.
	 * @param opponentRating
	 *            Rating of either the opponent player or the average of the
	 *            opponent team or teams.
	 * @param score
	 *            Score: 0=Loss 0.5=Draw 1.0=Win
	 * @return the new rating
	 */
	public int getNewRating(int rating, int opponentRating, double score)
	{
		double kFactor = getKFactor(rating);
		double expectedScore = getExpectedScore(rating, opponentRating);
		int newRating = calculateNewRating(rating, score, expectedScore, kFactor);

		return newRating;
	}

	private int calculateNewRating(int oldRating, double score, double expectedScore, double kFactor)
	{
		return oldRating + (int) (kFactor * (score - expectedScore));
	}

	double getKFactor(int rating)
	{
		for (int i = 0; i < _kFactors.length; i++)
		{
			if (rating >= _kFactors[i].getStartIndex() && rating <= _kFactors[i].getEndIndex())
			{
				return _kFactors[i].value;
			}
		}
		
		return DEFAULT_KFACTOR;
	}

	private double getExpectedScore(int rating, int opponentRating)
	{
		return 1.0 / (1.0 + Math.pow(10.0, ((double) (opponentRating - rating) / 400.0)));
	}
}
