package mineplex.game.clans.clans.gui.page;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanShop;

public abstract class ClanPageBase extends ShopPageBase<ClansManager, ClanShop>
{
	public ClanPageBase(ClansManager plugin, ClanShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, int size)
	{
		super(plugin, shop, clientManager, donationManager, name, player, size);
	}

	@Override
	protected final void buildPage()
	{
		ClanInfo clanInfo = getPlugin().getClan(getPlayer());

		if (clanInfo == null)
		{
			buildNoClan();
		}
		else
		{
			ClanRole role = getPlugin().getClanUtility().getRole(getPlayer());
			buildClan(clanInfo, role);
		}
	}

	public abstract void buildNoClan();

	public abstract void buildClan(ClanInfo clanInfo, ClanRole clanRole);

	protected void addBackButton()
	{
		addBackButton(4);
	}

	protected void addBackButton(int slot)
	{
		addButton(slot, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				ShopPageBase<ClansManager, ClanShop> mainPage = new ClanMainPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer());

				getShop().openPageForPlayer(getPlayer(), mainPage);
				player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			}
		});
	}
}
