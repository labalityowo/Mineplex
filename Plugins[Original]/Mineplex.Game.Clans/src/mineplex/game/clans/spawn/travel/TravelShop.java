package mineplex.game.clans.spawn.travel;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Sets;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.spawn.Spawn;

public class TravelShop extends ShopBase<ClansManager>
{
	public static final String[] TRAVEL_LOCATIONS = { "North Shop", "South Shop", "East Spawn", "West Spawn" };
	
	public TravelShop(ClansManager plugin, CoreClientManager clientManager, mineplex.core.donation.DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Travel Hub");
	}
	
	@Override
	protected ShopPageBase<ClansManager, ? extends ShopBase<ClansManager>> buildPagesFor(Player player)
	{
		return new TravelPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
	
	@Override
	public boolean attemptShopOpen(Player player)
	{
		if (Spawn.getInstance().isCombatTagged(player))
		{
			notify(player, "You cannot use the Travel Hub while combat tagged!");
			return false;
		}
		
		return super.attemptShopOpen(player);
	}
	
	/**
	 * Destroy lone instances of Travel buttons that are fetched into a non-shop
	 * inventory (via lag)
	 * 
	 * @param event
	 */
	@EventHandler
	public void onInventoryClickedd(InventoryClickEvent event)
	{
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null)
			return;
		else if (isPlayerInShop(event.getWhoClicked())) return;
		
		String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
		for (String travelLocation : TRAVEL_LOCATIONS)
		{
			if (displayName.contains(travelLocation))
			{
				event.setCurrentItem(null);
			}
		}
	}
	
	private static void notify(Player player, String message)
	{
		UtilPlayer.message(player, F.main("Travel Hub", message));
	}
}
