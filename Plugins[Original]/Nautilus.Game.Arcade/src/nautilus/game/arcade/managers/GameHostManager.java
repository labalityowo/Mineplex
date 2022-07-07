package nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mineplex.anticheat.MineplexAnticheat;
import com.mineplex.anticheat.checks.move.Glide;

import mineplex.core.Managers;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.CommunityManager;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import nautilus.game.arcade.gui.privateServer.page.GameVotingPage;

public class GameHostManager implements Listener
{
	public enum Perm implements Permission
	{
		AUTO_ADMIN_ACCESS,
		INCREASE_MAX_PLAYERS_60,
		INCREASE_MAX_PLAYERS_100,
	}

	private final AntiHack _antiHack;
	private List<GameType> _games = new ArrayList<>();

	ArcadeManager Manager;
	
	private Player _host;
	private PermissionGroup _hostRank;
	private long _serverStartTime = System.currentTimeMillis();
	private long _serverExpireTime = 21600000;
	private long _lastOnline = System.currentTimeMillis();
	private long _expireTime = 300000;
	private boolean _hostExpired = false;

	private Set<Player> _onlineAdmins = new HashSet<>();
	private Set<String> _adminList = new HashSet<>();
	private Set<String> _whitelist = new HashSet<>();
	private Set<String> _blacklist = new HashSet<>();

	private PrivateServerShop _shop;
	
	private boolean _isEventServer = false;
	
	private Map<Player, Boolean> _permissionMap = new HashMap<>();

	private boolean _voteInProgress = false;
	private Map<String, GameType> _votes = new HashMap<>();
	private int _voteNotificationStage = 1;
	
