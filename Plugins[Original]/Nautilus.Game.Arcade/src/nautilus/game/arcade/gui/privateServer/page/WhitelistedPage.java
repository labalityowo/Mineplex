package nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.portal.Intent;
import mineplex.core.shop.item.IButton;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class WhitelistedPage extends BasePage
{
	public WhitelistedPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Whitelisted Players", player);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackButton(4);

		int slot = 9;
		for (final String s : getPlugin().GetGameHostManager().getWhitelist())
		{
			ItemStack head = getPlayerHead(s, C.cGreen + C.Bold + s, new String[]{C.cGray + "Click to un-whitelist player"});
			final int i = slot;
			addButton(slot, head, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					getPlugin().GetGameHostManager().getWhitelist().remove(s);
					removeButton(i);
					getPlayer().sendMessage(F.main("Whitelist", "§e" + s + " §7is no longer whitelisted."));
					if (Bukkit.getPlayer(s) != null)
						getPlugin().GetPortal().sendToHub(Bukkit.getPlayer(s), "You are no longer whitelisted.", Intent.KICK);
				}
			});

			slot++;
		}
	}
}
