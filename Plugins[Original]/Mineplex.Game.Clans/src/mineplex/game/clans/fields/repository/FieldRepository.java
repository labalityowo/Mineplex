package mineplex.game.clans.fields.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mineplex.core.database.MinecraftRepository;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.ResultSetCallable;
import mineplex.serverdata.database.column.ColumnByte;
import mineplex.serverdata.database.column.ColumnDouble;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

public class FieldRepository extends RepositoryBase
{
	private static String ALL_STRING = "ALL";

	private static String CREATE_FIELD_BLOCK_TABLE = "CREATE TABLE IF NOT EXISTS fieldBlock (id INT NOT NULL AUTO_INCREMENT, server VARCHAR(100), location VARCHAR(100), blockId INT, blockData TINYINT, emptyId INT, emptyData TINYINT, stockMax INT, stockRegenTime DOUBLE, loot VARCHAR(100), PRIMARY KEY (id), INDEX serverLocation (server, location));";
	private static String CREATE_FIELD_ORE_TABLE = "CREATE TABLE IF NOT EXISTS fieldOre (id INT NOT NULL AUTO_INCREMENT, server VARCHAR(100), location VARCHAR(100), PRIMARY KEY (id), INDEX serverLocation (server, location));";
	private static String CREATE_FIELD_MONSTER_TABLE = "CREATE TABLE IF NOT EXISTS fieldMonster (id INT NOT NULL AUTO_INCREMENT, server VARCHAR(100), name VARCHAR(100), type VARCHAR(100), mobMax INT, mobRate DOUBLE, center VARCHAR(100), radius INT, height INT, PRIMARY KEY (id), INDEX serverName (server, name));";
	private static String RETRIEVE_FIELD_BLOCKS = "SELECT server, location, blockId, blockData, emptyId, emptyData, stockMax, stockRegenTime, loot FROM fieldBlock WHERE server = ? OR server = \"" + ALL_STRING + "\";";
	private static String ADD_FIELD_BLOCK = "INSERT INTO fieldBlock (server, location, blockId, blockData, emptyId, emptyData, stockMax, stockRegenTime, loot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static String DEL_FIELD_BLOCK = "DELETE FROM fieldBlock WHERE server = ? AND location = ?;";
	private static String RETRIEVE_FIELD_ORES = "SELECT server, location FROM fieldOre WHERE server = ? OR server = \"" + ALL_STRING + "\";";
	private static String ADD_FIELD_ORE = "INSERT INTO fieldOre (server, location) VALUES (?, ?);";
	private static String DEL_FIELD_ORE = "DELETE FROM fieldOre WHERE server = ? AND location = ?;";
	private static String RETRIEVE_FIELD_MONSTERS = "SELECT server, name, type, mobMax, mobRate, center, radius, height FROM fieldMonster WHERE server = ? OR server = \"" + ALL_STRING + "\";";
	private static String ADD_FIELD_MONSTER = "INSERT INTO fieldMonster (server, name, type, mobMax, mobRate, center, radius, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	private static String DEL_FIELD_MONSTER = "DELETE FROM fieldMonster WHERE server = ? AND name = ?;";

