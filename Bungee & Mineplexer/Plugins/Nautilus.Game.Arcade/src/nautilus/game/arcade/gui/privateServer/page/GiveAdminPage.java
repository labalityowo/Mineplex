package nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class GiveAdminPage extends PlayerPage
{
	public GiveAdminPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Give Co-Host", player);
		buildPage();
	}

	@Override
	public boolean showPlayer(Player player)
	{
		return !_manager.isAdmin(player, false);
	}

	@Override
	public void clicked(int slot, Player player)
	{
		removeButton(slot);
		_manager.giveAdmin(player);
		UtilPlayer.message(getPlayer(), F.main("Server", "You gave " + F.name(player.getName()) + " Co-Host power."));
	}

	@Override
	public String getDisplayString(Player player)
	{
		return "Click to Make Co-Host";
	}
}
