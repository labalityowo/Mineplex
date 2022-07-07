package mineplex.clanshub;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.clanshub.queue.HubQueueManager;
import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.StatsManager;
import mineplex.game.clans.core.repository.tokens.SimpleClanToken;

/**
 * GUI page for clans servers
 */
public class ClansServerPage extends ShopPageBase<ClansTransferManager, ClansServerShop>
{
	private final HubQueueManager _queue = Managers.require(HubQueueManager.class);
	
	public ClansServerPage(ClansTransferManager plugin, ClansServerShop shop, CoreClientManager clientManager, 
							DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Clans", player, 54);
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		if (getClientManager().Get(_player).hasPermission(ClansTransferManager.Perm.STAFF_PAGE))
		{
			buildStaffJoinServers(getPlugin().Get(_player));
		}
		else
		{
			if (!getPlugin().Get(_player).getClanName().isEmpty())
			{
				buildJoinHomeServer(getPlugin().Get(_player));
			}
			else
			{
				buildNoClanPage();
			}
		}
	}
	
	private boolean canPlayHardcore()
	{
		if (getClientManager().Get(_player).hasPermission(ClansTransferManager.Perm.ALLOW_HARDCORE))
		{
			return true;
		}
		long secondsPlayed = Managers.get(StatsManager.class).Get(_player).getStat("Clans.TimePlaying"); 
		
		return secondsPlayed >= UtilTime.convert(20, TimeUnit.HOURS, TimeUnit.SECONDS);
	}
	
	private String timeTillUnlockHardcore()
	{
		if (canPlayHardcore())
		{
			return "0 Seconds";
		}
		else
		{
			long secondsPlayed = Managers.get(StatsManager.class).Get(_player).getStat("Clans.TimePlaying");
			long needed = UtilTime.convert(20, TimeUnit.HOURS, TimeUnit.SECONDS);
			
			return UtilTime.MakeStr(UtilTime.convert(needed - secondsPlayed, TimeUnit.SECONDS, TimeUnit.MILLISECONDS));
		}
	}
	
	private void buildNoClanPage()
	{
		Collection<ServerInfo> servers = UtilAlg.sortSet(getPlugin().getServers(true), (o1, o2) ->
		{
			try
			{
				int server1;
				int server2;
				if (o1.Name.contains("-"))
				{
					server1 = Integer.parseInt(o1.Name.substring(o1.Name.lastIndexOf('-') + 1));
				}
				else
				{
					server1 = Integer.parseInt(o1.Name);
				}
				if (o2.Name.contains("-"))
				{
					server2 = Integer.parseInt(o2.Name.substring(o2.Name.lastIndexOf('-') + 1));
				}
				else
				{
					server2 = Integer.parseInt(o2.Name);
				}
				
				return Integer.compare(server1, server2);
			}
			catch (NumberFormatException ex)
			{
				return o1.Name.compareTo(o2.Name);
			}
		});

		int currentSlot = 9;
		for (ServerInfo server : servers)
		{
			buildJoinServer(currentSlot, server);
			currentSlot++;
		}
	}
	
	private void buildJoinHomeServer(SimpleClanToken clan)
	{
		ServerInfo serverInfo = getServerInfo(clan.getHomeServer());
		boolean serverOnline = (serverInfo != null);
		if (!serverOnline)
		{
			System.out.println("Returning null");
		}
		String serverStatus = serverOnline ? C.cGreen + "Online" : C.cRed + "Offline";
		
		String title = (serverOnline ? C.cGreen : C.cRed) + C.Bold + "Join Home Server!";
		String serverName = C.cYellow + "Server Name: " + C.cWhite + clan.getHomeServer();
		String serverDesc = C.cYellow + "Server Status: " + C.cWhite + serverStatus;
		String players = (serverOnline ? C.cYellow + "Players: " + C.cWhite +  serverInfo.CurrentPlayers + "/" + serverInfo.MaxPlayers : "");
		String mode = (serverOnline ? (C.cYellow + "Mode: " + C.cWhite + (serverInfo.Hardcore ? "Hardcore" : "Casual")) : "");
		String queue1 = (serverOnline ? (C.cYellow + "Queue Status: " + (_queue.getData(serverInfo).QueuePaused ? C.cRed + "Paused" : C.cGreen + "Active")) : "");
		String queue2 = (serverOnline ? (C.cYellow + "Your Position: " + ((_queue.Get(getPlayer()).TargetServer != null && _queue.Get(getPlayer()).TargetServer.equals(serverInfo.Name)) ? (_queue.Get(getPlayer()).Queued ? C.cGreen + "#" + _queue.Get(getPlayer()).QueuePosition : C.cGray + "Joining...") : C.cGray + "Not Joined")) : "");
		String change = C.cRed + "Note: " + C.cWhite + "You must leave your Clan to ";
		String change2 = C.cWhite + "play on a different Clans Server!";
		ShopItem shopItem = new ShopItem(Material.EMERALD_BLOCK, title, new String[] {" ", serverName, serverDesc, players, mode, " ", queue1, queue2, " ", change, change2, " "}, 0, true, true);
		addButton(13, shopItem, new JoinServerButton(this, getServerInfo(clan.getHomeServer())));
	}
	
