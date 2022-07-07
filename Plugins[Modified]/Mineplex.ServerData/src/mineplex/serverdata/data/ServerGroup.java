package mineplex.serverdata.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mineplex.serverdata.Region;

public class ServerGroup 
{
	private HashMap<String, String> _dataMap = null;
	
	private String _name;
	private String _host;
	private String _prefix;

	private int _minPlayers;
	private int _maxPlayers;
	
	private int _requiredRam;
	private int _requiredCpu;
	private int _requiredTotalServers;
	private int _requiredJoinableServers;

	private String _uptimes = "";

	private boolean _arcadeGroup;
	private String _worldZip;
	private String _plugin;
	private String _configPath;
	private int _portSection;
	
	private boolean _pvp;
	private boolean _tournament;
	private boolean _tournamentPoints;
	private boolean _teamRejoin;
	private boolean _teamAutoJoin;
	private boolean _teamForceBalance;
	
	private boolean _gameAutoStart;
	private boolean _gameTimeout;
	private boolean _gameVoting, _mapVoting;
	private boolean _rewardGems;
	private boolean _rewardItems;
	private boolean _rewardStats;
	private boolean _rewardAchievements;

	private boolean _hotbarInventory;
	private boolean _hotbarHubClock;
	private boolean _playerKickIdle;
	private boolean _hardMaxPlayerCap;
	
	private String _games;
	private String _modes;
	private String _boosterGroup;
	private String _serverType;	
	private boolean _addNoCheat;
	private boolean _addWorldEdit;
	private boolean _whitelist;
	private boolean _staffOnly;
	private String _resourcePack = "";
	
	private String _npcName = "";
	private String _portalBottomCornerLocation = "";
	private String _portalTopCornerLocation = "";
	
	private String _teamServerKey = "";
	
	private Region _region;
	
	private Set<MinecraftServer> _servers;
	
	public ServerGroup(Map<String, String> data, Collection<MinecraftServer> serverStatuses)
	{
		_name = data.get("name");
		_prefix = data.get("prefix");
		_requiredRam = Integer.valueOf(data.get("ram"));
		_requiredCpu = Integer.valueOf(data.get("cpu"));
		_requiredTotalServers = Integer.valueOf(data.get("totalServers"));
		_requiredJoinableServers = Integer.valueOf(data.get("joinableServers"));
		_portSection = Integer.valueOf(data.get("portSection"));
		_uptimes = data.getOrDefault("uptimes", "");
		_arcadeGroup = Boolean.valueOf(data.get("arcadeGroup"));
		_worldZip = data.get("worldZip");
		_plugin = data.get("plugin");
		_configPath = data.get("configPath");
		_minPlayers = Integer.valueOf(data.get("minPlayers"));
		_maxPlayers = Integer.valueOf(data.get("maxPlayers"));
		_pvp = Boolean.valueOf(data.get("pvp"));
		_tournament = Boolean.valueOf(data.get("tournament"));
		_tournamentPoints = Boolean.valueOf(data.get("tournamentPoints"));
		_hardMaxPlayerCap = Boolean.valueOf(data.get("hardMaxPlayerCap"));
		_games = data.get("games");
		_modes = data.get("modes");
		_boosterGroup = data.get("boosterGroup");
		_serverType = data.get("serverType");
		_addNoCheat = Boolean.valueOf(data.get("addNoCheat"));
		_addWorldEdit = Boolean.valueOf(data.get("addWorldEdit"));
		_teamRejoin = Boolean.valueOf(data.get("teamRejoin"));
		_teamAutoJoin = Boolean.valueOf(data.get("teamAutoJoin"));
		_teamForceBalance = Boolean.valueOf(data.get("teamForceBalance"));
		_gameAutoStart = Boolean.valueOf(data.get("gameAutoStart"));
		_gameTimeout = Boolean.valueOf(data.get("gameTimeout"));
		_gameVoting = Boolean.valueOf(data.get("gameVoting"));
		_mapVoting = Boolean.valueOf(data.get("mapVoting"));
		_rewardGems = Boolean.valueOf(data.get("rewardGems"));
		_rewardItems = Boolean.valueOf(data.get("rewardItems"));
		_rewardStats = Boolean.valueOf(data.get("rewardStats"));
		_rewardAchievements = Boolean.valueOf(data.get("rewardAchievements"));
		_hotbarInventory = Boolean.valueOf(data.get("hotbarInventory"));
		_hotbarHubClock = Boolean.valueOf(data.get("hotbarHubClock"));
		_playerKickIdle = Boolean.valueOf(data.get("playerKickIdle"));
		_staffOnly = Boolean.valueOf(data.get("staffOnly"));
		_whitelist = Boolean.valueOf(data.get("whitelist"));
		_resourcePack = data.getOrDefault("resourcePack", "");
		_host = data.get("host");
		_region = data.containsKey("region") ? Region.valueOf(data.get("region")) : Region.ALL;
		_teamServerKey = data.getOrDefault("teamServerKey", "");
		_portalBottomCornerLocation = data.getOrDefault("portalBottomCornerLocation", "");
		_portalTopCornerLocation = data.getOrDefault("portalTopCornerLocation", "");
		_npcName = data.getOrDefault("npcName", "");
		
		if (serverStatuses != null)
			parseServers(serverStatuses);
	}
	
