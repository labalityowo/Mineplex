package nautilus.game.arcade.game.games.smash.perks.guardian;

import org.bukkit.entity.Player;

public class TargetLazerData
{

	private Player _attacker;
	private Player _target;
	private long _startTime;

	TargetLazerData(Player attacker)
	{
		_attacker = attacker;
	}

	public void setTarget(Player target)
	{
		_target = target;
		_startTime = System.currentTimeMillis();
	}

	public Player getAttacker()
	{
		return _attacker;
	}

	public Player getTarget()
	{
		return _target;
	}
	
	public long getTimeElapsed()
	{
		return System.currentTimeMillis() - _startTime;
	}
}
