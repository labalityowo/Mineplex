package mineplex.core.friend.ui;

import org.bukkit.entity.Player;

import mineplex.core.anvilMenu.player.PlayerNameMenu;
import mineplex.core.friend.FriendManager;

public class FriendAddPage extends PlayerNameMenu
{

	private final FriendManager _manager;

	FriendAddPage(FriendManager plugin, Player player)
	{
		super(plugin, plugin.getClientManager(), player);

		_manager = plugin;
	}

	@Override
	public void onSuccess(String name)
	{
		_player.closeInventory();
		_manager.addFriend(_player, name);
	}

	@Override
	public void onFail(String name)
	{

	}
}
