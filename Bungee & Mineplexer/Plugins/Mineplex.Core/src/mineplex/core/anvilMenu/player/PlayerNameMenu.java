package mineplex.core.anvilMenu.player;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.anvilMenu.PlayerInputActionMenu;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;

/**
 * A wrapped menu that handles looking for players specifically.
 */
public abstract class PlayerNameMenu extends PlayerInputActionMenu
{

	protected final CoreClientManager _clientManager;

	public PlayerNameMenu(MiniPlugin plugin, CoreClientManager clientManager, Player player)
	{
		super(plugin, player);
		_clientManager = clientManager;
	}

	public abstract void onSuccess(String name);

	public abstract void onFail(String name);

	@Override
	public void inputReceived(String name)
	{
		Player onlinePlayer = UtilPlayer.searchExact(name);

		if (onlinePlayer != null)
		{
			onSuccess(onlinePlayer.getName());
			return;
		}

		_clientManager.checkPlayerName(_player, _itemName, result ->
		{
			_plugin.runSync(() ->
			{
				_searching = false;

				if (result != null)
				{
					onSuccess(result);
				}
				else
				{
					onFail(name);
					_currentInventory.setItem(2, new ItemBuilder(Material.PAPER)
							.setTitle(C.cYellow + "0" + C.cGray + " matches for [" + C.cYellow + name + C.cGray + "]")
							.build());
					_player.playSound(_player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
				}
			});
		});
	}
}
