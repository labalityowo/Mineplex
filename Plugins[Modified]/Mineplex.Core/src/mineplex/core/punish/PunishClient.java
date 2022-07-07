package mineplex.core.punish;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.NautHashMap;

public class PunishClient
{
    private NautHashMap<Category, List<Punishment>> _punishments;

    public PunishClient()
    {
        _punishments = new NautHashMap<Category, List<Punishment>>();
    }
    
    public void AddPunishment(Category category, Punishment punishment)
    {
        if (!_punishments.containsKey(category))
        	_punishments.put(category, new ArrayList<Punishment>());
        
        _punishments.get(category).add(punishment);
    }

	public boolean IsBanned()
	{
		for (List<Punishment> punishments : _punishments.values())
		{
			for (Punishment punishment : punishments)
			{
				if (punishment.IsBanned())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean IsMuted()
	{
		for (List<Punishment> punishments : _punishments.values())
		{
			for (Punishment punishment : punishments)
			{
				if (punishment.IsMuted())
				{
					return true;
				}
			}
		}
		
		return false;	
	}

	public boolean IsReportBanned()
	{
		for (List<Punishment> punishments : _punishments.values())
		{
			for (Punishment punishment : punishments)
			{
				if (punishment.IsReportBanned())
				{
					return true;
				}
			}
		}

		return false;
	}

	public Punishment GetPunishment(PunishmentSentence sentence)
	{
		for (List<Punishment> punishments : _punishments.values())
		{
			for (Punishment punishment : punishments)
			{
				if (sentence == PunishmentSentence.Ban && punishment.IsBanned())
				{
					return punishment;
				}
				else if (sentence == PunishmentSentence.Mute && punishment.IsMuted())
				{
					return punishment;
				}
				else if (sentence == PunishmentSentence.ReportBan && punishment.IsReportBanned())
				{
					return  punishment;
				}
			}
		}
		
		return null;
	}

	public NautHashMap<Category, List<Punishment>> GetPunishments()
	{
		return _punishments;
	}
}
