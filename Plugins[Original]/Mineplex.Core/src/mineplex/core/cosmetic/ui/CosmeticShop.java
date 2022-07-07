package mineplex.core.cosmetic.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.messaging.PluginMessageListener;

import mineplex.core.account.CoreClientManager;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.page.ItemGadgetPage;
import mineplex.core.cosmetic.ui.page.Menu;
import mineplex.core.cosmetic.ui.page.PetTagPage;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;

public class CosmeticShop extends ShopBase<CosmeticManager> implements PluginMessageListener
{
	public CosmeticShop(CosmeticManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name)
	{
		super(plugin, clientManager, donationManager, name);
		
		plugin.getPlugin().getServer().getMessenger().registerIncomingPluginChannel(plugin.getPlugin(), "MC|ItemName", this);
	}

	@Override
	protected ShopPageBase<CosmeticManager, ? extends ShopBase<CosmeticManager>> buildPagesFor(Player player)
	{
		return new Menu(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message)
	{
        if (!channel.equalsIgnoreCase("MC|ItemName"))
            return;
        
        if (getPlayerPageMap().containsKey(player.getUniqueId()) && getPlayerPageMap().get(player.getUniqueId()) instanceof PetTagPage)
        {
	        if (message != null && message.length >= 1)
	        {
	            String tagName = new String(message);
	            
	            ((PetTagPage) getPlayerPageMap().get(player.getUniqueId())).SetTagName(tagName);
	        }
	    }
	}

	@EventHandler
	public void itemGadgetEmptyAmmo(ItemGadgetOutOfAmmoEvent event)
	{
		new ItemGadgetPage(getPlugin(), this, getClientManager(), getDonationManager(), "Item Gadgets", event.getPlayer()).purchaseGadget(event.getPlayer(), event.getGadget());
	}
}
