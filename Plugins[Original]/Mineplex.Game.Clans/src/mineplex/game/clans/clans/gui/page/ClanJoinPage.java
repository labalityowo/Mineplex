package mineplex.game.clans.clans.gui.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.gui.button.ClanJoinButton;

public class ClanJoinPage extends ClanPageBase
{
	public ClanJoinPage(ClansManager plugin, ClanShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Join Clan", player, 54);

		buildPage();
	}

	@Override
	public void buildNoClan()
	{
		int currentIndex = 9;

		for (ClanInfo clanInfo : getPlugin().getClanMap().values())
		{
			if (clanInfo.isInvited(getPlayer().getName()))
			{
				addInvitation(currentIndex, clanInfo);
				currentIndex++;
			}
		}

		if (currentIndex == 9)
		{
			// No invitations :(
			ShopItem shopItem = new ShopItem(Material.BOOK, "You have no Clan Invitations!", new String[] {}, 1, true, false);
			setItem(22, shopItem);
		}

		addBackButton();
	}

	private void addInvitation(int index, ClanInfo clanInfo)
	{
		ShopItem shopItem = new ShopItem(Material.BOOK_AND_QUILL, clanInfo.getName(), new String[] {}, 1, false, false);
		addButton(index, shopItem, new ClanJoinButton(getPlugin(), clanInfo));
	}

	@Override
	public void buildClan(ClanInfo clanInfo, ClanRole clanRole)
	{
		// Nothing
	}
}
