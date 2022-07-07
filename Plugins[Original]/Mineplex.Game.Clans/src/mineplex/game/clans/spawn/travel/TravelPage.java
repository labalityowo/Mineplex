package mineplex.game.clans.spawn.travel;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.gui.ClanIcon;
import mineplex.game.clans.clans.siege.outpost.Outpost;
import mineplex.game.clans.shop.ClansShopItem;
import mineplex.game.clans.spawn.Spawn;

public class TravelPage extends ShopPageBase<ClansManager, TravelShop>
{
	public TravelPage(ClansManager plugin, TravelShop shop, CoreClientManager clientManager, DonationManager donationManager, org.bukkit.entity.Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Travel", player, 45);
		
		buildPage();
	}
	
	@Override
	protected void buildPage()
	{
		addTravelLocation(Spawn.getNorthSpawn(), getPlayer().getLocation().distance(Spawn.getNorthSpawn()) <= 64 ? Material.SKULL_ITEM : Material.IRON_SWORD, (getPlayer().getLocation().distance(Spawn.getNorthSpawn()) <= 64 ? C.cRedB : C.cGreenB) + "North Spawn", new String[] {
				C.cWhite + "Spawns are locations where",
				C.cWhite + "you respawn after dying.",
				" ",
				C.cWhite + "You cannot be attacked here,",
				C.cWhite + "as they are Safe Zones.",
				getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? " " : "",
				getPlayer().getLocation().distance(Spawn.getNorthSpawn()) <= 64 ? C.cRed + "You are already here." : "",
		}, 4, getPlayer().getLocation().distance(Spawn.getNorthSpawn()) <= 64 ? (byte) 3 : (byte) 0);
		
		addTravelLocation(Spawn.getSouthSpawn(), getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? Material.SKULL_ITEM : Material.IRON_SWORD, (getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? C.cRedB : C.cGreenB) + "South Spawn", new String[] {
				C.cWhite + "Spawns are locations where",
				C.cWhite + "you respawn after dying.",
				" ",
				C.cWhite + "You cannot be attacked here,",
				C.cWhite + "as they are Safe Zones.",
				getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? " " : "",
				getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? C.cRed + "You are already here." : "",
		}, 22 + 9 + 9, getPlayer().getLocation().distance(Spawn.getSouthSpawn()) <= 64 ? (byte) 3 : (byte) 0);
		
		addTravelLocation(Spawn.getWestTown(), ClanIcon.CASTLE.getMaterial(), C.cDGreenB + "West Shop", new String[] {
				C.cWhite + "Shops are locations where you",
				C.cWhite + "can buy and sell all sorts of goods.",
				" ",
				C.cWhite + "You cannot be attacked here,",
				C.cWhite + "as they are Safe Zones.",
		}, 12 + 8, ClanIcon.CASTLE.getData());
		
		addTravelLocation(Spawn.getEastTown(), ClanIcon.CASTLE.getMaterial(), C.cDGreenB + "East Shop", new String[] {
				C.cWhite + "Shops are locations where you",
				C.cWhite + "can buy and sell all sorts of goods.",
				" ",
				C.cWhite + "You cannot be attacked here,",
				C.cWhite + "as they are Safe Zones.",
		}, 14 + 10, ClanIcon.CASTLE.getData());
		
		final ClanInfo clan = _plugin.getClan(getPlayer());
		
		if (Clans.HARDCORE)
		{
			Outpost outpost = _plugin.getSiegeManager().getOutpostManager().Get(clan);
			
			addTravelLocation(outpost == null ? null : outpost.getCoreLocation().clone().add(0, 1, 0), ClansShopItem.OUTPOST.getMaterial(), (outpost == null ? C.cRedB : C.cDGreenB) + "Outpost", new String[] {
					C.cWhite + "Teleport to your Clan's currently",
					C.cWhite + "active Outpost.",
					" ",
					(outpost == null ? C.cRed + "Your Clan does not have an Outpost." : ""),
			}, 8, ClanIcon.CASTLE.getData());
		}
		
		
		
		final ItemStack item = new ItemStack(Material.BED, 1);
		ItemMeta meta = item.getItemMeta();
		
		if (meta == null)
		{
			meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		}
		
		if (clan == null)
		{
			meta.setDisplayName(C.cRedB + "Clan Home");
			meta.setLore(Arrays.asList(" ", C.cWhite + "You are not in a Clan."));
		}
		else if (clan.getHome() == null)
		{
			meta.setDisplayName(C.cRedB + "Clan Home");
			meta.setLore(Arrays.asList(" ", C.cWhite + "Your Clan does not have a Home. ", (clan.getMembers().get(getPlayer().getUniqueId()).getRole().equals(ClanRole.ADMIN) || clan.getMembers().get(getPlayer().getUniqueId()).getRole().equals(ClanRole.LEADER) ? C.cWhite + "Type " + C.cYellow + "/c sethome" + C.cWhite + " to set a home" : "Ask your Clan's Leader to set a home.")));
		}
		else
		{
			if (UtilBlock.isValidBed(clan.getHome()) && clan.getHome().clone().add(0, 1, 0).getBlock().getType().equals(Material.AIR) && clan.getHome().clone().add(0, 2, 0).getBlock().getType().equals(Material.AIR))
			{
				meta.setDisplayName(C.cGreenB + "Clan Home");
				meta.setLore(Arrays.asList(" ", C.cWhite + "Teleport to your Clan Home."));
			}
			else
			{
				meta.setDisplayName(C.cRedB + "Clan Home");
				meta.setLore(Arrays.asList(" ", C.cWhite + "Your Clan's Home Bed has been",
										   C.cWhite + "destroyed, or is obstructed."));
			}
		}
		
		item.setItemMeta(meta);
		
		addButton(22, item, new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				if (item.getItemMeta().getDisplayName().startsWith(C.cGreen))
				{
					player.closeInventory();
					player.teleport(clan.getHome().clone().add(0, 1, 0));
				}
			}
		});
		
		addButton(44, new ItemBuilder(new ItemStack(Material.WATCH, 1)).setTitle(C.cGoldB + "Mineplex Lobby").addLore(" ", C.cYellow + "Left-Click" + C.cWhite + " to Warp", " ", C.cWhite + "Return to the main Mineplex Lobby", " ", C.cWhite + "You can do this at any time by", C.cWhite + "typing " + C.cYellow + "/lobby" + C.cWhite + ".").build(), new IButton()
		{
			public void onClick(Player player, ClickType clickType)
			{
				Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.PLAYER_REQUEST);
			}
		});
	}
	
	public void addTravelLocation(Location location, Material material, String name, String[] lore, int slot)
	{
		TravelButton button = new TravelButton(this, location, material, name, lore, (byte) 0);
		addButton(slot, button.generateButtonItem(), button);
	}
	
	public void addTravelLocation(Location location, Material material, String name, String[] lore, int slot, byte data)
	{
		TravelButton button = new TravelButton(this, location, material, name, lore, data);
		addButton(slot, button.generateButtonItem(), button);
	}
}
