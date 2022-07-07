package mineplex.core.punish;

import mineplex.core.common.util.TimeSpan;

public class Punishment
{
	private int _id;
	private PunishmentSentence _punishmentType;
	private Category _category;
	private String _reason;
	private String _admin;
	private double _hours;
	private int _severity;
	private long _time;
	private boolean _active;
	private boolean _removed;
	private String _removeAdmin;
	private String _removeReason;
	
	public Punishment(int id, PunishmentSentence punishmentType, Category category, String reason, String admin, double hours, int severity, long time, boolean active, boolean removed, String removeAdmin, String removeReason)
	{
		_id = id;
		_punishmentType = punishmentType;
		_category = category;
		_reason = reason;
		_admin = admin;
		_hours = hours;
		_severity = severity;
		_time = time;
		_active = active;
		_removed = removed;
		_removeAdmin = removeAdmin;
		_removeReason = removeReason;
	}
	
	public int GetPunishmentId()
	{
		return _id;
	}
	
	public PunishmentSentence GetPunishmentType()
	{
		return _punishmentType;
	}
	
	public Category GetCategory()
	{
		return _category;
	}
	
	public String GetReason()
	{
		return _reason;
	}
	
	public String GetAdmin()
	{
		return _admin;
	}
	
	public double GetHours()
	{
		return _hours;
	}

	public int GetSeverity()
	{
		return _severity;
	}
	
	public long GetTime()
	{
		return _time;
	}
	
	public boolean GetActive()
	{
		return _active;
	}
	
	public boolean GetRemoved()
	{
		return _removed;
	}
	
	public void Remove(String admin, String reason)
	{
		_removed = true;
		_removeAdmin = admin;
		_removeReason = reason;
	}
	
	public String GetRemoveReason()
	{
		return _removeReason;
	}

	public boolean IsBanned()
	{
		return _punishmentType == PunishmentSentence.Ban && (GetRemaining() > 0 || _hours < 0) && _active;
	}
	
	public boolean IsMuted()
	{
		return _punishmentType == PunishmentSentence.Mute && (GetRemaining() > 0 || _hours < 0) && _active;
	}

	public boolean IsReportBanned()
	{
		return _punishmentType == PunishmentSentence.ReportBan && (GetRemaining() > 0 || _hours < 0) && _active;
	}

	public long GetRemaining()
	{
		return _hours < 0 ? -1 : (long) ((_time + (TimeSpan.HOUR * _hours)) - System.currentTimeMillis());
	}

	public String GetRemoveAdmin()
	{
		return _removeAdmin;
	}
}