	public ServerGroup(String name, String prefix, String host, int ram, int cpu, int totalServers, int joinable, int portSection, String uptimes, boolean arcade, String worldZip, String plugin, String configPath
			, int minPlayers, int maxPlayers, boolean pvp, boolean tournament, boolean tournamentPoints, String games, String modes, String boosterGroup, String serverType, boolean noCheat, boolean worldEdit, boolean teamRejoin
			, boolean teamAutoJoin, boolean teamForceBalance, boolean gameAutoStart, boolean gameTimeout, boolean gameVoting, boolean mapVoting, boolean rewardGems, boolean rewardItems, boolean rewardStats
			, boolean rewardAchievements, boolean hotbarInventory, boolean hotbarHubClock, boolean playerKickIdle, boolean hardMaxPlayerCap, boolean staffOnly, boolean whitelist, String resourcePack, Region region
			, String teamServerKey, String portalBottomCornerLocation, String portalTopCornerLocation, String npcName)
	{
		_name = name;
		_prefix = prefix;
		_host = host;
		_requiredRam = ram;
		_requiredCpu = cpu;
		_requiredTotalServers = totalServers;
		_requiredJoinableServers = joinable;
		_portSection = portSection;
		_uptimes = uptimes;
		_arcadeGroup = arcade;
		_worldZip = worldZip;
		_plugin = plugin;
		_configPath = configPath;
		_minPlayers = minPlayers;
		_maxPlayers = maxPlayers;
		_pvp = pvp;
		_tournament = tournament;
		_tournamentPoints = tournamentPoints;
		_games = games;
		_modes = modes;
		_boosterGroup = boosterGroup;
		_serverType = serverType;
		_addNoCheat = noCheat;
		_addWorldEdit = worldEdit;
		_teamRejoin = teamRejoin;
		_teamAutoJoin = teamAutoJoin;
		_teamForceBalance = teamForceBalance;
		_gameAutoStart = gameAutoStart;
		_gameTimeout = gameTimeout;
		_gameVoting = gameVoting;
		_mapVoting = mapVoting;
		_rewardGems = rewardGems;
		_rewardItems = rewardItems;
		_rewardStats = rewardStats;
		_rewardAchievements = rewardAchievements;
		_hotbarInventory = hotbarInventory;
		_hotbarHubClock = hotbarHubClock;
		_playerKickIdle = playerKickIdle;
		_hardMaxPlayerCap = hardMaxPlayerCap;
		_staffOnly = staffOnly;
		_whitelist = whitelist;
		_resourcePack = resourcePack;
		_region = region;
		_teamServerKey = teamServerKey;
		_portalBottomCornerLocation = portalBottomCornerLocation;
		_portalTopCornerLocation = portalTopCornerLocation;
		_npcName = npcName;
	}
	