	public FieldRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public List<FieldBlockToken> getFieldBlocks(String server)
	{		
		final List<FieldBlockToken> fieldBlocks = new ArrayList<FieldBlockToken>();
		
		executeQuery(RETRIEVE_FIELD_BLOCKS, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					FieldBlockToken token = new FieldBlockToken();
					token.Server = resultSet.getString(1);
					token.Location = resultSet.getString(2);
					token.BlockId = resultSet.getInt(3);
					token.BlockData = resultSet.getByte(4);
					token.EmptyId = resultSet.getInt(5);
					token.EmptyData = resultSet.getByte(6);
					token.StockMax = resultSet.getInt(7);
					token.StockRegenTime = resultSet.getLong(8);
					token.Loot = resultSet.getString(9);
					
					fieldBlocks.add(token);
				}
			}
		}, new ColumnVarChar("server", 100, server));
		
		return fieldBlocks;
	}

	public void addFieldBlock(FieldBlockToken token)
	{
		executeUpdate(ADD_FIELD_BLOCK,
				new ColumnVarChar("server", 100, token.Server),
				new ColumnVarChar("location", 100, token.Location),
				new ColumnInt("blockId", token.BlockId),
				new ColumnByte("blockData", token.BlockData),
				new ColumnInt("emptyId", token.EmptyId),
				new ColumnByte("emptyData", token.EmptyData),
				new ColumnInt("stockMax", token.StockMax),
				new ColumnDouble("stockRegen", token.StockRegenTime),
				new ColumnVarChar("loot", 100, token.Loot)
		);
	}

	public void deleteFieldBlock(String server, String location)
	{
		executeUpdate(DEL_FIELD_BLOCK, new ColumnVarChar("server", 100, server), new ColumnVarChar("location", 100, location));
	}

	public List<FieldOreToken> getFieldOres(String server)
	{
		final List<FieldOreToken> fieldOres = new ArrayList<FieldOreToken>();
		
		this.executeQuery(RETRIEVE_FIELD_ORES, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					FieldOreToken token = new FieldOreToken();
					token.Server = resultSet.getString(1);
					token.Location = resultSet.getString(2);
					
					fieldOres.add(token);
				}
			}
		}, new ColumnVarChar("server", 100, server));
		
		return fieldOres;
	}

	public void addFieldOre(FieldOreToken token)
	{
		executeUpdate(ADD_FIELD_ORE,
				new ColumnVarChar("server", 100, token.Server),
				new ColumnVarChar("location", 100, token.Location)
		);
	}

	public void deleteFieldOre(String server, String location)
	{
		executeUpdate(DEL_FIELD_ORE, new ColumnVarChar("server", 100, server), new ColumnVarChar("location", 100, location));
	}

	public List<FieldMonsterToken> getFieldMonsters(String server)
	{
		final List<FieldMonsterToken> fieldMonsters = new ArrayList<FieldMonsterToken>();
		
		executeQuery(RETRIEVE_FIELD_MONSTERS, new ResultSetCallable()
		{
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException
			{
				while (resultSet.next())
				{
					FieldMonsterToken token = new FieldMonsterToken();
					token.Server = resultSet.getString(1);
					token.Name = resultSet.getString(2);
					token.Type = resultSet.getString(3);
					token.MobMax = resultSet.getInt(4);
					token.MobRate = resultSet.getDouble(5);
					token.Centre = resultSet.getString(6);
					token.Radius = resultSet.getInt(7);
					token.Height = resultSet.getInt(8);
					
					fieldMonsters.add(token);
				}
			}
		}, new ColumnVarChar("server", 100, server));
		
		return fieldMonsters;
	}

	public void addFieldMonster(FieldMonsterToken token)
	{
		executeUpdate(ADD_FIELD_MONSTER,
				new ColumnVarChar("server", 100, token.Server),
				new ColumnVarChar("name", 100, token.Name),
				new ColumnVarChar("blockId", 100, token.Type),
				new ColumnInt("mobMax", token.MobMax),
				new ColumnDouble("mobRate", token.MobRate),
				new ColumnVarChar("center", 100, token.Centre),
				new ColumnInt("radius", token.Radius),
				new ColumnInt("height", token.Height)
		);
	}

	public void deleteFieldMonster(FieldMonsterToken token)
	{
		executeUpdate(DEL_FIELD_MONSTER, new ColumnVarChar("server", 100, token.Server), new ColumnVarChar("name", 100, token.Name));
	}

	@Override
	protected void initialize()
	{
		executeUpdate(CREATE_FIELD_BLOCK_TABLE);
		executeUpdate(CREATE_FIELD_ORE_TABLE);
		executeUpdate(CREATE_FIELD_MONSTER_TABLE);
	}
}
