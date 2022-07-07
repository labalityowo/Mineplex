package mineplex.core.communities.redis;

import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunitySetting;
import mineplex.serverdata.commands.CommandCallback;
import mineplex.serverdata.commands.ServerCommand;

public class CommunityUpdateSettingHandler implements CommandCallback
{
	private CommunityManager _manager;

	public CommunityUpdateSettingHandler(CommunityManager manager)
	{
		_manager = manager;
	}

	@Override
	public void run(ServerCommand command)
	{
		if (command instanceof CommunityUpdateSetting)
		{
			CommunityUpdateSetting update = ((CommunityUpdateSetting) command);
			Integer id = update.getCommunityId();
			CommunitySetting setting = CommunitySetting.valueOf(update.getSetting());
			String newValue = update.getNewValue();
			
			_manager.handleCommunitySettingUpdate(id, update.getSender(), setting, newValue);
		}
	}
}