package mineplex.game.clans.clans.murder;

public class ClientMurder
{
	private boolean _isWeakling;
	private long _lastWeaklingUpdate;
	private long _ignoreWeaklingTime;
	private int _inventoryValue;
	
	public ClientMurder()
	{
	}
	
	public boolean isWeakling()
	{
		return _isWeakling && _ignoreWeaklingTime < System.currentTimeMillis();
	}
	
	public void setIsWeakling(boolean isWeakling)
	{
		_isWeakling = isWeakling;
		_lastWeaklingUpdate = System.currentTimeMillis();
	}
	
	public void setInventoryValue(int inventoryValue)
	{
		_inventoryValue = inventoryValue;
	}
	
	/**
	 * Disable weakling status for x milliseconds
	 * 
	 * @param milliseconds
	 */
	public void disableWeakling(long milliseconds)
	{
		if (_ignoreWeaklingTime > System.currentTimeMillis())
			_ignoreWeaklingTime += milliseconds;
		else
			_ignoreWeaklingTime = System.currentTimeMillis() + milliseconds;
	}
	
	public long getLastWeaklingUpdate()
	{
		return _lastWeaklingUpdate;
	}
	
	public int getInventoryValue()
	{
		return _inventoryValue;
	}
}
