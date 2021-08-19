package mineplex.core.communities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.Chat;
import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.communities.commands.CommunityCommand;
import mineplex.core.communities.data.BrowserCommunity;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.CommunityJoinRequestInfo;
import mineplex.core.communities.data.CommunityMemberData;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.data.CommunitySetting;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.communities.events.CommunityBrowserUpdateEvent;
import mineplex.core.communities.events.CommunityDisbandEvent;
import mineplex.core.communities.events.CommunityJoinRequestsUpdateEvent;
import mineplex.core.communities.events.CommunityMemberDataUpdateEvent;
import mineplex.core.communities.events.CommunityMembershipUpdateEvent;
import mineplex.core.communities.events.CommunityNameUpdateEvent;
import mineplex.core.communities.events.CommunitySettingUpdateEvent;
import mineplex.core.communities.redis.CommunityChat;
import mineplex.core.communities.redis.CommunityChatHandler;
import mineplex.core.communities.redis.CommunityCloseJoinRequest;
import mineplex.core.communities.redis.CommunityCloseJoinRequestHandler;
import mineplex.core.communities.redis.CommunityCreate;
import mineplex.core.communities.redis.CommunityCreateHandler;
import mineplex.core.communities.redis.CommunityDisband;
import mineplex.core.communities.redis.CommunityDisbandHandler;
import mineplex.core.communities.redis.CommunityInvite;
import mineplex.core.communities.redis.CommunityInviteHandler;
import mineplex.core.communities.redis.CommunityJoinRequest;
import mineplex.core.communities.redis.CommunityJoinRequestHandler;
import mineplex.core.communities.redis.CommunityUnInvite;
import mineplex.core.communities.redis.CommunityUnInviteHandler;
import mineplex.core.communities.redis.CommunityUpdateMemberChatReading;
import mineplex.core.communities.redis.CommunityUpdateMemberChatReadingHandler;
import mineplex.core.communities.redis.CommunityUpdateMemberRole;
import mineplex.core.communities.redis.CommunityUpdateMemberRoleHandler;
import mineplex.core.communities.redis.CommunityUpdateMembership;
import mineplex.core.communities.redis.CommunityUpdateMembershipHandler;
import mineplex.core.communities.redis.CommunityUpdateName;
import mineplex.core.communities.redis.CommunityUpdateNameHandler;
import mineplex.core.communities.redis.CommunityUpdateSetting;
import mineplex.core.communities.redis.CommunityUpdateSettingHandler;
import mineplex.core.customdata.CustomDataManager;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerCommandManager;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

@ReflectivelyCreateMiniPlugin
public class CommunityManager extends MiniDbClientPlugin<CommunityMemberData>
{
	public enum Perm implements Permission
	{
		OWN_COMMUNITY,
		COMMUNITY_CHAT_COMMAND,
		COMMUNITY_COLEAD_COMMAND,
		COMMUNITY_COMMAND,
		COMMUNITY_DESCRIPTION_COMMAND,
		COMMUNITY_DESCRIPTION_STAFF_COMMAND,
		COMMUNITY_DISBAND_COMMAND,
		COMMUNITY_DISBAND_STAFF_COMMAND,
		COMMUNITY_INVITE_COMMAND,
		COMMUNITY_INVITE_STAFF_COMMAND,
		COMMUNITY_JOIN_COMMAND,
		COMMUNITY_MCS_COMMAND,
		COMMUNITY_MCS_STAFF_COMMAND,
		COMMUNITY_MENU_COMMAND,
		COMMUNITY_RENAME_COMMAND,
		COMMUNITY_RENAME_STAFF_COMMAND,
		COMMUNITY_UNINVITE_COMMAND,
		COMMUNITY_UNINVITE_STAFF_COMMAND,
	}

	public final static String COMMUNITY_CHAT_KEY = "core.communities.chat.selected";
	private final static int MAX_NAME_LENGTH = 15;

