package nautilus.game.pvp.modules.Fishing;

public class FishData
{
	private double _pounds;
	private String _catcher;
	
	public FishData(double size, String catcher)
	{
		_pounds = size;
		_catcher = catcher;
	}

	public double GetPounds()
	{
		return _pounds;
	}
	
	public String GetCatcher()
	{
		return _catcher;
	}
}
