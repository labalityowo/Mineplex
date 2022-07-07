package mineplex.game.clans.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.NautHashMap;
import mineplex.database.tables.records.ClansRecord;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.tokens.ClanAllianceToken;
import mineplex.game.clans.core.repository.tokens.ClanMemberToken;
import mineplex.game.clans.core.repository.tokens.ClanTerritoryToken;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.core.repository.tokens.ClanWarToken;
import mineplex.game.clans.core.repository.tokens.SimpleClanToken;
import mineplex.game.clans.core.war.ClanWarData;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.Column;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

import static mineplex.database.Tables.accountClan;
import static mineplex.database.Tables.accounts;
import static mineplex.database.Tables.clans;
import static org.jooq.impl.DSL.select;

public class ClanRepository extends RepositoryBase
{
	private static String CREATE_CLAN_TABLE = "CREATE TABLE IF NOT EXISTS clans (id INT NOT NULL AUTO_INCREMENT, serverId INT NOT NULL, name VARCHAR(100), description VARCHAR(140), home VARCHAR(140), admin BIT(1), dateCreated DATETIME, lastOnline DATETIME, energy INT, PRIMARY KEY (id), INDEX clanName (name));";
	private static String CREATE_ACCOUNT_CLAN_TABLE = "CREATE TABLE IF NOT EXISTS accountClan (id INT NOT NULL AUTO_INCREMENT, accountId INT, clanId INT, clanRole VARCHAR(140), PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id), FOREIGN KEY (clanId) REFERENCES clans(id), INDEX clanIdIndex (clanId));";
	private static String CREATE_CLAN_TERRITORY_TABLE = "CREATE TABLE IF NOT EXISTS clanTerritory (id INT NOT NULL AUTO_INCREMENT, clanId INT, chunk VARCHAR(100), safe BIT(1), PRIMARY KEY (id), FOREIGN KEY (clanId) REFERENCES clans(id));";
	private static String CREATE_CLAN_ALLIANCE_TABLE = "CREATE TABLE IF NOT EXISTS clanAlliances (id INT NOT NULL AUTO_INCREMENT, clanId INT, otherClanId INT, trusted BIT(1), PRIMARY KEY (id), FOREIGN KEY (otherClanId) REFERENCES clans(id), FOREIGN KEY (clanId) REFERENCES clans(id), INDEX clanIdIndex (clanId));";

	private static String RETRIEVE_CLAN_INFO = "SELECT c.id, c.name, c.description, c.home, c.admin, c.energy, c.kills, c.murder, c.deaths, c.warWins, c.warLosses, c.generator, c.generatorStock, c.dateCreated, c.lastOnline, c.eloRating FROM clans AS c WHERE lower(c.name) = ?;";
	private static String RETRIEVE_PLAYER_CLAN_INFO = "SELECT clans.name, accountClan.clanRole, clanServer.serverName, clans.id FROM accountClan INNER JOIN clans ON clans.id = accountClan.clanId INNER JOIN clanServer ON clans.serverId = clanServer.id WHERE accountClan.accountId = ?;";
	private static String RETRIEVE_START_CLAN_INFO = "SELECT c.id, c.name, c.description, c.home, c.admin, c.energy, c.kills, c.murder, c.deaths, c.warWins, c.warLosses, c.generator, c.generatorStock, c.dateCreated, c.lastOnline, c.eloRating, ct.chunk, ct.safe FROM clans AS c LEFT JOIN clanTerritory AS ct ON ct.clanId = c.id WHERE c.serverId = ? OR c.admin = 1;";
	private static String RETRIEVE_CLAN_MEMBER_INFO = "SELECT c.name, a.name, a.uuid, clanRole FROM accountClan AS ac INNER JOIN accounts AS a ON a.id = ac.accountId INNER JOIN clans AS c on c.id = ac.clanId WHERE c.serverId = ?;";
	private static String RETRIEVE_CLAN_MEMBERS = "SELECT c.name, a.name, a.uuid, clanRole FROM accountClan AS ac INNER JOIN accounts AS a ON a.id = ac.accountId INNER JOIN clans AS c on c.id = ac.clanId WHERE lower(c.name) = ?;";
	private static String RETRIEVE_CLAN_ALLIANCE_INFO = "SELECT c.name, cOther.name, ca.trusted FROM clanAlliances AS ca INNER JOIN clans AS c ON c.id = ca.clanId INNER JOIN clans as cOther ON cOther.id = ca.otherClanId WHERE c.serverId = ?;";
//	private static String RETRIEVE_CLAN_ENEMY_INFO = "SELECT c.name, cOther.name, clanScore, otherClanScore, clanKills, otherClanKills, timeFormed FROM clanEnemies AS ce INNER JOIN clans AS c ON c.id = ce.clanId INNER JOIN clans as cOther ON cOther.id = ce.otherClanId WHERE c.serverId = ?;";
	private static String RETRIEVE_CLAN_WAR_INFO = "SELECT c.name, cOther.name, cw.score, cw.created, cw.ended, cw.lastUpdated FROM clanWar AS cw INNER JOIN clans AS c on c.id = cw.initiatorId INNER JOIN clans as cOther ON cOther.id = cw.clanId WHERE c.serverId = ? AND completed = FALSE";
	private static String RETRIEVE_CLAN_WAR_FROM_CLAN_IDS = "SELECT c.name, cOther.name, cw.score, cw.created, cw.ended, cw.lastUpdated FROM clanWar AS cw INNER JOIN clans AS c on c.id = cw.initiatorId INNER JOIN clans as cOther ON cOther.id = cw.clanId WHERE cw.initiatorId = ? AND cw.clanId = ? AND completed = FALSE";

