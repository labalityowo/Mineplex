package mineplex.clanshub;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.F;
import mineplex.core.donation.DonationManager;
import mineplex.core.party.Party;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * GUI Shop manager for clans servers
 */
public class ClansServerShop extends ShopBase<ClansTransferManager>
{
	public ClansServerShop(ClansTransferManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Clans Servers");
	}

	@Override
	protected ShopPageBase<ClansTransferManager, ? extends ShopBase<ClansTransferManager>> buildPagesFor(Player player)
	{
		return new ClansServerPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	@Override
	protected boolean canOpenShop(Player player)
	{
		Party party = getPlugin().getPartyManager().getPartyByPlayer(player);
		 
		if (party != null) 
		{  
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, .6f);
			player.sendMessage(F.main("Party", "You cannot join Clans while in a party."));
			return false;
		}
		
		return true;
	}
}