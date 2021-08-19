package mineplex.core.antispam;

/**
 * @author Shaun Bennett
 */
public class ChatPayload
{
	private String _playerName;
	private String _uuid;
	private String _region;
	private String _server;
	private String _message;
	private long _time;

	public ChatPayload(String playerName, String uuid, String region, String server, String message, long time)
	{
		_playerName = playerName;
		_uuid = uuid;
		_region = region;
		_server = server;
		_message = message;
		_time = time;
	}

	public String getPlayerName()
	{
		return _playerName;
	}

	public void setPlayerName(String playerName)
	{
		_playerName = playerName;
	}

	public String getUuid()
	{
		return _uuid;
	}

	public void setUuid(String uuid)
	{
		_uuid = uuid;
	}

	public String getMessage()
	{
		return _message;
	}

	public void setMessage(String message)
	{
		_message = message;
	}

	public String getServer()
	{
		return _server;
	}

	public void setServer(String server)
	{
		_server = server;
	}

	public long getTime()
	{
		return _time;
	}

	public void setTime(long time)
	{
		_time = time;
	}

	public String getRegion()
	{
		return _region;
	}

	public void setRegion(String region)
	{
		_region = region;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChatPayload payload = (ChatPayload) o;

		if (_time != payload._time) return false;
		if (!_playerName.equals(payload._playerName)) return false;
		if (!_uuid.equals(payload._uuid)) return false;
		if (!_region.equals(payload._region)) return false;
		if (!_server.equals(payload._server)) return false;
		return _message.equals(payload._message);

	}

	@Override
	public int hashCode()
	{
		int result = _playerName.hashCode();
		result = 31 * result + _uuid.hashCode();
		result = 31 * result + _region.hashCode();
		result = 31 * result + _server.hashCode();
		result = 31 * result + _message.hashCode();
		result = 31 * result + (int) (_time ^ (_time >>> 32));
		return result;
	}

	@Override
	public String toString()
	{
		return "ChatPayload{" +
				"_playerName='" + _playerName + '\'' +
				", _uuid='" + _uuid + '\'' +
				", _region='" + _region + '\'' +
				", _server='" + _server + '\'' +
				", _message='" + _message + '\'' +
				", _time=" + _time +
				'}';
	}
}