	private static String DELETE_CLAN_MEMBER = "DELETE aC FROM accountClan AS aC INNER JOIN accounts ON accounts.id = aC.accountId WHERE aC.clanId = ? AND accounts.name = ?;";
	private static String DELETE_CLAN_MEMBERS = "DELETE FROM accountClan WHERE clanId = ?;";	
	private static String DELETE_CLAN_TERRITORY = "DELETE FROM clanTerritory WHERE clanId = ? AND chunk = ?;";
	private static String DELETE_CLAN_ALL_TERRITORY = "DELETE FROM clanTerritory WHERE clanId = ?;"; //
	private static String DELETE_CLAN_TERRITORIES = "DELETE FROM clanTerritory WHERE clanId = ?;"; //
	private static String DELETE_CLAN_ALLIANCE = "DELETE FROM clanAlliances WHERE clanId = ? AND otherClanId = ?;";
	private static String DELETE_CLAN_ALLIANCES = "DELETE FROM clanAlliances WHERE clanId = ? OR otherClanId = ?;";
//	private static String DELETE_CLAN_ENEMIES = "DELETE FROM clanEnemies WHERE clanId = ? OR otherClanId = ?;";
	private static String DELETE_CLAN = "DELETE FROM clans WHERE id = ?;";
	private static String DELETE_ALL_WAR = "DELETE FROM clanWar WHERE initiatorId = ? OR clanId = ?";
	
	private static String ADD_CLAN = "INSERT INTO clans (serverId, name, description, home, admin, dateCreated, energy, lastOnline) VALUES (?, ?, ?, ?, ?, now(), ?, now());";
	private static String ADD_CLAN_MEMBER = "INSERT INTO accountClan (accountId, clanId, clanRole) SELECT accounts.id, ?, ? FROM accounts WHERE accounts.name = ?;";
	private static String ADD_CLAN_ALLIANCE = "INSERT INTO clanAlliances (clanId, otherClanId, trusted) VALUES (?, ?, ?);";
	private static String ADD_CLAN_WAR = "INSERT INTO clanWar (initiatorId, clanId, score, created, lastUpdated) VALUES (?, ?, ?, ?, ?)";
	private static String ADD_CLAN_TERRITORY = "INSERT INTO clanTerritory (clanId, chunk, safe) VALUES (?, ?, ?);";
	
	//Not Sure if UPDATE_CLAN should set eloRating, but I would think it would need to
	private static String UPDATE_CLAN = "UPDATE clans SET name = ?, description = ?, home = ?, admin = ?, energy = ?, kills = ?, murder = ?, deaths = ?, warWins = ?, warLosses = ?, lastOnline = ? WHERE id = ?;";
	private static String UPDATE_CLAN_MEMBER = "UPDATE accountClan AS AC INNER JOIN accounts ON accounts.id = AC.accountId SET AC.clanRole = ? WHERE AC.clanId = ? AND accounts.name = ?;";
	private static String UPDATE_CLAN_ALLIANCE = "UPDATE clanAlliances SET trusted = ? WHERE clanId = ? AND otherClanId = ?;";
	private static String UPDATE_CLAN_TERRITORY = "UPDATE clanTerritory SET safe = ? WHERE chunk = ?;"; //
	private static String UPDATE_CLAN_WAR = "UPDATE clanWar SET score = ?, lastUpdated = ? WHERE initiatorId = ? AND clanId = ?";
	private static String UPDATE_CLAN_SERVER_ID = "UPDATE clans SET serverId = ?, home = '', generator = '' WHERE id = ?;";
	private static String UPDATE_CLAN_GENERATOR = "UPDATE clans SET generator = ?, generatorStock = ? WHERE id = ?;";

