package mineplex.core.benefit;

import java.sql.ResultSet;
import java.sql.SQLException;

import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

import org.bukkit.plugin.java.JavaPlugin;

public class BenefitManagerRepository extends RepositoryBase
{
	private static String CREATE_BENEFIT_TABLE = "CREATE TABLE IF NOT EXISTS rankBenefits (id INT NOT NULL AUTO_INCREMENT, accountId INT, benefit VARCHAR(100), PRIMARY KEY (id), FOREIGN KEY (accountId) REFERENCES accounts(id));";
	
	private static String INSERT_BENEFIT = "INSERT INTO rankBenefits (accountId, benefit) VALUES (?, ?);";
	private static String DELETE_BENEFIT = "DELETE FROM rankBenefits WHERE accountId = ? AND benefit = ?;";
		
	public BenefitManagerRepository(JavaPlugin plugin)
	{
		super(DBPool.getAccount());
	}

	public boolean addBenefit(int accountId, String benefit)
	{
		return executeUpdate(INSERT_BENEFIT, new ColumnInt("accountId", accountId), new ColumnVarChar("benefit", 100, benefit)) > 0;
	}

	public BenefitData retrievePlayerBenefitData(ResultSet resultSet) throws SQLException
	{
		BenefitData playerBenefit = new BenefitData();
		
		while (resultSet.next())
		{
			playerBenefit.Benefits.add(resultSet.getString(1));			
		}
		
		playerBenefit.Loaded = true;
		
		return playerBenefit;
	}

	public void removeBenefit(int accountId, String benefit) 
	{
		executeUpdate(DELETE_BENEFIT, new ColumnInt("accountId", accountId), new ColumnVarChar("benefit", 100, benefit));
	}
}
