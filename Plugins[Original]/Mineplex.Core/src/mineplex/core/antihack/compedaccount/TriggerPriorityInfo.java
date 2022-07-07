package mineplex.core.antihack.compedaccount;

public class TriggerPriorityInfo
{
	private final PlayerInfo _target;
	private final PriorityCause _cause;

	public TriggerPriorityInfo(PlayerInfo target, PriorityCause cause)
	{
		_target = target;
		_cause = cause;
	}

	public PlayerInfo getTarget()
	{
		return _target;
	}

	public PriorityCause getCause()
	{
		return _cause;
	}
}
