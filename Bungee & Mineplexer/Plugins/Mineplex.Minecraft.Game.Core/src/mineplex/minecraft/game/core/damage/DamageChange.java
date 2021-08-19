package mineplex.minecraft.game.core.damage;

public class DamageChange 
{

	private final String _source;
	private final String _reason;
	private final double _modifier;
	private final boolean _useReason;
	
	DamageChange(String source, String reason, double modifier, boolean useReason)
	{
		_source = source;
		_reason = reason;
		_modifier = modifier;
		_useReason = useReason;
	}
	
	public String GetSource()
	{
		return _source;
	}
	
	public String GetReason()
	{
		return _reason;
	}
	
	public double GetDamage()
	{
		return _modifier;
	}
	
	public boolean UseReason()
	{
		return _useReason;
	}
}
