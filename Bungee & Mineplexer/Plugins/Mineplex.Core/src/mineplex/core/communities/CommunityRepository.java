package mineplex.core.communities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.Callback;
import mineplex.core.communities.data.BrowserCommunity;
import mineplex.core.communities.data.Community;
import mineplex.core.communities.data.CommunityJoinRequestInfo;
import mineplex.core.communities.data.CommunityMemberInfo;
import mineplex.core.communities.data.CommunityRole;
import mineplex.core.communities.data.CommunitySetting;
import mineplex.core.communities.data.ICommunity;
import mineplex.core.game.GameDisplay;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;
import mineplex.serverdata.redis.RedisDataRepository;

public class CommunityRepository extends RepositoryBase
{
	private static final String GET_COMMUNITIES_BY_ID = "SELECT * FROM communities";
	private static final String GET_COMMUNITY_MEMBERS = "SELECT cm.communityId, cm.accountId, cm.communityRole, ac.name, ac.uuid, ac.lastLogin, cm.readingChat FROM communityMembers cm INNER JOIN accounts ac ON ac.id=cm.accountId";
	private static final String GET_COMMUNITY_SIZE = "SELECT communityId FROM communityMembers";
	private static final String GET_COMMUNITY_JOIN_REQUESTS = "SELECT cjr.communityId, cjr.accountId, ac.name, ac.uuid FROM communityJoinRequests cjr INNER JOIN accounts ac ON ac.id=cjr.accountId WHERE cjr.accountId=?;";
	private static final String GET_COMMUNITY_SETTINGS = "SELECT communityId, settingId, settingValue FROM communitySettings";
	private static final String GET_PUBLIC_COMMUNITIES = "SELECT communityId FROM communitySettings WHERE settingId=8 AND settingValue='true';";

	private static final String REMOVE_FROM_COMMUNITY = "DELETE FROM communityMembers WHERE accountId=? AND communityId=?;";
	private static final String UPDATE_COMMUNITY_ROLE = "UPDATE communityMembers SET communityRole=? WHERE accountId=? AND communityId=?;";
	private static final String ADD_TO_COMMUNITY = "INSERT INTO communityMembers (accountId, communityId, communityRole, readingChat) VALUES (?, ?, ?, true);";
	private static final String UPDATE_COMMUNITY_SETTING = "INSERT INTO communitySettings (settingId, communityId, settingValue) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE settingValue=VALUES(settingValue);";
	private static final String UPDATE_COMMUNITY_NAME = "UPDATE communities SET name=? WHERE id=?;";
	private static final String INVITE_TO_COMMUNITY = "INSERT INTO communityInvites (accountId, communityId) SELECT a.id AS accountId, ? FROM accounts as a WHERE a.name = ? ORDER BY a.lastLogin DESC LIMIT 1 ON DUPLICATE KEY UPDATE communityInvites.id=communityInvites.id;";
	private static final String DELETE_INVITE_TO_COMMUNITY = "DELETE i FROM communityInvites AS i INNER JOIN accounts as a ON i.accountId = a.id WHERE a.name = ? AND i.communityId=?;";
	private static final String ADD_JOIN_REQUEST = "INSERT INTO communityJoinRequests (accountId, communityId) VALUES (?, ?);";
	private static final String REMOVE_JOIN_REQUEST = "DELETE FROM communityJoinRequests WHERE accountId=? AND communityId=?;";
	
	private static final String CREATE_COMMUNITY = "INSERT INTO communities (name, region) VALUES (?, ?);";
	
	private static final String SET_READING_CHAT_IN = "UPDATE communityMembers SET readingChat=? WHERE accountId=? AND communityId=?;";
	
	private DataRepository<PlayerStatus> _repo;
	private Region _region;
	
	public CommunityRepository(RedisDataRepository<PlayerStatus> statusRepo, Region region)
	{
		super(DBPool.getAccount());
		
		_repo = statusRepo;
		_region = region;
	}

	public void communityExists(String name, Consumer<Boolean> result)
	{
		try (Connection connection = getConnection())
		{
			executeQuery(connection,
					"SELECT name FROM communities WHERE name=?",
					resultSet -> result.accept(resultSet.next()),
					new ColumnVarChar("name", 15, name));
		} catch (Exception ex)
		{
			throw new RuntimeException("Failed to determine if community exists", ex);
		}
	}

