package mineplex.game.clans.clans.gui.page;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public class ClanInvitePage extends ClanPageBase
{
	public ClanInvitePage(ClansManager plugin, ClanShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Invite Players", player, 54);

		buildPage();
	}

	@Override
	public void buildNoClan()
	{

	}

	@Override
	public void buildClan(ClanInfo clanInfo, ClanRole clanRole)
	{
		int index = 9;
		for (Player player : UtilServer.getSortedPlayers())
		{
			if (index <= 54 && getPlugin().getClan(player) == null && !_plugin.getIncognitoManager().Get(player).Status)
			{
				addPlayerButton(index, player, clanInfo);
				index++;
			}
		}

		if (index == 9)
		{
			// No players online to join!
			String[] lore = new String[] { C.Reset + "There are no players online who", C.Reset + "are not in a clan already!" };
			ShopItem shopItem = new ShopItem(Material.BARRIER, "No Players!", lore,1, true, false);
			addItem(22, shopItem);
		}

		addBackButton();
	}

	private void addPlayerButton(int slot, final Player target, final ClanInfo clan)
	{
		String itemName = C.cGreenB + target.getName();
		ArrayList<String> lore = new ArrayList<String>(5);
		lore.add(" ");
		lore.add(" ");
		lore.add(ChatColor.RESET + C.cGray + "Left Click " + C.cWhite + "Invite Player");

		ItemStack item = UtilSkull.getPlayerHead(target.getName(), itemName, lore);

		addButton(slot, item, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getPlugin().getClanUtility().invite(getPlayer(), clan, target);
			}
		});
	}
}
