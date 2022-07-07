package mineplex.game.clans.shop.energy;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClanEnergyManager;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.event.ClansShopAddButtonEvent;
import mineplex.game.clans.clans.event.EnergyPageBuildEvent;

public class EnergyPage extends ShopPageBase<ClanEnergyManager, EnergyShop>
{
	public EnergyPage(ClanEnergyManager plugin, EnergyShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Energy Shop", player, 18);
		buildPage();
	}
	
	@Override
	protected void buildPage()
	{
		ClanInfo clanInfo = getPlugin().getClansManager().getClan(getPlayer());
		if (clanInfo == null)
			buildNoClan();
		else
		{
			addInfo(clanInfo, 4);
			buildPurchase(clanInfo);
		}
	}
	
	private void buildPurchase(ClanInfo clanInfo)
	{
		int energyPerMin = Math.max(1, clanInfo.getEnergyCostPerMinute());
		
		int oneHourEnergy = energyPerMin * 60;
		int oneDayEnergy = oneHourEnergy * 24;
		int maxEnergy = clanInfo.getEnergyPurchasable();
		
		EnergyPageBuildEvent event = new EnergyPageBuildEvent(getPlayer());
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		addButton(clanInfo, 11, oneHourEnergy, Material.REDSTONE, (byte) 0, event.free(), " ", ChatColor.RESET + "Purchase 1 Hour of Energy for your Clan", " ", ChatColor.RESET + (event.free() ? "FREE! (Tutorial)" : "Costs " + C.cGreen + getPlugin().convertEnergyToGold(oneHourEnergy) + "g"));
		addButton(clanInfo, 13, oneDayEnergy, Material.REDSTONE_BLOCK, (byte) 0, event.free(), " ", ChatColor.RESET + "Purchase 1 Day of Energy for your Clan", " ", ChatColor.RESET + (event.free() ? "FREE! (Tutorial)" : "Costs " + C.cGreen + getPlugin().convertEnergyToGold(oneDayEnergy) + "g"));
		addButton(clanInfo, 15, maxEnergy, Material.FURNACE, (byte) 0, event.free(), " ", ChatColor.RESET + "Max Out your Clan's Energy", " ", ChatColor.RESET + (event.free() ? "FREE! (Tutorial)" : "Costs " + C.cGreen + getPlugin().convertEnergyToGold(maxEnergy) + "g"));
	}
	
	private void addInfo(ClanInfo clanInfo, int slot)
	{
		String itemName = clanInfo.getName();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(" ");
		lore.add(C.cYellow + "Energy: " + ChatColor.RESET + clanInfo.getEnergy());
		lore.add(C.cYellow + "Drain: " + ChatColor.RESET + clanInfo.getEnergyCostPerMinute() * 60 + " per Hour");
		lore.add(C.cYellow + "Max Energy: " + ChatColor.RESET + clanInfo.getEnergyMax());
		if (clanInfo.getEnergyCostPerMinute() > 0) lore.add(C.cYellow + "Time Left: " + ChatColor.RESET + UtilTime.convertString((clanInfo.getEnergy() / clanInfo.getEnergyCostPerMinute()) * 60000L, 1, UtilTime.TimeUnit.FIT));
		
		ShopItem shopItem = new ShopItem(Material.DIAMOND, itemName, lore.toArray(new String[0]), 1, false, false);
		setItem(slot, shopItem);
	}
	
	private void addButton(ClanInfo clanInfo, int slot, int energyAmount, Material material, byte data, boolean free, String... lore)
	{
		boolean locked = energyAmount > clanInfo.getEnergyPurchasable() || energyAmount == 0;
		String itemName = "Purchase " + energyAmount + " Energy";
		
		ShopItem shopItem = new ShopItem(material, itemName, lore, 1, locked, false);
		
		if (locked)
			setItem(slot, shopItem);
		else
			addButton(slot, shopItem, new EnergyShopButton(getPlugin(), this, energyAmount, clanInfo, free ? 0 : getPlugin().convertEnergyToGold(energyAmount)));
	}
	
	private void buildNoClan()
	{
		ShopItem shopItem = new ShopItem(Material.WOOD_SWORD, "No Clan!", new String[] { " ", ChatColor.RESET + "You need to be in a", ChatColor.RESET + "clan to purchase energy!" }, 1, false, false);
		setItem(4, shopItem);
	}
}