	private static String GET_CLAN_SERVER = "SELECT id FROM clanServer WHERE clanServer.serverName = ?"; 
	private static String ADD_CLAN_SERVER = "INSERT INTO clanServer (serverName) VALUES (?);";
	
	private String _serverName;
	private int _serverId;
	
	public ClanRepository(JavaPlugin plugin, String serverName, boolean isClansServer)
	{
		super(DBPool.getAccount());
		
		_serverName = serverName;
		_serverId = -1; 

		if (isClansServer)
		{
			loadServerId();
		}
	}
	
	public ClanRepository(JavaPlugin plugin, String serverName)
	{
		this(plugin, serverName, false);
	}

	private void loadServerId()
	{
		ResultSetCallable callable = new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					_serverId = resultSet.getInt(1);
				}
			}
		};

		executeQuery(GET_CLAN_SERVER, callable, new ColumnVarChar("serverName", 100, _serverName));

		if (_serverId == -1)
		{
			// Need to insert server into database
			executeInsert(ADD_CLAN_SERVER, callable, new ColumnVarChar("serverName", 100, _serverName));

			if (_serverId == -1)
			{
				System.out.println("Error loading serverId from database. Shutting down server!");
				Bukkit.shutdown();
				return;
			}
		}

		System.out.println("Loaded Server ID: " + _serverId);
	}

	@Override
	protected void initialize()
	{
		executeUpdate(CREATE_CLAN_TABLE);
		executeUpdate(CREATE_ACCOUNT_CLAN_TABLE);
		executeUpdate(CREATE_CLAN_TERRITORY_TABLE);
		executeUpdate(CREATE_CLAN_ALLIANCE_TABLE);
	}
	
	/**
	 * Updates a clan's home server while removing all 
	 * alliances, enemies, teritory claims and homes set on 
	 * originating server.
	 * @param clanId - the id of the clan to move
	 */
	public void moveClanServer(final int clanId, String serverName, final Callback<Boolean> callback)
	{
		executeQuery(GET_CLAN_SERVER, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				boolean success = resultSet.next();
				
				if (success)
				{
					int serverId = resultSet.getInt(1);
					ColumnInt clanIdCol = new ColumnInt("clanId", clanId);
					ColumnInt serverIdCol = new ColumnInt("serverId", serverId);
					
					executeUpdate(DELETE_CLAN_ALLIANCES, clanIdCol, clanIdCol);
					deleteAllWar(clanId);
					executeUpdate(DELETE_CLAN_TERRITORIES, clanIdCol);
					executeUpdate(UPDATE_CLAN_SERVER_ID, serverIdCol, clanIdCol);
				}
				
				callback.run(success); 
			}
		}, new ColumnVarChar("serverName", 100, serverName));
		
	}
	
	public ClanToken retrieveClan(String clanName)
	{
		final ClanToken clan = new ClanToken();
		
		executeQuery(RETRIEVE_CLAN_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				if (resultSet.next())
				{
					clan.Id = resultSet.getInt(1);
					clan.Name = resultSet.getString(2);
					clan.Description = resultSet.getString(3);
					clan.Home = resultSet.getString(4);
					clan.Admin = resultSet.getBoolean(5);
					clan.Energy = resultSet.getInt(6);
					clan.Kills = resultSet.getInt(7);
					clan.Murder = resultSet.getInt(8);
					clan.Deaths = resultSet.getInt(9);
					clan.WarWins = resultSet.getInt(10);
					clan.WarLosses = resultSet.getInt(11);
					clan.GeneratorBuyer = resultSet.getString(12);
					clan.GeneratorStock = resultSet.getInt(13);
					clan.DateCreated = resultSet.getTimestamp(14);
					clan.LastOnline = resultSet.getTimestamp(15);
					
					clan.EloRating = resultSet.getInt(16);
				}
			}
		}, new ColumnVarChar("name", 100, clanName.toLowerCase()));
		
		executeQuery(RETRIEVE_CLAN_MEMBERS, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					String clanName = resultSet.getString(1);
					
					ClanMemberToken memberToken = new ClanMemberToken();
					memberToken.Name = resultSet.getString(2);
					memberToken.PlayerUUID = UUID.fromString(resultSet.getString(3));
					memberToken.ClanRole = resultSet.getString(4);
					
					clan.Members.add(memberToken);
				}
			}
			
		}, new ColumnVarChar("name", 100, clanName.toLowerCase()));
		
		return clan;
	}
	
	public void clanExists(String clanName, final Callback<Boolean> callback)
	{
		executeQuery(RETRIEVE_CLAN_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				boolean clanExists = resultSet.next();
				callback.run(clanExists); 
			}
		}, new ColumnVarChar("name", 100, clanName.toLowerCase()));
	}

	public void retrievePlayersClan(int accountId, final Callback<SimpleClanToken> callback)
	{
		executeQuery(RETRIEVE_PLAYER_CLAN_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				SimpleClanToken clanToken = null;
				if (resultSet.next())
				{
					String clanName = resultSet.getString(1);
					String clanRole = resultSet.getString(2);
					String homeServer = resultSet.getString(3);
					int clanId = resultSet.getInt(4);
					clanToken = new SimpleClanToken(clanName, clanRole, homeServer, clanId);
				}
				
				callback.run(clanToken);
			}
		}, new ColumnInt("accountId", accountId));
	}
		
	public Collection<ClanToken> retrieveClans()
	{
		System.out.println("Beginning to load clans from database...");
		final NautHashMap<String, ClanToken> clans = new NautHashMap<String, ClanToken>();
		
		executeQuery(RETRIEVE_START_CLAN_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{ 
					ClanToken token = new ClanToken();
					token.Id = resultSet.getInt(1);
					token.Name = resultSet.getString(2);
					token.Description = resultSet.getString(3);
					token.Home = resultSet.getString(4);
					token.Admin = resultSet.getBoolean(5);
					token.Energy = resultSet.getInt(6);
					token.Kills = resultSet.getInt(7);
					token.Murder = resultSet.getInt(8);
					token.Deaths = resultSet.getInt(9);
					token.WarWins = resultSet.getInt(10);
					token.WarLosses = resultSet.getInt(11);
					
					token.GeneratorBuyer = resultSet.getString(12);
					token.GeneratorStock = resultSet.getInt(13);
					token.DateCreated = resultSet.getTimestamp(14);
					token.LastOnline = resultSet.getTimestamp(15);
					
					token.EloRating = resultSet.getInt(16);
					
					ClanTerritoryToken territoryToken = new ClanTerritoryToken();
					territoryToken.ClanName = token.Name;
					territoryToken.Chunk = resultSet.getString(17);
					territoryToken.Safe = resultSet.getBoolean(18);
					
					if (!clans.containsKey(token.Name))
					{
						clans.put(token.Name, token);
					}
					
					if (territoryToken.Chunk != null)
						clans.get(token.Name).Territories.add(territoryToken);
				}
			}
			
		}, new ColumnInt("serverId", _serverId));
		
		System.out.println("1");
		
		executeQuery(RETRIEVE_CLAN_MEMBER_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					String clanName = resultSet.getString(1);
					
					if (clans.containsKey(clanName))
					{
						ClanMemberToken memberToken = new ClanMemberToken();
						memberToken.Name = resultSet.getString(2);
						memberToken.PlayerUUID = UUID.fromString(resultSet.getString(3));
						memberToken.ClanRole = resultSet.getString(4);
						
						clans.get(clanName).Members.add(memberToken);
					}
				}
			}
			
		}, new ColumnInt("serverId", _serverId));

		System.out.println("2");
		
		executeQuery(RETRIEVE_CLAN_ALLIANCE_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					String clanName = resultSet.getString(1);

					if (clans.containsKey(clanName))
					{
						ClanAllianceToken allianceToken = new ClanAllianceToken();
						allianceToken.ClanName = resultSet.getString(2);
						allianceToken.Trusted = resultSet.getBoolean(3);

						clans.get(clanName).Alliances.add(allianceToken);
					}
				}
			}

		}, new ColumnInt("serverId", _serverId));

		System.out.println("3");

		executeQuery(RETRIEVE_CLAN_WAR_INFO, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					ClanWarToken warToken = new ClanWarToken();
					String clanA = resultSet.getString(1);
					String clanB = resultSet.getString(2);
					int score = resultSet.getInt(3);
					Timestamp created = resultSet.getTimestamp(4);
//					warToken.Ended = resultSet.getTimestamp(5);
					Timestamp updated = resultSet.getTimestamp(6);
					ClanWarData warData = new ClanWarData(clanA, clanB, score, created, updated, 0);
					warToken.WarData = warData;

					if (clans.containsKey(warToken.WarData.getClanA()) && clans.containsKey(warToken.WarData.getClanB()))
					{
						System.out.println("Loaded War Token: " + warToken.WarData.getClanA());
						clans.get(warToken.WarData.getClanA()).WarsOut.add(warToken);
						clans.get(warToken.WarData.getClanB()).WarsIn.add(warToken);
					}
				}
			}
		}, new ColumnInt("serverId", _serverId));

		System.out.println("Finished loading clans from database...");
		return clans.values();
	}

	public boolean deleteClan(int clanId)
	{
		return jooq().delete(clans).where(clans.id.equal(clanId)).execute() == 1;
	}

	public int addClan(ClanToken token)
	{
		ClansRecord record = jooq().newRecord(clans);
		record.setServerId(_serverId);
		record.setName(token.Name);
		record.setDescription(token.Description);
		record.setHome(token.Home);
		record.setAdmin(token.Admin);
		record.setEnergy(token.Energy);

		if (record.store() == 1)
		{
			return record.getId();
		}
		else
		{
			return -1;
		}
	}

	@Deprecated
	public boolean addMember(int clanId, String playerName, String role)
	{
		DSLContext create = jooq();
		int out = create.insertInto(accountClan)
				.set(accountClan.accountId, select(accounts.id).from(accounts).where(accounts.name.eq(playerName)))
				.set(accountClan.clanId, clanId)
				.set(accountClan.clanRole, role).execute();

		return out == 1;
	}

	public boolean addMember(int clanId, UUID playerUUID, String role)
	{
		int out = jooq().insertInto(accountClan)
				.set(accountClan.accountId, select(accounts.id).from(accounts).where(accounts.uuid.eq(playerUUID.toString())))
				.set(accountClan.clanId, clanId)
				.set(accountClan.clanRole, role).execute();

		return out == 1;
	}

	public boolean removeMember(int clanId, String playerName)
	{
		return executeUpdate(DELETE_CLAN_MEMBER, new ColumnInt("clanid", clanId), new ColumnVarChar("name", 100, playerName)) == 1;
	}

	public void updateMember(int clanId, String playerName, String role)
	{
		executeUpdate(UPDATE_CLAN_MEMBER, new ColumnVarChar("clanRole", 100, role) , new ColumnInt("clanid", clanId), new ColumnVarChar("name", 100, playerName));
	}

	public void addClanRelationship(int clanId, int otherClanId, boolean trusted)
	{
		executeUpdate(ADD_CLAN_ALLIANCE, new ColumnInt("clanid", clanId), new ColumnInt("otherClanId", otherClanId), new ColumnBoolean("trusted", trusted));
	}

	public void updateClanRelationship(int clanId, int otherClanId, boolean trusted)
	{
		executeUpdate(UPDATE_CLAN_ALLIANCE, new ColumnBoolean("trusted", trusted), new ColumnInt("clanid", clanId), new ColumnInt("otherClanId", otherClanId));
	}

	public void removeClanRelationship(int clanId, int otherClanId)
	{
		executeUpdate(DELETE_CLAN_ALLIANCE, new ColumnInt("clanid", clanId), new ColumnInt("otherClanId", otherClanId));
	}

	public boolean addTerritoryClaim(int clanId, ClaimLocation chunk, boolean safe)
	{
		return executeUpdate(ADD_CLAN_TERRITORY, new ColumnInt("clanId", clanId), new ColumnVarChar("chunk", 100, chunk.toStoredString()), new ColumnBoolean("safe", safe)) == 1;
	}
	
	public boolean addTerritoryClaims(int clanId, boolean safe, ClaimLocation... chunks)
	{
		int affectedRows = 0;
		int size = chunks.length;
		String query = "INSERT INTO clanTerritory (clanId, chunk, safe) VALUES";
		for (int i = 0; i < size; i++)
		{
			query += " (?, ?, ?)";
			
			if (i < (size - 1))
				query += ",";
		}
		query += ";";
		
		try (Connection connection = getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			Column<?> clanIdCol = new ColumnInt("clanId", clanId);
			Column<?> safeCol = new ColumnBoolean("safe", safe);
			
			int i = 0;
			for (ClaimLocation claim : chunks)
			{
				String chunk = claim.toStoredString();
				Column<?> chunkCol = new ColumnVarChar("chunk", 100, chunk);
				clanIdCol.setValue(preparedStatement, i + 1);
				chunkCol.setValue(preparedStatement, i + 2);
				safeCol.setValue(preparedStatement, i + 3);
				i += 3;
			}
			
			affectedRows = preparedStatement.executeUpdate();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}

		return affectedRows == size;
	}

	public void addWar(int initiatorId, int clanId, int score, Timestamp currentTime)
	{
		executeUpdate(ADD_CLAN_WAR, new ColumnInt("initiatorId", initiatorId), new ColumnInt("clanId", clanId), new ColumnInt("score", score),
				new ColumnTimestamp("created", currentTime), new ColumnTimestamp("lastUpdated", currentTime));
	}

	public boolean updateWar(int initiatorId, int clanId, int score, Timestamp lastUpdated)
	{
		return 1 == executeUpdate(UPDATE_CLAN_WAR, new ColumnInt("score", score),
				new ColumnTimestamp("lastUpdated", lastUpdated), new ColumnInt("initiatorId", initiatorId), new ColumnInt("clanId", clanId));
	}

	public void deleteAllWar(int clanId)
	{
		executeUpdate(DELETE_ALL_WAR, new ColumnInt("initiatorId", clanId), new ColumnInt("clanId", clanId));
	}

