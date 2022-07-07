package nautilus.game.arcade.gui.privateServer.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public abstract class PlayerPage extends BasePage
{
	private List<String> _players;
	private boolean useOtherList;

	public PlayerPage(ArcadeManager plugin, PrivateServerShop shop, String pageName, Player player)
	{
		super(plugin, shop, pageName, player);
		useOtherList = false;
	}

	public PlayerPage(ArcadeManager plugin, PrivateServerShop shop, String pageName, Player player, List<String> players)
	{
		super(plugin, shop, pageName, player);
		useOtherList = true;
		_players = players;
	}

	@Override
	protected void buildPage()
	{
		addBackButton(4);

		List<Player> players = new ArrayList<>();
		if (useOtherList)
		{
			players = new ArrayList<>();
			for (String s : _players)
			{
				Player player = Bukkit.getPlayer(s);
				
				if (!_plugin.isVanished(player))
				{
					players.add(player);
				}
			}
		}
		else
		{
			for (Player p : UtilServer.getPlayers())
				players.add(p);
		}

		int slot = 9;
		for (Player player : players)
		{
			if (showPlayer(player) && !_plugin.isVanished(player))
			{
				ItemStack head = getPlayerHead(player.getName(), C.cGreen + C.Bold + player.getName(), new String[]{ ChatColor.RESET + C.cGray + getDisplayString(player) });
				addButton(slot, head, new Button(slot, player));

				slot++;
			}
		}
	}

	public abstract boolean showPlayer(Player player);

	public abstract void clicked(int slot, Player player);

	public abstract String getDisplayString(Player player);

	private class Button implements IButton
	{
		private int _slot;
		private Player _player;

		public Button(int slot, Player player)
		{
			_slot = slot;
			_player = player;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			clicked(_slot, _player);
		}
	}


}