	public ServerGroup(String name, String npcName, String prefix)
	{
		_name = name;
		_npcName = npcName;
		_prefix = prefix;
	}
	
	public String getName() { return _name; }
	public String getHost() { return _host; }
	public String getPrefix() { return _prefix; }
	
	public int getMinPlayers() { return _minPlayers; }
	public int getMaxPlayers() { return _maxPlayers; }
	
	public int getRequiredRam() { return _requiredRam; }
	public int getRequiredCpu() { return _requiredCpu; }
	public int getRequiredTotalServers() { return _requiredTotalServers; }
	public int getRequiredJoinableServers() { return _requiredJoinableServers; }	
	public int getPortSection() { return _portSection; }
	
	public boolean getArcadeGroup() { return _arcadeGroup; }
	public String getWorldZip() { return _worldZip; }
	public String getPlugin() { return _plugin; }
	public String getConfigPath() { return _configPath; }

	public boolean getPvp() { return _pvp; }
	public boolean getTournament() { return _tournament; }
	public boolean getTournamentPoints() { return _tournamentPoints; }
	public boolean getTeamRejoin() { return _teamRejoin; }
	public boolean getTeamAutoJoin() { return _teamAutoJoin; }
	
	public boolean getTeamForceBalance() { return _teamForceBalance; }
	public boolean getGameAutoStart() { return _gameAutoStart; }
	public boolean getGameTimeout() { return _gameTimeout; }
	public boolean getGameVoting() { return _gameVoting; }
	public boolean getMapVoting() { return _mapVoting; }
	public boolean getRewardGems() { return _rewardGems; }
	public boolean getRewardItems() { return _rewardItems; }
	public boolean getRewardStats() { return _rewardStats; }
	public boolean getRewardAchievements() { return _rewardAchievements; }

	public boolean getHotbarInventory() { return _hotbarInventory; }
	public boolean getHotbarHubClock() { return _hotbarHubClock; }
	public boolean getPlayerKickIdle() { return _playerKickIdle; }
	public boolean getHardMaxPlayerCap() { return _hardMaxPlayerCap; }
	
	public String getGames() { return _games; }
	public String getModes() { return _modes; }
	public String getBoosterGroup() { return _boosterGroup; }

	public String getServerType() { return _serverType; }
	public boolean getAddNoCheat() { return _addNoCheat; }
	public boolean getAddWorldEdit() { return _addWorldEdit; }
	public boolean getWhitelist() { return _whitelist; }
	public boolean getStaffOnly() { return _staffOnly; }
	public String getResourcePack() { return _resourcePack; }	
	public Region getRegion() { return _region; }
	
	public String getTeamServerKey() { return _teamServerKey; }
	
	public String getServerNpcName() { return _npcName; }
	public String getPortalBottomCornerLocation() { return _portalBottomCornerLocation; }
	public String getPortalTopCornerLocation() { return _portalTopCornerLocation; }
	public String getUptimes() { return _uptimes; }
	
	public Set<MinecraftServer> getServers() { return _servers; }
	
	public int getServerCount()
	{
		return _servers.size();
	}
	
	public int getJoinableCount()
	{
		int joinable = 0;
		
		for (MinecraftServer server : _servers)
		{
			if (server.isJoinable())
			{
				joinable++;
			}
		}
		
		return joinable;
	}
	
	public int getPlayerCount()
	{
		int playerCount = 0;
		
		for (MinecraftServer server : _servers)
		{
			playerCount += server.getPlayerCount();
		}
		
		return playerCount;
	}
	
	public int getMaxPlayerCount()
	{
		int maxPlayerCount = 0;
		
		for (MinecraftServer server : _servers)
		{
			maxPlayerCount += server.getMaxPlayerCount();
		}
		
		return maxPlayerCount;
	}
	
