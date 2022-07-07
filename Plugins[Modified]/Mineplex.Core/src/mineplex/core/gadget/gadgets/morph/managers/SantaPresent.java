package mineplex.core.gadget.gadgets.morph.managers;

public class SantaPresent
{

	public enum PresentType
	{
		PRESENT,
		COAL
	}

	private final String _thrower;
	private final PresentType _presentType;
	private final int _ammo;

	public SantaPresent(String thrower, PresentType presentType, int ammo)
	{
		_thrower = thrower;
		_presentType = presentType;
		_ammo = ammo;
	}

	public String getThrower()
	{
		return _thrower;
	}

	public PresentType getPresentType()
	{
		return _presentType;
	}

	public int getAmmo()
	{
		return _ammo;
	}

}
