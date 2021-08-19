package mineplex.core.punish.clans;

import java.util.List;
import java.util.UUID;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;

/**
 * A client representing a player and a List of their Clans blacklists
 */
public class ClansBanClient
{
	public final UUID _uuid;
	public final List<ClansBan> _bans;
	
	public ClansBanClient(UUID uuid, List<ClansBan> bans)
	{
		_uuid = uuid;
		_bans = bans;
		sortBans();
	}

	public boolean isBanned()
	{
		for (ClansBan ban : _bans)
		{
			if (ban.isActive())
			{
				return true;
			}
		}
		
		return false;
	}

	public long getBanTime()
	{
		long time = 0;
		
		for (ClansBan ban : _bans)
		{
			if (!ban.isActive())
			{
				continue;
			}
			
			if (ban.isPermanent())
			{
				return -1;
			}
			
			time += ban.getTimeLeft();
		}
		
		return time;
	}
	
	public String getBanTimeFormatted()
	{
		long time = getBanTime();
		
		return time == -1 ? F.time("permanently") : "for " + F.time(UtilTime.MakeStr(time));
	}

	public ClansBan getLongestBan()
	{
		ClansBan longest = null;
		
		for (ClansBan ban : _bans)
		{
			if (!ban.isActive())
			{
				continue;
			}
			
			if (longest == null)
			{
				longest = ban;
				continue;
			}
			
			if (ban.getTimeLeft() > longest.getTimeLeft() || ban.isPermanent())
			{
				longest = ban;
			}
		}
		
		return longest;
	}
	
	public void sortBans()
	{
		_bans.sort((b1, b2) ->
		{
			if (b1.isActive() && !b2.isActive())
			{
				return -1;
			}
			if (b2.isActive() && !b1.isActive())
			{
				return 1;
			}
			if ((b1.isActive() && b1.isPermanent()) && !(b2.isActive() && b2.isPermanent()))
			{
				return -1;
			}
			if ((b2.isActive() && b2.isPermanent()) && !(b1.isActive() && b1.isPermanent()))
			{
				return 1;
			}
			return b1.getBanTime().compareTo(b2.getBanTime());
		});
	}
}