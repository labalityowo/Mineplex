package mineplex.core.antispam;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.antispam.repository.AntiSpamRepository;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.status.ServerStatusManager;

@ReflectivelyCreateMiniPlugin
public class AntiSpamManager extends MiniPlugin
{

	private final String _pluginName;
	private final String _serverName;
	private final String _region;
	private final AntiSpamRepository _repository;

	private AntiSpamManager()
	{
		super("AntiSpam");

		_pluginName = getPlugin().getClass().getSimpleName();
		_repository = new AntiSpamRepository();

		ServerStatusManager serverStatusManager = require(ServerStatusManager.class);
		if (serverStatusManager != null)
		{
			_serverName = serverStatusManager.getCurrentServerName();
			_region = serverStatusManager.getRegion().name();
		}
		else
		{
			_serverName = "Unknown";
			_region = "Unknown";
		}
	}

	@EventHandler
	public void onChat(FormatPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String message = event.getMessage();
		ChatPayload payload = new ChatPayload(player.getName(), player.getUniqueId().toString(), _region, _serverName, message, System.currentTimeMillis());
		// Run our API call async to the chat message (prevents blocking chat message)
		AntiSpamApiResponse response = _repository.sendMessage(_pluginName, payload);

		if (response != null && response.isShadowMuted()) // can be null if the request times out
		{
			event.getRecipients().removeIf(other -> !player.equals(other));
		}
	}
}
