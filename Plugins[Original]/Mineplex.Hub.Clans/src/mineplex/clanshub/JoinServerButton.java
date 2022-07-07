package mineplex.clanshub;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.clanshub.queue.HubQueueManager;
import mineplex.core.Managers;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;

/**
 * GUI button to select a server from a display
 */
public class JoinServerButton implements IButton
{
	private ShopPageBase<?, ?> _page;
	private ServerInfo _serverInfo;
	private final HubQueueManager _queue = Managers.require(HubQueueManager.class);
	
	public JoinServerButton(ShopPageBase<?, ?> page, ServerInfo serverInfo)
	{
		_page = page;
		_serverInfo = serverInfo;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		selectServer(player, _serverInfo);
	}
	
	/**
	 * Selects a server to send a player to
	 * @param player The player to send
	 * @param serverInfo The server to send the player to
	 */
	public void selectServer(Player player, ServerInfo serverInfo)
	{
		if (serverInfo != null)
		{
			if (_queue.Get(player).TargetServer == null || !_queue.Get(player).TargetServer.equals(serverInfo.Name))
			{
				_queue.attemptEnterQueue(player, _queue.getData(serverInfo));
			}
			else
			{
				_queue.leaveQueue(player, true);
			}
		}
		else
		{
			_page.playDenySound(player);
		}
	}
}