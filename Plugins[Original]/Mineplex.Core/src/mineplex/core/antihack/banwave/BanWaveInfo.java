package mineplex.core.antihack.banwave;

import java.util.Objects;

public class BanWaveInfo
{
	/**
	 * The account id for this BanWaveInfo
	 */
	private int _accountId;

	/**
	 * The time in milliseconds at which to ban this user
	 */
	private long _timeToBan;

	/**
	 * The hack type
	 */
	private String _hackType;

	/**
	 * The ban message
	 */
	private String _message;

	/**
	 * The violation level
	 */
	private int _vl;

	/**
	 * The server on which the user was flagged
	 */
	private String _server;

	public int getAccountId()
	{
		return _accountId;
	}

	public void setAccountId(int accountId)
	{
		_accountId = accountId;
	}

	public long getTimeToBan()
	{
		return _timeToBan;
	}

	public void setTimeToBan(long timeToBan)
	{
		_timeToBan = timeToBan;
	}

	public String getHackType()
	{
		return _hackType;
	}

	public void setHackType(String hackType)
	{
		_hackType = hackType;
	}

	public String getMessage()
	{
		return _message;
	}

	public void setMessage(String message)
	{
		_message = message;
	}

	public int getVl()
	{
		return _vl;
	}

	public void setVl(int vl)
	{
		_vl = vl;
	}

	public String getServer()
	{
		return _server;
	}

	public void setServer(String server)
	{
		_server = server;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BanWaveInfo that = (BanWaveInfo) o;

		return Objects.equals(_accountId, that._accountId)
			&& Objects.equals(_timeToBan, that._timeToBan)
			&& Objects.equals(_vl, that._vl)
			&& Objects.equals(_hackType, that._hackType)
			&& Objects.equals(_message, that._message)
			&& Objects.equals(_server, that._server);

	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_accountId, _timeToBan, _hackType, _message, _vl, _server);
	}
}