//	public void addEnemy(int clanId, int otherClanId)
//	{
//		executeUpdate(ADD_CLAN_ENEMY, new ColumnInt("clanId", clanId), new ColumnInt("otherClanId", otherClanId));
//	}

	public void removeTerritoryClaim(int clanId, ClaimLocation chunk)
	{
		executeUpdate(DELETE_CLAN_TERRITORY, new ColumnInt("clanId", clanId), new ColumnVarChar("chunk", 100, chunk.toStoredString()));
	}
	
	public void removeTerritoryClaims(int clanId)
	{
		executeUpdate(DELETE_CLAN_ALL_TERRITORY, new ColumnInt("clanId", clanId));
	}

	public void updateClan(int clanId, String name, String desc, String home, boolean admin, int energy, int kills, int murder, int deaths, int warWins, int warLosses, Timestamp lastOnline)
	{
		executeUpdate(UPDATE_CLAN, new ColumnVarChar("name", 100, name), new ColumnVarChar("desc", 100, desc), new ColumnVarChar("home", 100, home), new ColumnBoolean("admin", admin),
				new ColumnInt("energy", energy), new ColumnInt("kills", kills), new ColumnInt("murder", murder), new ColumnInt("deaths", deaths),
				new ColumnInt("warWins", warWins), new ColumnInt("warLosses", warLosses), new ColumnTimestamp("lastOnline", lastOnline), new ColumnInt("clanId", clanId));
	}

//	public void updateEnemy(int clanId, int otherClanId, int clanScore, int otherClanScore, int clanKills, int otherClanKills)
//	{
//		executeUpdate(UPDATE_CLAN_ENEMY, new ColumnInt("clanId", clanId), new ColumnInt("otherClanId", otherClanId),
//				new ColumnInt("clanScore", clanScore), new ColumnInt("otherClanScore", otherClanScore), new ColumnInt("clanKills", clanKills),
//				new ColumnInt("otherClanKills", otherClanKills), new ColumnInt("clanId", clanId), new ColumnInt("otherClanId", otherClanId));
//	}
	
	public int getServerId()
	{
		return _serverId;
	}

	public boolean updateClanGenerator(int clanId, String generator, int generatorStock)
	{
		return executeUpdate(UPDATE_CLAN_GENERATOR, new ColumnVarChar("generator", 140, generator), new ColumnInt("generatorStock", generatorStock), new ColumnInt("clanId", clanId))
				> 0;
	}

	public void updateTerritoryClaim(ClaimLocation chunk, boolean safe)
	{
		executeUpdate(UPDATE_CLAN_TERRITORY, new ColumnBoolean("safe", safe), new ColumnVarChar("chunk", 100, chunk.toStoredString()));
	}
}
