package mineplex.core.bonuses;

import java.sql.Date;
import java.sql.Timestamp;

import mineplex.core.hologram.Hologram;

public class BonusClientData
{
	private Hologram _hologram;

	private int _accountId;
	private Timestamp _dailyTime;
	private Timestamp _clansDailyTime;
	private Date _rankTime;
	private Date _voteTime;
	private Date _clansVoteTime;
	private int _dailyStreak;
	private int _maxDailyStreak;
	private int _voteStreak;
	private int _maxVoteStreak;
	private int _tickets;

	public BonusClientData()
	{
		_accountId = -1;
	}

	public void setAccountId(Integer value)
	{
		_accountId = value;
	}

	public Integer getAccountId()
	{
		return _accountId;
	}

	public void setDailyTime(Timestamp value)
	{
		_dailyTime = value;
	}

	public Timestamp getDailyTime()
	{
		return _dailyTime;
	}
	
	public void setClansDailyTime(Timestamp value)
	{
		_clansDailyTime = value;
	}

	public Timestamp getClansDailyTime()
	{
		return _clansDailyTime;
	}

	public void setRankTime(Date value)
	{
		_rankTime = value;
	}

	public Date getRankTime()
	{
		return _rankTime;
	}

	public void setVoteTime(Date value)
	{
		_voteTime = value;
	}

	public Date getVoteTime()
	{
		return _voteTime;
	}
	
	public void setClansVoteTime(Date value)
	{
		_clansVoteTime = value;
	}

	public Date getClansVoteTime()
	{
		return _clansVoteTime;
	}

	public void setDailyStreak(Integer value)
	{
		_dailyStreak = value;
	}

	public Integer getDailyStreak()
	{
		return _dailyStreak;
	}

	public void setMaxDailyStreak(Integer value)
	{
		_maxDailyStreak = value;
	}

	public Integer getMaxDailyStreak()
	{
		return _maxDailyStreak;
	}

	public void setVoteStreak(Integer value)
	{
		_voteStreak = value;
	}

	public Integer getVoteStreak()
	{
		return _voteStreak;
	}

	public void setMaxVoteStreak(Integer value)
	{
		_maxVoteStreak = value;
	}

	public Integer getMaxVoteStreak()
	{
		return _maxVoteStreak;
	}

	public void setTickets(Integer value)
	{
		_tickets = value;
	}

	public Integer getTickets()
	{
		return _tickets;
	}

	public Hologram getHologram()
	{
		return _hologram;
	}

	public void setHologram(Hologram hologram)
	{
		_hologram = hologram;
	}
}