package mineplex.core.mission;

import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.common.util.UtilMath;
import mineplex.core.game.GameDisplay;

public class MissionContext<T> implements Mission<T>
{

	private final int _id;
	private final String _name;
	private final String _description;
	private final GameDisplay[] _games;
	private final MissionTrackerType _trackerType;
	private final T _data;
	private final LevelReward[] _rewards;
	private final boolean _eventMission;
	private final boolean _scaleDownRewards;

	/*
		To explain what these x and y values mean.
		When a mission is generated for a player it picks a random x and y value between the set values.
		x is used for the required progress of the mission, so for example "Kill x players".
		y is used for as a requirement for progressing the mission and the tracker much provide a y less than or equal to the
		y that the mission has set, for example "Kill x players within y seconds of each other".
		x is used to calculate the rewards for a mission.
		y values do not affect the rewards.
	 */
	private final int _minX, _maxX;
	private final int _minY, _maxY;

	private MissionContext(int id, String name, String description, GameDisplay[] games, MissionTrackerType trackerType, T data, LevelReward[] rewards, int minX, int maxX, int minY, int maxY, boolean eventMission, boolean scaleDownRewards)
	{
		_id = id;
		_name = name;
		_description = description;
		_games = games;
		_trackerType = trackerType;
		_data = data;
		_rewards = rewards;
		_minX = minX;
		_maxX = maxX;
		_minY = minY;
		_maxY = maxY;
		_eventMission = eventMission;
		_scaleDownRewards = scaleDownRewards;
	}

	@Override
	public int getId()
	{
		return _id;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public String getDescription()
	{
		return _description;
	}

	@Override
	public GameDisplay[] getGames()
	{
		return _games;
	}

	@Override
	public int getRequiredProgress()
	{
		return 0;
	}

	@Override
	public MissionTrackerType getTrackerType()
	{
		return _trackerType;
	}

	@Override
	public T getData()
	{
		return _data;
	}

	@Override
	public LevelReward[] getRewards()
	{
		return _rewards;
	}

	public int getRandomX()
	{
		return _minX == _maxX ? _minX : UtilMath.rRange(_minX, _maxX);
	}

	public int getRandomY()
	{
		return _minY == _maxY ? _minY : UtilMath.rRange(_minY, _maxY);
	}

	public boolean isEventMission()
	{
		return _eventMission;
	}

	public boolean isScaleDownRewards()
	{
		return _scaleDownRewards;
	}

	public static <T> MissionBuilder<T> newBuilder(MissionManager manager, int id)
	{
		return new MissionBuilder<>(manager, id);
	}

	public static final class MissionBuilder<T>
	{

		private final MissionManager _manager;
		private final int _id;

		private String _name;
		private String _description;
		private GameDisplay[] _games;
		private MissionTrackerType _trackerType;
		private T _data;
		private LevelReward[] _rewards;

		private int _minX = 1, _maxX = 1;
		private int _minY, _maxY;

		private boolean _eventMission;
		private boolean _scaleDownRewards;

		private MissionBuilder(MissionManager manager, int id)
		{
			_manager = manager;
			_id = id;
		}

		public MissionBuilder<T> name(String name)
		{
			_name = name;
			return this;
		}

		public MissionBuilder<T> description(String description)
		{
			_description = description;
			return this;
		}

		public MissionBuilder<T> games(GameDisplay... games)
		{
			_games = games;
			return this;
		}

		public MissionBuilder<T> tracker(MissionTrackerType tracker)
		{
			_trackerType = tracker;
			return this;
		}

		public MissionBuilder<T> trackerData(T data)
		{
			_data = data;
			return this;
		}

		public MissionBuilder<T> rewards(LevelReward... rewards)
		{
			_rewards = rewards;
			return this;
		}

		public MissionBuilder<T> xRange(int min, int max)
		{
			_minX = min;
			_maxX = max;
			return this;
		}

		public MissionBuilder<T> yRange(int min, int max)
		{
			_minY = min;
			_maxY = max;
			return this;
		}

		public MissionBuilder<T> event()
		{
			_eventMission = true;
			return this;
		}

		public MissionBuilder<T> scaleDownRewards()
		{
			_scaleDownRewards = true;
			return this;
		}

		public void build()
		{
			if (_name == null || _name.isEmpty())
			{
				throw new IllegalStateException("The name cannot be null or empty! Mission [" + _id + "]");
			}
			else if (_description == null || _description.isEmpty())
			{
				throw new IllegalStateException("The description cannot be null or empty! Mission [" + _id + "]");
			}
			else if (_games == null)
			{
				_games = new GameDisplay[0];
			}
			else if (_trackerType == null)
			{
				throw new IllegalStateException("The tracker type cannot be null! Mission [" + _id + "]");
			}
			else if (_rewards == null || _rewards.length == 0)
			{
				throw new IllegalStateException("The rewards cannot be null or empty! Mission [" + _id + "]");
			}
			else if (_minX < 0 || _maxX < 0 || _minY < 0 || _maxY < 0 || _minX > _maxX || _minY > _maxY)
			{
				throw new IllegalStateException("The scales or y values are invalid! Mission [" + _id + "]");
			}

			_manager.addMission(new MissionContext<>(_id, _name, _description, _games, _trackerType, _data, _rewards, _minX, _maxX, _minY, _maxY, _eventMission, _scaleDownRewards));
		}
	}
}