	/**
	 * Loads all communities that are eligible to be shown in the browser.
	 * That is, they have 5 or more members and aren't private.
	 */
	public void loadBrowserIds(final Collection<Integer> store)
	{
		try (Connection connection = getConnection())
		{
			executeQuery(connection, GET_PUBLIC_COMMUNITIES, resultSet ->
			{
				while (resultSet.next())
				{
					int id = resultSet.getInt("communityId");
					store.add(id);
				}
			});
		} catch (SQLException ex)
		{
			System.err.println("Failed to load public community IDs");
			ex.printStackTrace();
		}
	}

	public void loadBrowserCommunities(final Map<Integer, BrowserCommunity> store, final List<Integer> load)
	{
		if (load.isEmpty())
		{
			return;
		}

		try (Connection connection = getConnection())
		{
			String inClause = getInClause(load);
			ColumnInt[] idColumns = genIdColumns("id", load);

			executeQuery(connection, GET_COMMUNITIES_BY_ID + inClause.replace("%col", "id"), resultSet ->
			{
				while (resultSet.next())
				{
					int id = resultSet.getInt("id");
					String cName = resultSet.getString("name");
					BrowserCommunity community = new BrowserCommunity(id, cName);

					store.put(id, community);
				}
			}, idColumns);

			idColumns = genIdColumns("communityId", load);
			executeQuery(GET_COMMUNITY_SIZE + inClause.replace("%col", "communityId"), resultSet ->
			{
				while (resultSet.next())
				{
					int communityId = resultSet.getInt("communityId");
					BrowserCommunity com = store.get(communityId);
					if (com != null)
					{
						com.addMember();
					}
				}
			}, idColumns);

			idColumns = genIdColumns("communityId", load);
			executeQuery(connection, GET_COMMUNITY_SETTINGS
			                         + inClause.replace("%col", "communityId").replace(";", "")
			                         + " AND (settingId=5 OR settingId=6 OR settingId=7);", settingSet ->
			{
				while (settingSet.next())
				{
					int communityId = settingSet.getInt("communityId");
					int settingId = settingSet.getInt("settingId");
					String value = settingSet.getString("settingValue");

					BrowserCommunity community = store.get(communityId);
					CommunitySetting setting = CommunitySetting.getSetting(settingId);
					if (setting == CommunitySetting.DESCRIPTION)
					{
						community.setDescription(value);
					} else if (setting == CommunitySetting.FAVORITE_GAME)
					{
						community.setFavoriteGame(GameDisplay.matchName(value));
					} else if (setting == CommunitySetting.PRIVACY)
					{
						community.setPrivacySetting(Community.PrivacySetting.parsePrivacy(value));
					}
				}
			}, idColumns);
		} catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Loads and stores a single community.
	 */
	public void loadCommunity(final Map<Integer, Community> store, final int id)
	{
		loadInternal(store, Collections.singletonList(id), -1);
	}

	/**
	 * Loads all of the provided communities and the player's join requests.
	 */
	public void handlePlayerJoin(final Map<Integer, Community> store, final List<Integer> load, final int accountId)
	{
		List<Community> communities = loadInternal(store, load, accountId);
		updateMembersAndJoinRequests(communities);
	}

	public void updateBrowserStatus(ICommunity community, boolean flag)
	{
		updateCommunitySetting(CommunitySetting.SHOW_IN_BROWSER, community.getId(), String.valueOf(flag));
	}

	private ColumnInt[] genIdColumns(String colName, List<Integer> nums)
	{
		return nums.stream().map(i -> new ColumnInt(colName, i)).toArray(ColumnInt[]::new);
	}

	private String getInClause(List<Integer> load)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(" WHERE %col IN (");

		for (int index = 0; index < load.size(); index++)
		{
			if (index != 0)
				builder.append(", ");
			builder.append("?");
		}

		builder.append(");");
		return builder.toString();
	}