	private void buildJoinServer(int slot, ServerInfo server)
	{
		String title = C.cGreen + C.Bold + "Join Clans Server!";
		String desc1 = C.cYellow + "Server Name: " + C.cWhite + server.Name;
		String desc2 = C.cYellow + "Players: " + C.cWhite + server.CurrentPlayers + "/" + server.MaxPlayers;
		String desc3 = C.cYellow + "Mode: " + C.cWhite + (server.Hardcore ? "Hardcore" : "Casual");
		String queue1 = C.cYellow + "Queue Status: " + (_queue.getData(server).QueuePaused ? C.cRed + "Paused" : C.cGreen + "Active");
		String queue2 = C.cYellow + "Your Position: " + ((_queue.Get(getPlayer()).TargetServer != null && _queue.Get(getPlayer()).TargetServer.equals(server.Name)) ? (_queue.Get(getPlayer()).Queued ? C.cGreen + "#" + _queue.Get(getPlayer()).QueuePosition : C.cGray + "Joining...") : C.cGray + "Not Joined");
		String desc4 = "";
		String desc5 = "";
		if (!server.Hardcore || canPlayHardcore())
		{
			desc4 = C.cRed + "Note: " + C.cWhite + "Creating or Joining a clan on this";
			desc5 = C.cWhite + "server will set your Home Server!";
		}
		else
		{
			desc4 = C.cRed + "You have not unlocked Hardcore play yet!";
			desc5 = C.cWhite + "You need to play Casual for " + timeTillUnlockHardcore() + " to unlock Hardcore play!";
		}

		ShopItem shopItem = new ShopItem(Material.GOLD_BLOCK, title, new String[] {" ", desc1, desc2, desc3, " ", queue1, queue2, " ", desc4, desc5}, 0, true, true);
		if (server.Hardcore && !canPlayHardcore())
		{
			addButtonNoAction(slot, shopItem);
		}
		else
		{
			addButton(slot, shopItem, new JoinServerButton(this, server));
		}
	}
	
	private void buildStaffJoinServers(SimpleClanToken clan)
	{
		if (!clan.getClanName().isEmpty())
		{
			ServerInfo serverInfo = getServerInfo(clan.getHomeServer());
			boolean serverOnline = (serverInfo != null);
			if (!serverOnline)
			{
				System.out.println("Returning null");
			}
			String serverStatus = serverOnline ? C.cGreen + "Online" : C.cRed + "Offline";

			String title = (serverOnline ? C.cGreen : C.cRed) + C.Bold + "Join Home Server!";
			String serverName = C.cYellow + "Server Name: " + C.cWhite + clan.getHomeServer();
			String serverDesc = C.cYellow + "Server Status: " + C.cWhite + serverStatus;
			String mode = (serverOnline ? (C.cYellow + "Mode: " + C.cWhite + (serverInfo.Hardcore ? "Hardcore" : "Casual")) : "");
			String players = C.cYellow + "Players: " + C.cWhite + (serverOnline ? serverInfo.CurrentPlayers + "/" + serverInfo.MaxPlayers : "0/0");
			String queue1 = (serverOnline ? (C.cYellow + "Queue Status: " + (_queue.getData(serverInfo).QueuePaused ? C.cRed + "Paused" : C.cGreen + "Active")) : "");
			String queue2 = (serverOnline ? (C.cYellow + "Your Position: " + ((_queue.Get(getPlayer()).TargetServer != null && _queue.Get(getPlayer()).TargetServer.equals(serverInfo.Name)) ? (_queue.Get(getPlayer()).Queued ? C.cGreen + "#" + _queue.Get(getPlayer()).QueuePosition : C.cGray + "Joining...") : C.cGray + "Not Joined")) : "");
			ShopItem shopItem = new ShopItem(Material.EMERALD_BLOCK, title, new String[] {" ", serverName, serverDesc, players, mode, " ", queue1, queue2, " "}, 0, true, true);
			addButton(13, shopItem, new JoinServerButton(this, getServerInfo(clan.getHomeServer())));
		}
		
		Collection<ServerInfo> servers = UtilAlg.sortSet(getPlugin().getServers(true), (o1, o2) ->
		{
			try
			{
				int server1;
				int server2;
				if (o1.Name.contains("-"))
				{
					server1 = Integer.parseInt(o1.Name.substring(o1.Name.lastIndexOf('-') + 1));
				}
				else
				{
					server1 = Integer.parseInt(o1.Name);
				}
				if (o2.Name.contains("-"))
				{
					server2 = Integer.parseInt(o2.Name.substring(o2.Name.lastIndexOf('-') + 1));
				}
				else
				{
					server2 = Integer.parseInt(o2.Name);
				}
				
				return Integer.compare(server1, server2);
			}
			catch (NumberFormatException ex)
			{
				return o1.Name.compareTo(o2.Name);
			}
		});

		int currentSlot = 27;
		for (ServerInfo server : servers)
		{
			if (!clan.getClanName().isEmpty() && server.Name.equalsIgnoreCase(clan.getHomeServer()))
			{
				continue;
			}
			buildJoinServer(currentSlot, server);
			currentSlot++;
		}
	}
	 
	private ServerInfo getServerInfo(String serverName)
	{
		return getPlugin().getServer(serverName);
	}
	
	/**
	 * Refresh all GUI pages and buttons
	 */
	public void update()
	{
		getButtonMap().clear();
		buildPage();
	}
}