package nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class BanPage extends PlayerPage
{
	public BanPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Remove Players", player);
		buildPage();
	}

	@Override
	public boolean showPlayer(Player player)
	{
		return !_manager.isAdmin(player, true);
	}

	@Override
	public void clicked(int slot, Player player)
	{
		removeButton(slot);
		_manager.ban(player);
		UtilPlayer.message(getPlayer(), F.main("Server", "You have removed " + F.name(player.getName()) + " from this private server."));
	}

	@Override
	public String getDisplayString(Player player)
	{
		return "Click to Remove Player";
	}
}