	public GameHostManager(ArcadeManager manager)
	{
		_antiHack = Managers.require(AntiHack.class);
		Manager = manager;
		_shop = new PrivateServerShop(manager, manager.GetClients(), manager.GetDonation());
		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
		
		//Games
		_games.add(GameType.Smash);
		_games.add(GameType.BaconBrawl);
		_games.add(GameType.DeathTag);
		_games.add(GameType.DragonEscape);
		_games.add(GameType.Dragons);
		_games.add(GameType.Micro);
		_games.add(GameType.Paintball);
		_games.add(GameType.Quiver);
		_games.add(GameType.Runner);
		_games.add(GameType.Sheep);
		_games.add(GameType.Snake);
		_games.add(GameType.SneakyAssassins);
		_games.add(GameType.TurfWars);
		_games.add(GameType.Spleef);
		_games.add(GameType.Lobbers);
		_games.add(GameType.Evolution);
		_games.add(GameType.MonsterMaze);
		_games.add(GameType.Gladiators);
		_games.add(GameType.ChampionsDominate);
		_games.add(GameType.ChampionsTDM);
		_games.add(GameType.ChampionsCTF);
		_games.add(GameType.HideSeek);
		_games.add(GameType.Draw);
		_games.add(GameType.Bridge);
		_games.add(GameType.SurvivalGames);
		_games.add(GameType.CastleSiege);
		_games.add(GameType.WitherAssault);
		_games.add(GameType.Wizards);
		_games.add(GameType.Build);
		_games.add(GameType.UHC);
		_games.add(GameType.MineStrike);
		_games.add(GameType.Skywars);
		_games.add(GameType.SpeedBuilders);
		_games.add(GameType.MOBA);
		_games.add(GameType.CakeWars4);
		_games.add(GameType.CakeWarsDuos);
		_games.add(GameType.CastleAssault);
		_games.add(GameType.CastleAssaultTDM);

		//Rejected / Other
		_games.add(GameType.MilkCow);
		_games.add(GameType.ZombieSurvival);
		_games.add(GameType.SurvivalGamesTeams);
		_games.add(GameType.SkywarsTeams);
		_games.add(GameType.SmashTeams);
		_games.add(GameType.SnowFight);
		_games.add(GameType.Gravity);
		_games.add(GameType.Barbarians);
		_games.add(GameType.SmashDomination);

		_games.sort(Comparator.comparing(GameType::getName));

		//Config Defaults
		if (Manager.GetHost() != null && Manager.GetHost().length() > 0)
		{
			setDefaultConfig();
		}
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.AUTO_ADMIN_ACCESS, true, true);
		PermissionGroup.SRMOD.setPermission(Perm.INCREASE_MAX_PLAYERS_100, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.INCREASE_MAX_PLAYERS_60, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.INCREASE_MAX_PLAYERS_100, true, true);
		PermissionGroup.YT.setPermission(Perm.INCREASE_MAX_PLAYERS_100, false, false);
	}

	public ArrayList<GameType> hasWarning()
	{
		ArrayList<GameType> games = new ArrayList<>();
		games.add(GameType.MilkCow);
		games.add(GameType.ZombieSurvival);
		return games;
	}

	@EventHandler
	public void onStateChange(GameStateChangeEvent event)
	{
		if (!isEventServer())
		{
			return;
		}

		// Disable all checks in event servers
		Stream.concat(AntiHack.ACTIONS.keySet().stream(), AntiHack.CHECKS.keySet().stream()).distinct().forEach(_antiHack::addIgnoredCheck);
		JavaPlugin.getPlugin(MineplexAnticheat.class).getCheckManager().disableCheck(Glide.class);
	}

	@EventHandler
	public void updateHost(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		//No Host - Not MPS
		if (Manager.GetHost() == null || Manager.GetHost().length() == 0)
			return;
		
		// Set as event server
		if (Manager.GetGame() != null && Manager.GetGame().GetType() == GameType.Event)
		{
			setEventServer(true);
		}
		
		// Admins update
		for (Player player : UtilServer.getPlayers())
		{
			if (isHost(player) || isAdmin(player, true))
			{
				if (Manager.GetGame() == null || Manager.GetGame().GetState() == GameState.Recruit)
					giveAdminItem(player);
			}
			
			if (isHost(player) || (isAdmin(player, false) && (isEventServer() || isCommunityServer())))
				_lastOnline = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void voteNotification(UpdateEvent e)
	{
		if (e.getType() != UpdateType.FAST)
			return;

		if (!_voteInProgress)
			return;

		if (_voteNotificationStage == 1)
		{
			UtilTextBottom.display(C.cYellow + C.Bold + "Type " + C.cGold +  C.Bold + "/vote" + C.cYellow + C.Bold + " to vote for next game", UtilServer.getPlayers());
			_voteNotificationStage++;
			return;
		}
		else if (_voteNotificationStage == 2)
		{
			UtilTextBottom.display(C.cGold + C.Bold + "Type " + C.cYellow +  C.Bold + "/vote" + C.cGold + C.Bold + " to vote for next game", UtilServer.getPlayers());
			_voteNotificationStage = 1;
			return;
		}
	}

	@EventHandler
	public void handleLogin(PlayerLoginEvent event)
	{
		Player p = event.getPlayer();
		boolean alwaysAllow = Manager.GetClients().Get(p.getUniqueId()).hasPermission(ArcadeManager.Perm.BYPASS_MPS_WHITELIST);
		
		if (alwaysAllow)
		{
			return;
		}
		
		if (isCommunityServer())
		{
			if (getOwner().getMembers().containsKey(p.getUniqueId()))
			{
				return;
			}
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You are not a member of this MCS.");
			return;
		}
		if (Manager.GetServerConfig().PlayerServerWhitelist)
		{
			if (!getWhitelist().contains(p.getName().toLowerCase()))
			{
				if ((Manager.GetHost() != null) && (Manager.GetHost().equalsIgnoreCase(p.getName())))
				{
					return;
				}
				event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "This MPS is whitelisted.");
			}
		}
		if (_blacklist.contains(p.getName()))
		{
			if (isCommunityServer())
			{
				event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You were removed from this Mineplex Community Server.");
			}
			else
			{
				event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You were removed from this Mineplex Private Server.");
			}
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		if (_shop.isPlayerInShop(event.getWhoClicked()))
		{
			if (event.getClickedInventory().getType() == InventoryType.PLAYER)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void adminJoin(PlayerJoinEvent event)
	{
		if (!isPrivateServer())
			return;

		if (Manager.GetHost().equals(event.getPlayer().getName()))
		{
			_host = event.getPlayer();
			_hostRank = Manager.GetClients().Get(_host).getPrimaryGroup();
			System.out.println("Game Host Joined.");
			
			//Limit player count!
			if (Manager.GetServerConfig().MaxPlayers > getMaxPlayerCap())
			{
				Manager.GetServerConfig().MaxPlayers = getMaxPlayerCap();
			}
			
			if (isEventServer())
			{
				worldeditPermissionSet(event.getPlayer(), true);
			}
		}
		else if (isAdmin(event.getPlayer(), false))
		{
			System.out.println("Admin Joined.");
			_onlineAdmins.add(event.getPlayer());
			
			if (isEventServer())
			{
				worldeditPermissionSet(event.getPlayer(), true);
			}
		}
	}
	
	@EventHandler
	public void adminQuit(PlayerQuitEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (isHost(event.getPlayer()))
		{
			System.out.println("Game Host Quit.");
			_host = null;
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), false);
		}
		else if (isAdmin(event.getPlayer(), false))
		{
			_onlineAdmins.remove(event.getPlayer());
			
			if (isEventServer())
				worldeditPermissionSet(event.getPlayer(), false);
		}
	}
		
	public void worldeditPermissionSet(Player player, boolean hasPermission)
	{
		if (!_permissionMap.containsKey(player) || _permissionMap.get(player) != hasPermission)
		{
			for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
			{
				player.addAttachment(plugin, "worldedit.*", hasPermission);
			}
			
			_permissionMap.put(player, hasPermission);
			
			UtilPlayer.message(player, "World Edit Permissions: " + F.tf(hasPermission));
		}
	}
	
	@EventHandler
	public void updateHostExpired(UpdateEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (event.getType() != UpdateType.FAST)
			return;
	
		if (Manager.GetGame() != null && Manager.GetGame().GetState() != GameState.Recruit)
			return;
		
		if (_hostExpired)
			return;
		
		if (UtilTime.elapsed(_lastOnline, _expireTime))
		{
			if (isCommunityServer())
			{
				setHostExpired(true, getOwner().getName() + " has abandoned the server. Thanks for playing!");
			}
			else
			{
				setHostExpired(true, Manager.GetServerConfig().HostName + " has abandoned the server. Thanks for playing!");
			}
		}
		
		else if (UtilTime.elapsed(_serverStartTime, _serverExpireTime))
			setHostExpired(true, "This server has expired! Thank you for playing!");
	}
	
	public boolean isHostExpired()
	{
		if (!isPrivateServer())
			return false;
		
		return _hostExpired;
	}

	public void setHostExpired(boolean expired, String string)
	{
		for (Player other : UtilServer.getPlayers())
		{
			UtilPlayer.message(other, C.cGold + C.Bold + string);
			other.playSound(other.getLocation(), Sound.ENDERDRAGON_GROWL, 10f, 1f);
		}

		Manager.GetPortal().sendAllPlayersToGenericServer(GenericServer.HUB, Intent.KICK);

		_hostExpired = expired;
	}
	
	private void giveAdminItem(Player player)
	{
		if (Manager.GetGame() == null)
			return;

		if (UtilGear.isMat(player.getInventory().getItem(8), Material.SPECKLED_MELON))
			return;
		
		if (player.getOpenInventory().getType() != InventoryType.CRAFTING &&
			player.getOpenInventory().getType() != InventoryType.CREATIVE)
			return;
		
		player.getInventory().setItem(8, ItemStackFactory.Instance.CreateStack(Material.SPECKLED_MELON, (byte)0, 1, C.cGreen + C.Bold + "/menu"));
	}

	private void removeAdminItem(Player player)
	{
		if (player.getInventory().getItem(8) != null && player.getInventory().getItem(8).getType() == Material.SPECKLED_MELON)
		{
			player.getInventory().setItem(8, null);
		}
	}
	
	public Set<String> getWhitelist()
	{
		return _whitelist;
	}
	
	public Set<String> getBlacklist()
	{
		return _blacklist;
	}

	public Set<String> getAdminList()
	{
		return _adminList;
	}

	@EventHandler
	public void broadcastCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/bc"))
			return;

		if (!isPrivateServer())
			return;

		if (!isAdmin(event.getPlayer(), true))
		{
			event.getPlayer().sendMessage(F.main("Broadcast", "Only Co-Hosts can use this command."));
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);

		if (event.getMessage().split(" ").length < 2)
		{
			event.getPlayer().sendMessage(F.main("Broadcast", "/bc <message>"));
			return;
		}

		if (Manager.getPunishments().GetClient(event.getPlayer().getName()).IsMuted())
		{
			return;
		}

		String msg = "";
		for (int i = 1; i < event.getMessage().split(" ").length; i++)
		{
			msg += event.getMessage().split(" ")[i] + " ";
		}
		msg = msg.trim();
		
		msg = Manager.GetChat().filterMessage(event.getPlayer(), msg);
		
		if (msg == null)
		{
			return;
		}
		
		Bukkit.broadcastMessage(C.cDGreen + C.Bold + event.getPlayer().getName() + " " + C.cGreen + msg);
	}

	@EventHandler
	public void voteCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/vote"))
			return;

		if (!isPrivateServer())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Vote", "This command is only available on private servers."));
			event.setCancelled(true);
			return;
		}

		if (!_voteInProgress)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Vote", "There is no vote in progress."));
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		_shop.openPageForPlayer(event.getPlayer(), new GameVotingPage(Manager, _shop, event.getPlayer()));
		return;
	}
	
	@EventHandler
	public void menuCommand(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/menu"))
			return;

		if (!isPrivateServer())
			return;
		
		if (!isAdmin(event.getPlayer(), true))
			return;
		
		event.setCancelled(true);
		openMenu(event.getPlayer());
	}
	
	@EventHandler
	public void menuInteract(PlayerInteractEvent event)
	{
		if (!isPrivateServer())
			return;
		
		if (!isAdmin(event.getPlayer(), true))
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.SPECKLED_MELON))
			return;
		
		openMenu(event.getPlayer());
		event.setCancelled(true);
	}
	
	private void openMenu(Player player)
	{
		_shop.attemptShopOpen(player);
	}

	public boolean isAdmin(Player player, boolean includeStaff)
	{
		if (isCommunityServer())
		{
			return (getOwner().getMembers().containsKey(player.getUniqueId()) && getOwner().getMembers().get(player.getUniqueId()).Role.ordinal() <= CommunityRole.COLEADER.ordinal()) || (includeStaff && Manager.GetClients().Get(player).hasPermission(Perm.AUTO_ADMIN_ACCESS));
		}
		return player.equals(_host) || _adminList.contains(player.getName()) || (includeStaff && Manager.GetClients().Get(player).hasPermission(Perm.AUTO_ADMIN_ACCESS));
	}

	public boolean isHost(Player player)
	{
		if (Manager.GetHost() != null && Manager.GetHost().startsWith("COM-"))
		{
			CommunityManager cmanager = Managers.get(CommunityManager.class);
			int communityId = Integer.parseInt(Manager.GetHost().replace("COM-", ""));
			Community c = cmanager.getLoadedCommunity(communityId);
			return c.getMembers().containsKey(player.getUniqueId()) && c.getMembers().get(player.getUniqueId()).Role == CommunityRole.LEADER;
		}
		
		return player.getName().equals(Manager.GetHost());
	}

	public boolean isPrivateServer()
	{
		return Manager.GetHost() != null && Manager.GetHost().length() > 0;
	}

	@EventHandler
	public void whitelistCommand(PlayerCommandPreprocessEvent event)
	{
		if (_host == null)
			return;

		if (!event.getMessage().toLowerCase().startsWith("/whitelist") && !event.getMessage().toLowerCase().startsWith("/wl"))
			return;

		if (!event.getPlayer().equals(_host))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("MPS", "You do not have permission to whitelist players. Ask the MPS host"));
			return;
		}
		if (isCommunityServer() || !isPrivateServer())
		{
			event.setCancelled(true);
			return;
		}

		event.setCancelled(true);
		
		String[] args = event.getMessage().split(" ");
		
		for (int i = 1; i < args.length; i++)
		{
			String name = args[i].toLowerCase();
			
			if (_whitelist.add(name))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Host", "Added " + F.elem(args[i]) + " to the whitelist."));
			}
		}
	}
	
	public boolean hasPermission(Permission permission)
	{
		if (isCommunityServer())
		{
			return PermissionGroup.ETERNAL.hasPermission(permission);
		}

		return _hostRank != null && _hostRank.hasPermission(permission);
	}

	public List<GameType> getGames()
	{
		return _games;
	}

	public void ban(Player player)
	{
		_blacklist.add(player.getName());
		
		if (isCommunityServer())
		{
			Manager.GetPortal().sendToHub(player, "You were removed from this Mineplex Community Server.", Intent.KICK);
		}
		else
		{
			Manager.GetPortal().sendToHub(player, "You were removed from this Mineplex Private Server.", Intent.KICK);
		}
	}
	
	@EventHandler
	public void kickBlacklist(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_blacklist.contains(player.getName()))
			{
				if (isCommunityServer())
				{
					Manager.GetPortal().sendToHub(player, "You were removed from this Mineplex Community Server.", Intent.KICK);
				}
				else
				{
					Manager.GetPortal().sendToHub(player, "You were removed from this Mineplex Private Server.", Intent.KICK);
				}
			}
		}
	}
	
	@EventHandler
	public void onDisband(CommunityDisbandEvent event)
	{
		if (isCommunityServer())
		{
			if (getOwner().getId() == event.getCommunity().getId())
			{
				setHostExpired(true, getOwner().getName() + " has disbanded and abandoned the server. Thanks for playing!");
			}
		}
	}

	public void giveAdmin(Player player)
	{
		_adminList.add(player.getName());
		_onlineAdmins.add(player);
		UtilPlayer.message(player, F.main("Server", "You were given Co-Host privileges."));
		
		if (isEventServer())
			worldeditPermissionSet(player, true);
	}

	public void removeAdmin(String playerName)
	{
		_adminList.remove(playerName);
		Player player = UtilPlayer.searchExact(playerName);
		if (player != null)
		{
			_onlineAdmins.remove(player);
			removeAdminItem(player);
			if (_shop.isPlayerInShop(player))
			{
				player.closeInventory();
			}
			UtilPlayer.message(player, F.main("Server", "Your Co-Host privileges were removed."));
						
			if (isEventServer())
			{
				player.setGameMode(GameMode.SURVIVAL);
				worldeditPermissionSet(player, false);
			}
		}
	}
	
	public boolean isAdminOnline()
	{
		return _onlineAdmins.isEmpty();
	}
	
	public void setDefaultConfig()
	{
		Manager.GetServerConfig().HotbarInventory = false;
		
		Manager.GetServerConfig().RewardAchievements = false;
		Manager.GetServerConfig().RewardGems = false;
		Manager.GetServerConfig().RewardItems = false;
		Manager.GetServerConfig().RewardStats = false;
		
		Manager.GetServerConfig().GameAutoStart = true;
		Manager.GetServerConfig().GameTimeout = true;
		Manager.GetServerConfig().PlayerKickIdle = true;
		Manager.GetServerConfig().TeamForceBalance = true;
	}

	public int getMaxPlayerCap()
	{
		if (isCommunityServer())
		{
			return 20;
		}
		if (hasPermission(Perm.INCREASE_MAX_PLAYERS_100))
		{
			return 100;
		}
		else if (hasPermission(Perm.INCREASE_MAX_PLAYERS_60))
		{
			return 60;
		}
		
		return 40;
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (!isPrivateServer())
			return;
		
		String serverName = Manager.getPlugin().getConfig().getString("serverstatus.name");
		if (!isCommunityServer())
		{
			UtilPlayer.message(event.getPlayer(), ChatColor.BOLD + "Welcome to Mineplex Private Servers!");
		}
		else
		{
			UtilPlayer.message(event.getPlayer(), ChatColor.BOLD + "Welcome to Mineplex Community Servers!");
		}
		UtilPlayer.message(event.getPlayer(), C.Bold + "Friends can connect with " + C.cGreen + C.Bold + "/server " + serverName);
	}
	
	public boolean isCommunityServer()
	{
		return Manager.GetHost() != null && Manager.GetHost().startsWith("COM-");
	}
	
	public Community getOwner()
	{
		if (!isCommunityServer())
		{
			return null;
		}
		return Managers.get(CommunityManager.class).getLoadedCommunity(Integer.parseInt(Manager.GetHost().replace("COM-", "")));
	}
	
	public boolean isEventServer()
	{
		return _isEventServer;
	}
	
	public void setEventServer(boolean var)
	{
		_isEventServer = var;
	}

	public Map<String, GameType> getVotes()
	{
		return _votes;
	}

	public void setVoteInProgress(boolean voteInProgress)
	{
		_voteInProgress = voteInProgress;
	}

	public boolean isVoteInProgress()
	{
		return _voteInProgress;
	}
	
	public PermissionGroup getHostRank()
	{
		return _hostRank;
	}
	
	public void setHostRank(PermissionGroup group)
	{
		_hostRank = group;
	}
	
	public Player getHost()
	{
		return _host;
	}
	
	public void setHost(Player player)
	{
		_host = player;
	}	
}