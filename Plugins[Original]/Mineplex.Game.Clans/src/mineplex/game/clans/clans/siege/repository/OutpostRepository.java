package mineplex.game.clans.clans.siege.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.database.MinecraftRepository;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.siege.outpost.OutpostManager;
import mineplex.game.clans.clans.siege.outpost.OutpostState;
import mineplex.game.clans.clans.siege.outpost.OutpostType;
import mineplex.game.clans.clans.siege.repository.tokens.OutpostToken;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnByte;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

public class OutpostRepository extends RepositoryBase
{
	private OutpostManager _manager;
	
	private static final String CREATE =       "CREATE TABLE IF NOT EXISTS clansOutposts (uniqueId INT NOT NULL,"
																					   + "serverId INT NOT NULL,"
																					   + "origin VARCHAR(30),"
																					   + "outpostType TINYINT NOT NULL,"
																					   + "ownerClan INT NOT NULL,"
																					   + "timeSpawned LONG,"
																					   + "outpostState TINYINT NOT NULL,"
																					   + "PRIMARY KEY (uniqueId));";
	
	private static final String GET_OUTPOST_BY_ID = "SELECT * FROM clansOutposts WHERE uniqueId=?;";
	private static final String GET_OUTPOST_BY_CLAN = "SELECT * FROM clansOutposts WHERE ownerClan=?;";
	private static final String GET_OUTPOSTS_BY_SERVER = "SELECT * FROM clansOutposts WHERE serverId=?;";
	
	private static final String UPDATE_OUTPOST = "UPDATE clansOutposts SET outpostState=? WHERE uniqueId=?;";
	private static final String INSERT_OUTPOST = "INSERT INTO clansOutposts VALUES (?, ?, ?, ?, ?, ?, ?);";
	
	private static final String DELETE_OUTPOST = "DELETE FROM clansOutposts WHERE uniqueId=?;";
	
	public OutpostRepository(JavaPlugin plugin, OutpostManager manager)
	{
		super(DBPool.getAccount());
		
		_manager = manager;
	}
	
	public void deleteOutpost(final int uniqueId)
	{
		executeUpdate(DELETE_OUTPOST, new ColumnInt("uniqueId", uniqueId));
	}
	
	public void getOutpostById(final int uniqueId, final Callback<OutpostToken> callback)
	{
		executeQuery(GET_OUTPOST_BY_ID, resultSet -> {
			OutpostToken token = new OutpostToken();
			
			resultSet.next();
			
			load(token, resultSet);
			
			callback.run(token);
		}, new ColumnInt("uniqueId", uniqueId));
	}

	public void getOutpostByClan(final ClanInfo clan, final Callback<OutpostToken> callback)
	{
		executeQuery(GET_OUTPOST_BY_CLAN, resultSet -> {
			resultSet.next();
			
			OutpostToken token = new OutpostToken();
			
			load(token, resultSet);
			
			callback.run(token);
		}, new ColumnInt("ownerClan", clan.getId()));
	}
	
	public void getOutpostsByServer(final int serverId, final Callback<List<OutpostToken>> callback)
	{
		executeQuery(GET_OUTPOSTS_BY_SERVER, resultSet -> {
			List<OutpostToken> tokens = Lists.newArrayList();
			
			while (resultSet.next())
			{
				OutpostToken token = new OutpostToken();
				load(token, resultSet);
				tokens.add(token);
			}
			
			callback.run(tokens);
		}, new ColumnInt("serverId", serverId));
	}
	
	private void load(OutpostToken token, ResultSet columns) throws SQLException
	{
		token.UniqueId = columns.getInt("uniqueId");
		token.Origin = UtilWorld.strToLoc(columns.getString("origin"));
		token.Type = OutpostType.ById(columns.getByte("outpostType"));
		token.OwnerClan = _manager.getClansManager().getClanUtility().getClanById(columns.getInt("ownerClan"));
		token.TimeSpawned = columns.getTimestamp("timeSpawned").getTime();
		token.OutpostState = OutpostState.ById(columns.getByte("outpostState"));
	}
	
	@Override
	protected void initialize()
	{
		executeUpdate(CREATE);
	}

	public void updateOutpost(OutpostToken token)
	{
		executeUpdate(UPDATE_OUTPOST,
				new ColumnByte("outpostState", token.OutpostState.getId()),
				new ColumnInt("uniqueId", token.UniqueId));
	}

	public void insertOutpost(OutpostToken token)
	{
		executeUpdate(INSERT_OUTPOST, 
				new ColumnInt("uniqueId", token.UniqueId),
				new ColumnInt("serverId", _manager.getClansManager().getServerId()),
				new ColumnVarChar("origin", 30, UtilWorld.locToStr(token.Origin)),
				new ColumnInt("outpostType", token.Type.getId()),
				new ColumnInt("ownerClan", token.OwnerClan.getId()),
				new ColumnTimestamp("timeSpawned", new Timestamp(token.TimeSpawned)),
				new ColumnByte("outpostState", token.OutpostState.getId()));
	}
}