package mineplex.clanshub.salesannouncements;

import mineplex.serverdata.commands.ServerCommand;

public class SalesAnnouncementUpdateCommand extends ServerCommand
{
	private Integer _id;
	private String _from;
	private boolean _clans;

	public SalesAnnouncementUpdateCommand(Integer id, String from, boolean clans)
	{
		_id = id;
		_from = from;
		_clans = clans;
	}

	public Integer getId()
	{
		return _id;
	}

	public String getFrom()
	{
		return _from;
	}
	
	public boolean isClans()
	{
		return _clans;
	}
}