	private List<Community> loadInternal(final Map<Integer, Community> store, final List<Integer> load, final int accountId)
	{
		List<Community> communities = new ArrayList<>();

		try (Connection connection = getConnection())
		{
			if (!load.isEmpty())
			{
				String inClause = getInClause(load);
				ColumnInt[] idColumns = genIdColumns("id", load);

				executeQuery(connection, GET_COMMUNITIES_BY_ID + inClause.replace("%col", "id"), resultSet ->
				{
					while (resultSet.next())
					{
						int id = resultSet.getInt("id");
						String cName = resultSet.getString("name");
						Community community = new Community(id, cName);
						communities.add(community);

						store.put(id, community);
					}
				}, idColumns);

				idColumns = genIdColumns("cm.communityId", load);
				executeQuery(connection, GET_COMMUNITY_MEMBERS + inClause.replace("%col", "cm.communityId"), memberSet ->
				{
					while (memberSet.next())
					{
						int communityId = memberSet.getInt("communityId");
						int accountId1 = memberSet.getInt("accountId");
						String name = memberSet.getString("name");
						UUID uuid = UUID.fromString(memberSet.getString("uuid"));
						CommunityRole role = CommunityRole.parseRole(memberSet.getString("communityRole"));
						long lastLogin = memberSet.getTimestamp("lastLogin").getTime();
						boolean readingChat = memberSet.getBoolean("readingChat");

						CommunityMemberInfo info = new CommunityMemberInfo(name, uuid, accountId1, role, lastLogin);
						info.ReadingChat = readingChat;

						Community community = store.get(communityId);
						if (community != null)
						{
							community.getMembers().put(info.UUID, info);
						}
					}
				}, idColumns);

				idColumns = genIdColumns("communityId", load);
				executeQuery(connection, GET_COMMUNITY_SETTINGS + inClause.replace("%col", "communityId"), settingSet ->
				{
					while (settingSet.next())
					{
						int communityId = settingSet.getInt("communityId");
						int settingId = settingSet.getInt("settingId");
						String value = settingSet.getString("settingValue");

						Community community = store.get(communityId);
						CommunitySetting setting = CommunitySetting.getSetting(settingId);
						if (community != null && setting != null)
						{
							setting.parseValueInto(value, community);
						}
					}
				}, idColumns);

				// Ensure the browser flag is set
				for (Community com : communities)
				{
					if (!com.isBrowserFlagSet())
					{
						updateBrowserStatus(com, com.isBrowserEligible());
						com.setBrowserFlag();
					}
				}
			}

			if (accountId != -1)
			{
				executeQuery(connection, GET_COMMUNITY_JOIN_REQUESTS, requestSet ->
				{
					while (requestSet.next())
					{
						int communityId = requestSet.getInt("communityId");
						UUID uuid = UUID.fromString(requestSet.getString("uuid"));
						String name = requestSet.getString("name");

						Community community = store.get(communityId);
						if (community != null)
						{
							community.getJoinRequests().put(uuid, new CommunityJoinRequestInfo(name, uuid, accountId));
						}
					}
				}, new ColumnInt("cjr.accountId", accountId));
			}
		} catch (SQLException ex)
		{
			System.err.println("Encountered an SQL exception loading communities " + load);
			ex.printStackTrace();
		}

		return communities;
	}

	public void updateMembersAndJoinRequests(List<Community> communities)
	{
		if (communities.isEmpty())
		{
			return;
		}

		TimingManager.start("members + join requests for " + communities.size() + " communities");
		Map<String, PlayerStatus> statuses = _repo.getElementsMap(
				Stream.concat(
						communities.stream().flatMap(community -> community.getMembers().keySet().stream()),
						communities.stream().flatMap(community -> community.getJoinRequests().keySet().stream())
				)
				.distinct()
				.map(UUID::toString)
				.collect(Collectors.toList())
		);

		for (Community c : communities)
		{
			// Update member player status
			for (Map.Entry<UUID,CommunityMemberInfo> entry : c.getMembers().entrySet())
			{
				CommunityMemberInfo info = entry.getValue();
				PlayerStatus status = statuses.get(entry.getKey().toString());

				boolean online = false;
				String server = "";
				if (status != null)
				{
					online = true;
					server = status.getServer();
					info.update(System.currentTimeMillis(), online, server);
					if (!info.Name.equals(status.getName()))
					{
						info.updateName(status.getName());
					}
				}
				else
				{
					if (info.isOnline())
					{
						info.setOffline();
					}
				}
			}

			// Update join request names
			for (Map.Entry<UUID,CommunityJoinRequestInfo> entry : c.getJoinRequests().entrySet())
			{

				CommunityJoinRequestInfo info = entry.getValue();
				PlayerStatus status = statuses.get(entry.getKey().toString());

				if (status != null)
				{
					if (!info.Name.equals(status.getName()))
					{
						info.update(status.getName());
					}
				}
			}
		}
		TimingManager.stop("members + join requests for " + communities.size() + " communities");
	}

