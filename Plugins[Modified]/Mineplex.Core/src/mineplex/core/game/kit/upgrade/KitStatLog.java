package mineplex.core.game.kit.upgrade;

import java.util.HashSet;
import java.util.Set;

import mineplex.core.game.kit.GameKit;

public class KitStatLog
{

	private final Set<GameKit> _kitsUsed;
	private int _experienceEarned;

	public KitStatLog()
	{
		_kitsUsed = new HashSet<>(2);
	}

	public Set<GameKit> getKitsUsed()
	{
		return _kitsUsed;
	}

	public void setExperienceEarned(int experienceEarned)
	{
		_experienceEarned = experienceEarned;
	}

	public int getExperienceEarned()
	{
		return _experienceEarned;
	}
}
