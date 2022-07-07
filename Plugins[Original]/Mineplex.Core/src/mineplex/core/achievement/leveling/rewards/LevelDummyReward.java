package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

public class LevelDummyReward implements LevelReward
{

	private final String _description;

	public LevelDummyReward(String description)
	{
		_description = description;
	}

	@Override
	public void claim(Player player)
	{

	}

	@Override
	public String getDescription()
	{
		return _description;
	}
}
