package mineplex.clanshub.salesannouncements;

import mineplex.serverdata.commands.CommandCallback;

public class SalesAnnouncementUpdateHandler implements CommandCallback<SalesAnnouncementUpdateCommand>
{
	private final SalesAnnouncementManager _manager;

	public SalesAnnouncementUpdateHandler(SalesAnnouncementManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(SalesAnnouncementUpdateCommand command)
	{
		if (_manager.getServer().equalsIgnoreCase(command.getFrom()))
		{
			return;
		}
		if (_manager.CLANS != command.isClans())
		{
			return;
		}
		_manager.handleRemoteUpdate(command.getId());
	}
}