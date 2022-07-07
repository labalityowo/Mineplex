package mineplex.core.mission;

import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.game.GameDisplay;

public interface Mission<T>
{

	int getId();

	String getName();

	String getDescription();

	GameDisplay[] getGames();

	MissionTrackerType getTrackerType();

	int getRequiredProgress();

	T getData();

	LevelReward[] getRewards();

	default boolean canProgress(GameDisplay display)
	{
		for (GameDisplay other : getGames())
		{
			if (other.equals(display))
			{
				return true;
			}
		}

		return getGames().length == 0 || getGames().length == GameDisplay.values().length;
	}

	default boolean validateData(T data)
	{
		return getData() == null || getData().equals(data);
	}

}