	public Collection<MinecraftServer> getEmptyServers()
	{
		Collection<MinecraftServer> emptyServers = new HashSet<MinecraftServer>();
		
		for (MinecraftServer server : _servers)
		{
			if (server.isEmpty() && server.getUptime() >= 150)	// Only return empty servers that have been online for >150 seconds
			{
				emptyServers.add(server);
			}
		}
		
		return emptyServers;
	}
	
	private void parseServers(Collection<MinecraftServer> servers)
	{
		_servers = new HashSet<>();

		for (MinecraftServer server : servers)
		{
			if (_name.equalsIgnoreCase(server.getGroup()))
			{
				_servers.add(server);
			}
		}
	}
	
	public int generateUniqueId(int startId)
	{
		int id = startId;
		
		while (true)
		{
			boolean uniqueId = true;
			
			for (MinecraftServer server : _servers)
			{
				String serverName = server.getName();
				try
				{
					String[] nameArgs = serverName.split("-");
					int serverNum = Integer.parseInt(nameArgs[nameArgs.length - 1]);
					
					if (serverNum == id)
					{
						uniqueId = false;
						break;
					}
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			}
			
			if (uniqueId)
			{
				return id;
			}
			else
			{
				id++;
			}
		}
	}

	public HashMap<String, String> getDataMap()
	{
		if (_dataMap == null)
		{
			_dataMap = new HashMap<>();
			
			_dataMap.put("name", _name);
			_dataMap.put("prefix", _prefix);
			_dataMap.put("ram", _requiredRam + "");
			_dataMap.put("cpu", _requiredCpu + "");
			_dataMap.put("totalServers", _requiredTotalServers + "");
			_dataMap.put("joinableServers", _requiredJoinableServers + "");
			_dataMap.put("portSection", _portSection + "");
			_dataMap.put("uptimes", _uptimes);
			_dataMap.put("arcadeGroup", _arcadeGroup + "");
			_dataMap.put("worldZip", _worldZip);
			_dataMap.put("plugin", _plugin);
			_dataMap.put("configPath", _configPath);
			_dataMap.put("minPlayers", _minPlayers + "");
			_dataMap.put("maxPlayers", _maxPlayers + "");
			_dataMap.put("pvp", _pvp + "");
			_dataMap.put("tournament", _tournament + "");
			_dataMap.put("tournamentPoints", _tournamentPoints + "");
			_dataMap.put("games", _games);
			_dataMap.put("modes", _modes);
			_dataMap.put("serverType", _serverType);
			_dataMap.put("addNoCheat", _addNoCheat + "");
			_dataMap.put("teamRejoin", _teamRejoin + "");
			_dataMap.put("teamAutoJoin", _teamAutoJoin + "");
			_dataMap.put("teamForceBalance", _teamForceBalance + "");
			_dataMap.put("gameAutoStart", _gameAutoStart + "");
			_dataMap.put("gameTimeout", _gameTimeout + "");
			_dataMap.put("gameVoting", String.valueOf(_gameVoting));
			_dataMap.put("mapVoting", String.valueOf(_mapVoting));
			_dataMap.put("rewardGems", _rewardGems + "");
			_dataMap.put("rewardItems", _rewardItems + "");
			_dataMap.put("rewardStats", _rewardStats + "");
			_dataMap.put("rewardAchievements", _rewardAchievements + "");
			_dataMap.put("hotbarInventory", _hotbarInventory + "");
			_dataMap.put("hotbarHubClock", _hotbarHubClock + "");
			_dataMap.put("playerKickIdle", _playerKickIdle + "");
			_dataMap.put("staffOnly", _staffOnly + "");
			_dataMap.put("whitelist", _whitelist + "");
			_dataMap.put("resourcePack", _resourcePack);
			_dataMap.put("host", _host);
			_dataMap.put("region", _region.name());
			_dataMap.put("teamServerKey", _teamServerKey);
			_dataMap.put("boosterGroup", _boosterGroup);
			_dataMap.put("portalBottomCornerLocation", _portalBottomCornerLocation);
			_dataMap.put("portalTopCornerLocation", _portalTopCornerLocation);
			_dataMap.put("npcName", _npcName);
		}
		
		return _dataMap;
	}
}
