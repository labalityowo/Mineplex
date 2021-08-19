package nautilus.game.minekart.kart.condition;

public class ConditionData 
{
	private ConditionType _type;
	private long _start;
	private long _duration;
	
	public ConditionData(ConditionType type, long duration)
	{
		_type = type;
		_start = System.currentTimeMillis();
		_duration = duration;
	}
	
	public boolean IsExpired()
	{
		return System.currentTimeMillis() > _start + _duration;
	}
	
	public boolean IsCondition(ConditionType type)
	{
		return _type == type;
	}

	public void Expire() 
	{
		_start = 0;
	}
}