	public void removeFromCommunity(int accountId, int communityId)
	{
		executeUpdate(REMOVE_FROM_COMMUNITY, new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId));
	}
	
	public void updateCommunityRole(int accountId, int communityId, CommunityRole role)
	{
		executeUpdate(UPDATE_COMMUNITY_ROLE, new ColumnVarChar("communityRole", 20, role.toString()), new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId));
	}
	
	public void addToCommunity(int accountId, int communityId)
	{
		executeUpdate(ADD_TO_COMMUNITY, new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId), new ColumnVarChar("communityRole", 20, CommunityRole.MEMBER.toString()));
	}
	
	public void updateCommunitySetting(CommunitySetting setting, int communityId, String value)
	{
		executeUpdate(UPDATE_COMMUNITY_SETTING, new ColumnInt("settingId", setting.getId()), new ColumnInt("communityId", communityId), new ColumnVarChar("settingValue", 100, value));
	}
	
	public void updateCommunityName(int communityId, String name)
	{
		executeUpdate(UPDATE_COMMUNITY_NAME, new ColumnVarChar("name", 15, name), new ColumnInt("id", communityId));
	}
	
	public boolean inviteToCommunity(int communityId, String name)
	{
		return executeUpdate(INVITE_TO_COMMUNITY, new ColumnInt("communityId", communityId), new ColumnVarChar("name", 32, name)) > 0;
	}
	
	public boolean deleteInviteToCommunity(int communityId, String name)
	{
		return executeUpdate(DELETE_INVITE_TO_COMMUNITY, new ColumnVarChar("name", 32, name), new ColumnInt("communityId", communityId)) > 0;
	}
	
	public void addJoinRequest(int communityId, int accountId)
	{
		executeUpdate(ADD_JOIN_REQUEST, new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId));
	}
	
	public void removeJoinRequest(int communityId, int accountId)
	{
		executeUpdate(REMOVE_JOIN_REQUEST, new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId));
	}
	
	public void createCommunity(String name, int leaderAccount, Callback<Integer> idCallback)
	{
		try (Connection connection = getConnection())
		{
			executeInsert(connection, CREATE_COMMUNITY, resultSet ->
			{
				if (resultSet.next())
				{
					int id = resultSet.getInt(1);
					executeUpdate(connection, ADD_TO_COMMUNITY, null, new ColumnInt("accountId", leaderAccount), new ColumnInt("communityId", id), new ColumnVarChar("communityRole", 20, CommunityRole.LEADER.toString()));
					idCallback.run(id);
				}
				else
				{
					idCallback.run(-1);
				}
			}, () -> idCallback.run(-1), new ColumnVarChar("name", 15, name), new ColumnVarChar("region", 5, _region.name()));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			idCallback.run(-1);
		}
	}
	
	public void deleteCommunity(int communityId)
	{
		try (Connection connection = getConnection())
		{
			executeUpdate(connection, "DELETE FROM communities WHERE id=?;", null, new ColumnInt("id", communityId));
			executeUpdate(connection, "DELETE FROM communitySettings WHERE communityId=?;", null, new ColumnInt("communityId", communityId));
			executeUpdate(connection, "DELETE FROM communityMembers WHERE communityId=?;", null, new ColumnInt("communityId", communityId));
			executeUpdate(connection, "DELETE FROM communityInvites WHERE communityId=?;", null, new ColumnInt("communityId", communityId));
			executeUpdate(connection, "DELETE FROM communityJoinRequests WHERE communityId=?", null, new ColumnInt("communityId", communityId));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setReadingChat(int accountId, int communityId, boolean reading)
	{
		executeUpdate(SET_READING_CHAT_IN, new ColumnBoolean("readingChat", reading), new ColumnInt("accountId", accountId), new ColumnInt("communityId", communityId));
	}

	protected void initialize() {}
	protected void update() {}
}