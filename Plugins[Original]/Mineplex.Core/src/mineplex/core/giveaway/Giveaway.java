package mineplex.core.giveaway;

public class Giveaway
{
	private int _id;
	private String _name;
	private String _prettyName;
	private String _header;
	private String _message;
	private boolean _notifyNetwork;
	private int _notifyCooldown;

	public Giveaway(int id, String name, String prettyName, String header, String message, boolean notifyNetwork, int notifyCooldown)
	{
		_id = id;
		_name = name;
		_prettyName = prettyName;
		_header = header;
		_message = message;
		_notifyNetwork = notifyNetwork;
		_notifyCooldown = notifyCooldown;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public String getPrettyName()
	{
		return _prettyName;
	}

	public String getMessage()
	{
		return _message;
	}

	public String getHeader()
	{
		return _header;
	}

	public boolean isNotifyNetwork()
	{
		return _notifyNetwork;
	}

	public int getNotifyCooldown()
	{
		return _notifyCooldown;
	}
}
