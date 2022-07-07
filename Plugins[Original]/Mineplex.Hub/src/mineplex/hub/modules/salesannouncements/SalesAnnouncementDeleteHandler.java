package mineplex.hub.modules.salesannouncements;

import mineplex.serverdata.commands.CommandCallback;

public class SalesAnnouncementDeleteHandler implements CommandCallback<SalesAnnouncementDeleteCommand>
{
	private final SalesAnnouncementManager _manager;

	public SalesAnnouncementDeleteHandler(SalesAnnouncementManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(SalesAnnouncementDeleteCommand command)
	{
		if (_manager.getServer().equalsIgnoreCase(command.getFrom()))
		{
			return;
		}
		if (_manager.CLANS != command.isClans())
		{
			return;
		}
		_manager.handleRemoteDeletion(command.getId());
	}
}