	private static final int UPDATE_CYCLE_SECONDS = 10; // The number of seconds between dirty communities refreshes
	private static final int CACHE_INVALIDATION_SECONDS = 300; // The number of seconds between full communities refreshes
	private final Pattern VALID_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]{1," + MAX_NAME_LENGTH + "}$");
	public final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^A-Za-z0-9]");
	public final List<String> BLOCKED_NAMES = Arrays.asList("help", "chat", "create", "description", "disband", "invite", "join", "mcs", "rename", "uninvite", "trainee", "mod", "moderator", "srmod", "seniormod", "seniormoderator", "builder", "maplead", "twitch", "youtube", "support", "admin", "administrator", "leader", "dev", "developer", "owner", "party", "mineplex", "mineplexofficial", "staff", "mineplexstaff", "qualityassurance", "traineemanagement", "modcoordination", "forumninja", "communitymanagement", "event", "socialmedia");

	private final CommunityRepository _repo;
	private final Map<Integer, Community> _loadedCommunities = new ConcurrentHashMap<>();
	private final Map<Integer, BrowserCommunity> _browserCommunities = new ConcurrentHashMap<>();

	private final Random _rand = new Random();
	private final List<Integer> _browserIds = new LinkedList<>();

	private final List<UUID> _creating = new ArrayList<>();

	private Integer _mcsCommunity = null;
	private Region _region;

	private final Set<Community> _dirty = Collections.newSetFromMap(new ConcurrentHashMap<>()); // Communities with redis updates

	private int _updateCycleCount; // The number of update cycles since we've updated all communities
	private volatile boolean _cycling = false;

	private Chat _chat;
	private PreferencesManager _prefManager;
	private CustomDataManager _customDataManager;
	
	private Chat getChat()
	{
		// the mini plugin contract is a lie
		if (_chat == null)
		{
			_chat = require(Chat.class);
		}
		
		return _chat;
	}
	
	private PreferencesManager getPrefManager()
	{
		if (_prefManager == null)
		{
			_prefManager = require(PreferencesManager.class);
		}
		
		return _prefManager;
	}
	
	public CustomDataManager getCustomDataManager()
	{
		if (_customDataManager == null)
		{
			_customDataManager = require(CustomDataManager.class);
		}
		
		return _customDataManager;
	}

	@SuppressWarnings({"deprecation", "unchecked"})
	private CommunityManager()
	{
		super("Communities");

		RedisDataRepository<PlayerStatus> statusRepo = new RedisDataRepository<>(ServerManager.getMasterConnection(),
				ServerManager.getSlaveConnection(), Region.currentRegion(), PlayerStatus.class, "playerStatus");
		
		_region = Region.currentRegion();
		_repo = new CommunityRepository(statusRepo, _region);

		runAsync(() ->
		{
			_repo.loadBrowserIds(_browserIds);
			log("Loaded " + _browserIds.size() + " communities to show in browser");
		});

		_chat = require(Chat.class);
		_prefManager = require(PreferencesManager.class);
		_customDataManager = require(CustomDataManager.class);

		getClientManager().addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return "community-invite-loader";
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				CommunityMemberData data = Get(uuid);
				while (resultSet.next())
				{
					String region = resultSet.getString("region");
					if (region.equalsIgnoreCase(_region.name()))
					{
						data.Invites.add(resultSet.getInt("communityId"));
					}
				}

				if (!data.Invites.isEmpty())
				{
					runAsync(() -> loadBrowserCommunities(data.Invites,  null));
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT ci.communityId, c.region FROM communityInvites AS ci INNER JOIN communities AS c ON c.id=ci.communityId WHERE accountId=" + accountId + ";";
			}
		});
		
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(_plugin, this::runUpdateCycle, 0L, 20 * UPDATE_CYCLE_SECONDS);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(_plugin, this::cycleBrowser, 0L, 20 * 30);

		addCommand(new CommunityCommand(this));
		
		ServerCommandManager.getInstance().registerCommandType(CommunityChat.class, new CommunityChatHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityCloseJoinRequest.class, new CommunityCloseJoinRequestHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityCreate.class, new CommunityCreateHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityDisband.class, new CommunityDisbandHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityInvite.class, new CommunityInviteHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityJoinRequest.class, new CommunityJoinRequestHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUnInvite.class, new CommunityUnInviteHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUpdateMemberChatReading.class, new CommunityUpdateMemberChatReadingHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUpdateMemberRole.class, new CommunityUpdateMemberRoleHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUpdateMembership.class, new CommunityUpdateMembershipHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUpdateName.class, new CommunityUpdateNameHandler(this));
		ServerCommandManager.getInstance().registerCommandType(CommunityUpdateSetting.class, new CommunityUpdateSettingHandler(this));

		// Load and keep community for MCS
		ServerGroup group = require(ServerConfiguration.class).getServerGroup();
		if (group.getName().startsWith("COM-"))
		{
			_mcsCommunity = Integer.valueOf(group.getName().split("-")[1]);

			Community community = getLoadedCommunity(_mcsCommunity);
			if (community == null)
			{
				_repo.loadCommunity(_loadedCommunities, _mcsCommunity);
			}
		}

		generatePermissions();
	}

	private void runUpdateCycle()
	{
		_updateCycleCount++;

		if (_cycling)
		{
			return;
		}

		List<Community> communities = new ArrayList<>();
		if (UPDATE_CYCLE_SECONDS * _updateCycleCount > CACHE_INVALIDATION_SECONDS)
		{
			// It's been five minutes since a full update; update all communities
			_updateCycleCount = 0;
			_dirty.clear();

			communities.addAll(_loadedCommunities.values());
		}
		else
		{
			communities.addAll(_dirty);
			_dirty.clear();
		}

		updateCommunities(communities);
	}

	private void updateCommunities(List<Community> communities)
	{
		_repo.updateMembersAndJoinRequests(communities);
	}

	private void generatePermissions()
	{
		PermissionGroup.ETERNAL.setPermission(Perm.OWN_COMMUNITY, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_CHAT_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_COLEAD_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_DESCRIPTION_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_DESCRIPTION_STAFF_COMMAND, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.COMMUNITY_DISBAND_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_DISBAND_STAFF_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_INVITE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_INVITE_STAFF_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_JOIN_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_MCS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_MCS_STAFF_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_MENU_COMMAND, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.COMMUNITY_RENAME_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_RENAME_STAFF_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.COMMUNITY_UNINVITE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.COMMUNITY_UNINVITE_STAFF_COMMAND, true, true);
	}

	public boolean isNameValid(String communityName)
	{
		return VALID_NAME_PATTERN.matcher(communityName).find();
	}

	public boolean isNameAllowed(Player caller, String communityName)
	{
		return !getChat().filterMessage(caller, communityName).contains("*");
	}

	public void communityExists(String name, Consumer<Boolean> result)
	{
		_repo.communityExists(name, result);
	}

	public boolean ownsCommunity(UUID uuid)
	{
		return _loadedCommunities.values().stream()
				.flatMap(community -> community.getMembers().entrySet().stream())
				.anyMatch(entry -> entry.getKey().equals(uuid) && entry.getValue().Role == CommunityRole.LEADER);
	}
	
	private void cycleBrowser()
	{
		if (_cycling)
		{
			return;
		}
		_cycling = true;
		runAsync(() ->
		{
			Collections.shuffle(_browserIds, _rand);
			
			runSync(() ->
			{
				_cycling = false;
				UtilServer.CallEvent(new CommunityBrowserUpdateEvent());
			});
		});
	}

	public List<Integer> getBrowserIds()
	{
		return _browserIds;
	}

	public void loadBrowserCommunities(final List<Integer> displaying, final Runnable onComplete)
	{
		final List<Integer> load = new ArrayList<>(displaying.size());
		for (Integer id : displaying)
		{
			if (!_loadedCommunities.containsKey(id) && !_browserCommunities.containsKey(id))
			{
				load.add(id);
			}
		}

		if (!load.isEmpty())
		{
			runAsync(() ->
			{
				_repo.loadBrowserCommunities(_browserCommunities, load);

				if (onComplete != null)
				{
					runSync(onComplete);
				}
			});
		} else if (onComplete != null)
		{
			runSync(onComplete);
		}
	}

	public void tempLoadCommunity(final int id, final Consumer<Community> consumer)
	{
		Community community = _loadedCommunities.get(id);
		if (community != null)
		{
			consumer.accept(community);
			return;
		}

		runAsync(() ->
		{
			try
			{
				final Map<Integer, Community> store = new HashMap<>();
				_repo.loadCommunity(store, id);

				if (!store.isEmpty())
				{
					runSync(() -> consumer.accept(store.get(id)));
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		});
	}

	public ICommunity getBrowserCommunity(int id)
	{
		ICommunity community = _loadedCommunities.get(id);
		return community != null ? community : _browserCommunities.get(id);
	}

	private void updateBrowserStatus(ICommunity community)
	{
		_repo.updateBrowserStatus(community, community.isBrowserEligible());
	}

	public int getCount()
	{
		return _loadedCommunities.size();
	}

	public Community getLoadedCommunity(Integer id)
	{
		return _loadedCommunities.get(id);
	}

	public ICommunity getCommunity(String name)
	{
		return getByName(_loadedCommunities, name, getByName(_browserCommunities, name, null));
	}

	public Community getLoadedCommunity(String name)
	{
		return (Community) getByName(_loadedCommunities, name, null);
	}

	private ICommunity getByName(Map<Integer, ? extends ICommunity> map, String name, ICommunity def)
	{
		for (Entry<Integer, ? extends ICommunity> entry : map.entrySet())
		{
			if (entry.getValue().getName().equalsIgnoreCase(name))
			{
				return entry.getValue();
			}
		}

		return def;
	}

	public void handleCommunitySettingUpdate(Integer id, String sender, CommunitySetting setting, String newValue)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		setting.parseValueInto(newValue, community);
		//community.message(F.main(getName(), F.name(sender) + " has changed settings in " + F.name(community.getName()) + "!"));
		runSync(() -> UtilServer.CallEvent(new CommunitySettingUpdateEvent(community)));
	}
	
	public void handleCommunityNameUpdate(Integer id, String sender, String name)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		String oldName = community.getName();
		community.setName(name);
		community.message(F.main(getName(), F.name(sender) + " has changed the name of " + F.name(oldName) + " to " + F.name(community.getName()) + "!"));
		runSync(() -> UtilServer.CallEvent(new CommunityNameUpdateEvent(community)));
	}
	
	public void handleCommunityMembershipRoleUpdate(Integer id, String sender, UUID uuid, CommunityRole role)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		CommunityMemberInfo member = community.getMembers().get(uuid);
		member.updateRole(role);
		if (Bukkit.getPlayer(uuid) != null)
		{
			if (role.ordinal() > CommunityRole.COLEADER.ordinal())
			{
				UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(getName(), F.name(sender) + " has changed your role to " + F.elem(role.getDisplay()) + " in " + F.name(community.getName()) + "!"));
			}
			Get(Bukkit.getPlayer(uuid)).setRoleIn(community, role);
		}
		String name = member.Name;
		community.message(F.main(getName(), F.name(sender) + " has changed " + F.name(name + "'s") + " role to " + F.elem(role.getDisplay()) + " in " + F.name(community.getName()) + "!"), CommunityRole.COLEADER);
		runSync(() -> UtilServer.CallEvent(new CommunityMembershipUpdateEvent(community)));
	}
	
	public void handleCommunityMembershipUpdate(Integer id, String sender, String playerName, UUID playerUUID, Integer accountId, boolean kick, boolean leave)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		
		if (kick)
		{
			community.message(F.main(getName(), F.name(sender) + " has kicked " + F.name(playerName) + " from " + F.name(community.getName()) + "!"));
			community.getMembers().remove(playerUUID);
			if (Bukkit.getPlayer(playerUUID) != null)
			{
				Get(Bukkit.getPlayer(playerUUID)).leaveCommunity(community);
			}
		}
		else if (leave)
		{
			community.message(F.main(getName(), F.name(playerName) + " has left " + F.name(community.getName()) + "!"));
			community.getMembers().remove(playerUUID);
			if (Bukkit.getPlayer(playerUUID) != null)
			{
				Get(Bukkit.getPlayer(playerUUID)).leaveCommunity(community);
			}
		}
		else
		{
			community.getMembers().put(playerUUID, new CommunityMemberInfo(playerName, playerUUID, accountId, CommunityRole.MEMBER, System.currentTimeMillis()));
			if (Bukkit.getPlayer(playerUUID) != null)
			{
				Get(Bukkit.getPlayer(playerUUID)).joinCommunity(community);
				runSync(() -> Get(Bukkit.getPlayer(playerUUID)).Invites.remove(community.getId()));
			}
			
			community.message(F.main(getName(), F.name(playerName) + " has joined " + F.name(community.getName()) + "!"));
		}

		runSync(() ->
		{
			UtilServer.CallEvent(new CommunityMembershipUpdateEvent(community));
			if (Bukkit.getPlayer(playerUUID) != null)
			{
				UtilServer.CallEvent(new CommunityMemberDataUpdateEvent(Bukkit.getPlayer(playerUUID)));
			}
		});
	}
	
	public void handleCommunityInvite(Integer id, String sender, String targetName, UUID targetUUID)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		runSync(() ->
		{
			if (Bukkit.getPlayer(targetUUID) != null)
			{
				if (!Get(Bukkit.getPlayer(targetUUID)).Invites.contains(community.getId()))
				{
					Get(Bukkit.getPlayer(targetUUID)).Invites.add(community.getId());
					if (getPrefManager().get(Bukkit.getPlayer(targetUUID)).isActive(Preference.COMMUNITY_INVITES))
					{
						new JsonMessage(F.main(getName(), "You have been invited to join " + F.elem(community.getName()) + " by " + F.name(sender) + "! " + C.cGreen + "Click this message to join!")).click(ClickEvent.RUN_COMMAND, "/community join " + community.getName()).sendToPlayer(Bukkit.getPlayer(targetUUID));
					}

					UtilServer.CallEvent(new CommunityMemberDataUpdateEvent(Bukkit.getPlayer(targetUUID)));
				}
			}
		});
		community.message(F.main(getName(), F.name(sender) + " has invited " + F.name(targetName) + " to " + F.name(community.getName()) + "!"), CommunityRole.COLEADER);
	}
	
	public void handleCommunityUninvite(Integer id, String sender, String targetName, UUID targetUUID, boolean announce)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		runSync(() ->
		{
			if (Bukkit.getPlayer(targetUUID) != null)
			{
				Get(Bukkit.getPlayer(targetUUID)).Invites.remove(community.getId());
				if (getPrefManager().get(Bukkit.getPlayer(targetUUID)).isActive(Preference.COMMUNITY_INVITES) && announce)
				{
					UtilPlayer.message(Bukkit.getPlayer(targetUUID), F.main(getName(), "Your invitation to join " + F.elem(community.getName()) + " has been revoked by " + F.name(sender) + "!"));
				}
				UtilServer.CallEvent(new CommunityMemberDataUpdateEvent(Bukkit.getPlayer(targetUUID)));
			}
		});
		if (announce)
		{
			community.message(F.main(getName(), F.name(targetName) + "'s invitation to join " + F.name(community.getName()) + " has been revoked by " + F.name(sender) + "!"), CommunityRole.COLEADER);
		}
	}
	
	public void handleCommunityJoinRequest(Integer id, String playerName, UUID playerUUID, Integer accountId)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		if (Bukkit.getPlayer(playerUUID) != null)
		{
			UtilPlayer.message(Bukkit.getPlayer(playerUUID), F.main(getName(), "You have requested to join " + F.elem(community.getName()) + "!"));
		}
		community.getJoinRequests().put(playerUUID, new CommunityJoinRequestInfo(playerName, playerUUID, accountId));
		community.message(F.main(getName(), F.name(playerName) + " has requested to join " + F.name(community.getName()) + "!"), CommunityRole.COLEADER);
		
		runSync(() -> UtilServer.CallEvent(new CommunityJoinRequestsUpdateEvent(community)));
	}
	
	public void handleCommunityCloseJoinRequest(Integer id, String sender, String playerName, UUID playerUUID, Integer accountId, boolean announce)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		_dirty.add(community);
		community.getJoinRequests().remove(playerUUID);
		if (announce)
		{
			if (Bukkit.getPlayer(playerUUID) != null)
			{
				UtilPlayer.message(Bukkit.getPlayer(playerUUID), F.main(getName(), "Your request to join " + F.name(community.getName()) + " has been denied by " + F.name(sender) + "!"));
			}
			community.message(F.main(getName(), F.name(playerName) + "'s request to join " + F.name(community.getName()) + " has been denied by " + F.name(sender) + "!"), CommunityRole.COLEADER);
		}
		
		runSync(() -> UtilServer.CallEvent(new CommunityJoinRequestsUpdateEvent(community)));
	}
	
	public void handleCommunityCreation(Integer id, String name, Integer leaderId, UUID leaderUUID, String leaderName)
	{
		runAsync(() ->
		{
			_loadedCommunities.put(id, new Community(id, name));
			_loadedCommunities.get(id).getMembers().put(leaderUUID, new CommunityMemberInfo(leaderName, leaderUUID, leaderId, CommunityRole.LEADER, System.currentTimeMillis()));
			runSync(() ->
			{
				Community community = _loadedCommunities.get(id);
				_dirty.add(community);
				if (Bukkit.getPlayer(leaderUUID) != null)
				{
					Player leader = Bukkit.getPlayer(leaderUUID);
					UtilPlayer.message(leader, F.main(getName(), "You have created a community named " + F.name(community.getName()) + "!"));
					Get(leader).joinCommunity(id, CommunityRole.LEADER);
				}
			});
		});
	}
	
	public void handleCommunityDisband(Integer id, String senderName)
	{
		_browserCommunities.remove(id);
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		community.message(F.main(getName(), F.name(senderName) + " has disbanded community " + F.name(community.getName()) + "!"));
		UtilServer.CallEvent(new CommunityDisbandEvent(community));
		runSync(() ->
		{
			UtilServer.GetPlayers().stream().filter(player -> Get(player).Invites.contains(community.getId())).forEach(player -> Get(player).Invites.remove(community.getId()));
		});
		community.getMembers().keySet().stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(uuid -> Get(Bukkit.getPlayer(uuid)).leaveCommunity(community));
		_loadedCommunities.remove(community.getId());
		runSync(() ->
		{
			if (_browserIds.remove(community.getId()))
			{
				UtilServer.CallEvent(new CommunityBrowserUpdateEvent());
			}
		});
	}
	
	public void handleToggleReadingCommunityChat(Integer id, UUID uuid, boolean reading)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		
		if (community.getMembers().containsKey(uuid))
		{
			community.getMembers().get(uuid).ReadingChat = reading;
			if (reading)
			{
				UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(getName(), "You are now reading chat from " + F.name(community.getName()) + "!"));
			}
			else
			{
				UtilPlayer.message(Bukkit.getPlayer(uuid), F.main(getName(), "You are no longer reading chat from " + F.name(community.getName()) + "!"));
			}
		}
	}
	
	public void handleCommunityChat(Integer id, String senderName, String message)
	{
		Community community = _loadedCommunities.get(id);
		if (community == null)
		{
			return;
		}
		community.sendChat(community.getChatFormatting()[0] + C.Bold + community.getName() + " " + community.getChatFormatting()[1] + C.Bold + senderName + " " + community.getChatFormatting()[2] + message);
	}
	
	public void handleInvite(Player sender, Community community, String target)
	{
		CommunityJoinRequestInfo[] jr = community.getJoinRequests().values().stream().filter(data -> data.Name.equalsIgnoreCase(target)).toArray(CommunityJoinRequestInfo[]::new);
		if (jr.length == 1)
		{
			UtilPlayer.message(sender, F.main(getName(), "You have accepted " + F.name(target) + "'s join request to " + F.name(community.getName()) + "!"));
			runAsync(() ->
			{
				_repo.addToCommunity(jr[0].AccountId, community.getId());
				_repo.removeJoinRequest(community.getId(), jr[0].AccountId);
			});
			new CommunityCloseJoinRequest(community.getId(), sender.getName(), jr[0].Name, jr[0].UUID.toString(), jr[0].AccountId, false).publish();
			new CommunityUpdateMembership(community.getId(), sender.getName(), jr[0].Name, jr[0].UUID.toString(), jr[0].AccountId, false, false).publish();
			return;
		}
		runAsync(() ->
		{
			if (_repo.inviteToCommunity(community.getId(), target))
			{
				if (!community.getMembers().containsKey(sender.getUniqueId()))
				{
					UtilPlayer.message(sender, F.main(getName(), "You have invited " + F.name(target) + " to join " + F.name(community.getName()) + "!"));
				}
				new CommunityInvite(community.getId(), sender.getName(), target, getClientManager().loadUUIDFromDB(target).toString()).publish();
			}
			else
			{
				UtilPlayer.message(sender, F.main(getName(), F.name(target) + " does not exist!"));
			}
		});
	}
	
	public void handleUninvite(Player sender, Community community, String target)
	{
		runAsync(() ->
		{
			if (_repo.deleteInviteToCommunity(community.getId(), target))
			{
				if (!community.getMembers().containsKey(sender.getUniqueId()))
				{
					UtilPlayer.message(sender, F.main(getName(), "You have revoked " + F.name(target) + "'s invitation to join " + F.name(community.getName()) + "!"));
				}
				new CommunityUnInvite(community.getId(), sender.getName(), target, getClientManager().loadUUIDFromDB(target).toString(), true).publish();
			}
			else
			{
				UtilPlayer.message(sender, F.main(getName(), "Either " + F.name(target) + " does not exist or you have not invited them to " + F.name(community.getName()) + "!"));
			}
		});
	}
	
	public void handleRejectInvite(Player sender, int id)
	{
		final String playerName = getClientManager().Get(sender).getName();
		runAsync(() ->
		{
			_repo.deleteInviteToCommunity(id, playerName);
		});
		
		new CommunityUnInvite(id, sender.getName(), sender.getName(), sender.getUniqueId().toString(), false).publish();
	}
	
	public void handleJoinRequest(Player sender, Community community)
	{
		final int accountId = getClientManager().getAccountId(sender);
		
		if (Get(sender).Invites.contains(community.getId()))
		{
			String playerName = getClientManager().Get(sender).getName(); //Guarantee real name (important in this instance)
			runAsync(() ->
			{
				_repo.addToCommunity(accountId, community.getId());
				_repo.deleteInviteToCommunity(community.getId(), playerName);
			});
			new CommunityUnInvite(community.getId(), sender.getName(), sender.getName(), sender.getUniqueId().toString(), false).publish();
			new CommunityUpdateMembership(community.getId(), sender.getName(), sender.getName(), sender.getUniqueId().toString(), accountId, false, false).publish();
			return;
		}
		runAsync(() ->
		{
			_repo.addJoinRequest(community.getId(), accountId);
		});
		new CommunityJoinRequest(community.getId(), sender.getName(), sender.getUniqueId().toString(), accountId).publish();
	}
	
	public void handleCloseJoinRequest(Player sender, Community community, CommunityJoinRequestInfo info, boolean announce)
	{
		runAsync(() ->
		{
			_repo.removeJoinRequest(community.getId(), info.AccountId);
		});
		new CommunityCloseJoinRequest(community.getId(), sender.getName(), info.Name, info.UUID.toString(), info.AccountId, announce).publish();
	}
	
	public void handleJoin(Player sender, ICommunity community, boolean fromInvite)
	{
		final int accountId = getClientManager().getAccountId(sender);
		final String playerName = getClientManager().Get(sender).getName();
		runAsync(() ->
		{
			_repo.addToCommunity(accountId, community.getId());
			if (fromInvite)
			{
				_repo.deleteInviteToCommunity(community.getId(), playerName);
			}
			updateBrowserStatus(community);
			_repo.handlePlayerJoin(_loadedCommunities, Collections.singletonList(community.getId()), accountId);
			_browserCommunities.remove(community.getId());
		});
		new CommunityUpdateMembership(community.getId(), sender.getName(), sender.getName(), sender.getUniqueId().toString(), accountId, false, false).publish();
		if (fromInvite)
		{
			new CommunityUnInvite(community.getId(), sender.getName(), sender.getName(), sender.getUniqueId().toString(), false).publish();
		}
	}
	
	public void handleKick(Player sender, Community community, CommunityMemberInfo info)
	{
		runAsync(() ->
		{
			_repo.removeFromCommunity(info.AccountId, community.getId());
		});
		new CommunityUpdateMembership(community.getId(), sender.getName(), info.Name, info.UUID.toString(), info.AccountId, true, false).publish();
	}
	
	public void handleLeave(Player sender, Community community, CommunityMemberInfo info)
	{
		runAsync(() ->
		{
			_repo.removeFromCommunity(info.AccountId, community.getId());
			updateBrowserStatus(community);
		});
		new CommunityUpdateMembership(community.getId(), sender.getName(), info.Name, info.UUID.toString(), info.AccountId, false, true).publish();
	}
	
	public void handleSettingUpdate(Player sender, Community community, CommunitySetting setting, String newValue)
	{
		runAsync(() ->
		{
			_repo.updateCommunitySetting(setting, community.getId(), newValue);
			if (setting == CommunitySetting.PRIVACY)
			{
				updateBrowserStatus(community);
			}
		});
		new CommunityUpdateSetting(community.getId(), sender.getName(), setting.toString(), newValue).publish();
	}
	
	public void handleNameUpdate(Player sender, Community community, String newName)
	{
		runAsync(() ->
		{
			_repo.updateCommunityName(community.getId(), newName);
		});
		new CommunityUpdateName(community.getId(), sender.getName(), newName).publish();
	}
	
	public void handleRoleUpdate(Player sender, Community community, CommunityMemberInfo info, CommunityRole role)
	{
		runAsync(() ->
		{
			_repo.updateCommunityRole(info.AccountId, community.getId(), role);
		});
		new CommunityUpdateMemberRole(community.getId(), sender.getName(), info.UUID.toString(), role.toString()).publish();
	}
	
	public void handleCreate(Player sender, String senderName, int accountId, String name)
	{
		if (_creating.contains(sender.getUniqueId()))
		{
			UtilPlayer.message(sender, F.main(getName(), "You are already creating a Community!"));
			return;
		}
		_creating.add(sender.getUniqueId());
		runAsync(() ->
		{
			_repo.createCommunity(name, accountId, id ->
			{
				if (id == -1)
				{
					UtilPlayer.message(sender, F.main(getName(), "Failed to create community " + F.elem(name)));
					runSync(() -> _creating.remove(sender.getUniqueId()));
				}
				else
				{
					if (ownsCommunity(sender.getUniqueId()))
					{
						UtilPlayer.message(sender, F.main(getName(), "You already own a community!"));
						_repo.deleteCommunity(id);
						runSync(() -> _creating.remove(sender.getUniqueId()));
						return;
					}
					new CommunityCreate(sender.getUniqueId().toString(), senderName, accountId, id, name).publish();
					runSync(() -> _creating.remove(sender.getUniqueId()));
				}
			});
		});
	}
	
	public void handleDisband(Player sender, Community community)
	{
		runAsync(() ->
		{
			_repo.deleteCommunity(community.getId());
		});
		new CommunityDisband(sender.getName(), community.getId()).publish();
	}
	
	public void handleToggleReadingChat(Player sender, Community community)
	{
		final int accountId = getClientManager().getAccountId(sender);
		final boolean reading = !community.getMembers().get(sender.getUniqueId()).ReadingChat;
		
		runAsync(() ->
		{
			_repo.setReadingChat(accountId, community.getId(), reading);
		});
		new CommunityUpdateMemberChatReading(community.getId(), sender.getUniqueId().toString(), reading).publish();
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT cm.communityId, cm.communityRole, c.region FROM communityMembers AS cm INNER JOIN communities AS c ON c.id=cm.communityId WHERE accountId=" + accountId + ";";
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		List<Integer> load = new ArrayList<>();
		CommunityMemberData data = new CommunityMemberData();
		while (resultSet.next())
		{
			Integer communityId = resultSet.getInt("communityId");
			CommunityRole role = CommunityRole.parseRole(resultSet.getString("communityRole"));
			String region = resultSet.getString("region");
			if (region.equalsIgnoreCase(_region.name()))
			{
				data.joinCommunity(communityId, role);
				if (getLoadedCommunity(communityId) == null)
				{
					load.add(communityId);
				}
			}
		}
		Set(uuid, data);

		if (!load.isEmpty())
		{
			_browserCommunities.keySet().removeAll(load);

			runAsync(() ->
			{
				_repo.handlePlayerJoin(_loadedCommunities, load, accountId);
				System.out.println("Loaded communities: " + load + "; Total: " + _loadedCommunities.size());
			});
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onChat(FormatPlayerChatEvent event)
	{
		if (event.getChatChannel() != ChatChannel.COMMUNITY)
		{
			return;
		}

		event.setCancelled(true);

		Player sender = event.getPlayer();

		CommunityMemberData memberData = Get(sender);

		// If the player does not have a specified community to chat to...
		if (memberData.getCommunityChattingTo() == -1)
		{
			// And they are only in a single community...
			if (memberData.getTotalCommunities() == 1)
			{
				// Set that as the one they are chatting to.
				memberData.setCommunityChattingTo(memberData.getCommunities().get(0));
			}
			else
			{
				int savedChattingTo = getCustomDataManager().getData(sender, COMMUNITY_CHAT_KEY);

				if (savedChattingTo != -1)
				{
					memberData.setCommunityChattingTo(savedChattingTo);
				}
				else
				{
					UtilPlayer.message(sender, F.main(getName(), "You are not chatting to a specific community! Use " + F.elem("/com chat <community>") + " to select a community to chat to."));
					return;
				}
			}
		}

		Community target = _loadedCommunities.get(memberData.getCommunityChattingTo());

		if (target == null || !target.getMembers().containsKey(event.getPlayer().getUniqueId()))
		{
			UtilPlayer.message(sender, F.main(getName(), "You are not in that community! Use " + F.elem("/com chat <community>") + " to select a new community to chat to!"));
		}
		else
		{
			if (Recharge.Instance.use(sender, "Community Chat to " + target.getId(), target.getChatDelay(), false, false))
			{
				new CommunityChat(sender.getName(), target.getId(), _chat.filterMessage(sender, true, event.getMessage())).publish();
			}
			else
			{
				UtilPlayer.message(sender, F.main(getName(), "You cannot chat to " + F.name(target.getName()) + " that quickly!"));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		// Load their communities if it hasn't already been done
		Player player = event.getPlayer();
		CommunityMemberData data = Get(player);

		if (data.Invites.size() > 0 && getPrefManager().get(event.getPlayer()).isActive(Preference.COMMUNITY_INVITES))
		{
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have been invited to join " + F.elem(data.Invites.size()) + " communities!"));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		// Remove their communities from memory if they're the last player
		List<Integer> unloaded = new ArrayList<>();
		Player player = event.getPlayer();
		List<Community> communities = Get(player).getCommunities();
		for (Community community : communities)
		{
			if (community.getId().equals(_mcsCommunity)
			    || community.getMembers().keySet().stream().anyMatch(uuid -> !player.getUniqueId().equals(uuid) && Bukkit.getPlayer(uuid) != null))
			{
				continue;
			}

			// If it's a browser community, keep some of the data
			if (community.isBrowserEligible())
			{
				_browserCommunities.put(community.getId(), community.toBrowser());
			}

			// Unload this community from memory
			_dirty.remove(community);
			_loadedCommunities.remove(community.getId());
			unloaded.add(community.getId());
		}

		if (!unloaded.isEmpty())
		{
			System.out.println("Unloaded communities: " + unloaded + "; Total: " + _loadedCommunities.size());
		}
	}

	@Override
	protected CommunityMemberData addPlayer(UUID uuid)
	{
		return new CommunityMemberData();
	}	
}