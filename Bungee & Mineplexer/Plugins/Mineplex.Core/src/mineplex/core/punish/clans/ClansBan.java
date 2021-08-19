package mineplex.core.punish.clans;

import java.sql.Timestamp;
import java.util.UUID;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;

/**
 * Stores the information about a blacklist in Clans.
 */
public class ClansBan
{
	private int _id;
	private String _reason;
	private String _admin;
	private Timestamp _banTime;
	private Timestamp _unbanTime;
	private boolean _permanent;
	private boolean _removed;
	private String _removeAdmin;
	private String _removeReason;
	private UUID _uuid;
	
	public ClansBan(int id, UUID uuid, String admin, String reason, Timestamp banTime, Timestamp unbanTime, boolean permanent, boolean removed, String removeAdmin, String removeReason)
	{
		_id = id;
		_uuid = uuid;
		_reason = reason;
		_admin = admin;
		_banTime = banTime;
		_unbanTime = unbanTime;
		_permanent = permanent;
		_removed = removed;
		_removeAdmin = removeAdmin;
		_removeReason = removeReason;
	}

	public int getId()
	{
		return _id;
	}
	
	public String getAdmin()
	{
		return _admin;
	}
	
	public String getReason()
	{
		return _reason;
	}
	
	public Timestamp getBanTime()
	{
		return _banTime;
	}
	
	public long getLength()
	{
		return _unbanTime.getTime() - _banTime.getTime();
	}
	
	public long getTimeLeft()
	{
		return Math.max(0, _unbanTime.getTime() - System.currentTimeMillis());
	}
	
	public Timestamp getUnbanTime()
	{
		return _unbanTime;
	}
	
	public boolean isPermanent()
	{
		return _permanent;
	}
	
	public String getBanTimeFormatted(boolean wording)
	{
		long time = getTimeLeft();
		
		return time == -1 ? F.time("permanently") : (wording ? "for " : "") + F.time(UtilTime.MakeStr(time));
	}
	
	public boolean isRemoved()
	{
		return _removed;
	}

	public boolean isActive()
	{
		return (isPermanent() || getTimeLeft() > 0) && !isRemoved();
	}

	public UUID getUUID()
	{
		return _uuid;
	}
	
	public String getRemoveAdmin()
	{
		return _removeAdmin;
	}
	
	public String getRemoveReason()
	{
		return _removeReason;
	}

	public void remove(String admin, String reason)
	{
		_removed = true;
		_removeAdmin = admin;
		_removeReason = reason;
	}
}