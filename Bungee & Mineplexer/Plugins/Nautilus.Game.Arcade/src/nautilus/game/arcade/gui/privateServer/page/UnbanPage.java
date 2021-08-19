package nautilus.game.arcade.gui.privateServer.page;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.shop.item.IButton;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class UnbanPage extends BasePage
{
	public UnbanPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Un-Remove Players", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackButton(4);

		Set<String> blackList = _manager.getBlacklist();
		Iterator<String> iterator = blackList.iterator();

		int slot = 9;
		while (iterator.hasNext())
		{
			String name = iterator.next();
			ItemStack head = getPlayerHead(name, C.cGreen + C.Bold + name, new String[] {ChatColor.RESET + C.cGray + "Click to Un-Remove Player"});
			addButton(slot, head, getUnbanButton(slot, name));

			slot++;
		}


//		int arrayModifier = _currentPage * 9 * 4;
//		for (int i = 0; i < _currentPage; i++)
//		{
//			int slot = 9 + i;
//			ItemStack head = getPlayerHead(blackList.)
//
//		}
	}

	private IButton getUnbanButton(final int slot, final String playerName)
	{
		return (player, clickType) ->
		{
			_manager.getBlacklist().remove(playerName);
			removeButton(slot);
			UtilPlayer.message(getPlayer(), F.main("Server", F.name(playerName) + " can now join this private server."));
		};
	}
}