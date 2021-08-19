package mineplex.core.mission;

import mineplex.core.achievement.leveling.rewards.LevelCurrencyReward;
import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.achievement.leveling.rewards.ScalableLevelReward;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.game.GameDisplay;

public class PlayerMission<T> implements Mission<T>
{

	public static final byte ACTIVE = 0, DISCARDED = -1, COMPLETE = 1;

	private final MissionContext<T> _context;

	private final String _description;
	private final MissionLength _length;
	private final int _x;
	private final int _y;

	private LevelReward[] _rewards;
	private int _currentProgress, _unsavedProgress;
	private boolean _rewarded, _discarded;

	PlayerMission(MissionContext<T> context, MissionLength length, int x, int y, int currentProgress, boolean fromContext)
	{
		_context = context;
		_length = length;

		_x = fromContext && x > 1 ? x * length.getxScale() : x;
		_y = y;

		if (x == 1 && y > 0)
		{
			_description = String.format(context.getDescription(), y);
		}
		else
		{
			_description = String.format(context.getDescription(), _x, y);
		}

		_currentProgress = currentProgress;
	}

	public void createRewards(double rankBonus)
	{
		if (_x != 1 || rankBonus > 0)
		{
			_rewards = new LevelReward[_context.getRewards().length];

			// This is only the case for things like the "Walk x blocks" missions.
			int x = _context.isScaleDownRewards() ? _x / 100 : _x;

			for (int i = 0; i < _rewards.length; i++)
			{
				LevelReward reward = _context.getRewards()[i];

				if (reward instanceof ScalableLevelReward)
				{
					double shardRankBonus = 1;

					if (reward instanceof LevelCurrencyReward && ((LevelCurrencyReward) reward).getType().equals(GlobalCurrency.TREASURE_SHARD))
					{
						shardRankBonus += rankBonus;
					}

					_rewards[i] = ((ScalableLevelReward) reward).cloneScalable(x * shardRankBonus);
				}
				else
				{
					_rewards[i] = reward;
				}
			}
		}
		else
		{
			_rewards = _context.getRewards();
		}
	}

	@Override
	public int getId()
	{
		return _context.getId();
	}

	@Override
	public String getName()
	{
		return _context.getName();
	}

	@Override
	public String getDescription()
	{
		return _description;
	}

	public MissionLength getLength()
	{
		return _length;
	}

	@Override
	public GameDisplay[] getGames()
	{
		return _context.getGames();
	}

	@Override
	public MissionTrackerType getTrackerType()
	{
		return _context.getTrackerType();
	}

	@Override
	public int getRequiredProgress()
	{
		return _x;
	}

	public boolean validateY(int y)
	{
		return y <= getY();
	}

	public int getY()
	{
		return _y;
	}

	@Override
	public T getData()
	{
		return _context.getData();
	}

	@Override
	public LevelReward[] getRewards()
	{
		return _rewards;
	}

	public int getCurrentProgress()
	{
		return _currentProgress;
	}

	public int getUnsavedProgress()
	{
		return _unsavedProgress;
	}

	public void incrementProgress(int progress)
	{
		_unsavedProgress += progress;
	}

	public boolean isComplete()
	{
		return _currentProgress >= getRequiredProgress();
	}

	public int saveProgress()
	{
		int unsaved = _unsavedProgress;

		_currentProgress += unsaved;
		clearUnsavedProgress();

		return unsaved;
	}

	public void clearUnsavedProgress()
	{
		_unsavedProgress = 0;
	}

	public void reward()
	{
		_rewarded = true;
	}

	public boolean hasRewarded()
	{
		return _rewarded;
	}

	public void discard()
	{
		_discarded = true;
	}

	public boolean isDiscarded()
	{
		return _discarded;
	}
}
