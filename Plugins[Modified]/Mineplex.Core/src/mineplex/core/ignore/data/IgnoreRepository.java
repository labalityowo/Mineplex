package mineplex.core.ignore.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnVarChar;

public class IgnoreRepository extends RepositoryBase
{
    private static String ADD_IGNORE_RECORD = "INSERT INTO accountIgnore (uuidIgnorer, uuidIgnored) SELECT fA.uuid AS uuidIgnorer, tA.uuid AS uuidIgnored FROM accounts as fA LEFT JOIN accounts AS tA ON tA.name = ? WHERE fA.name = ?;";
    private static String DELETE_IGNORE_RECORD = "DELETE aF FROM accountIgnore AS aF INNER JOIN accounts as fA ON aF.uuidIgnorer = fA.uuid INNER JOIN accounts AS tA ON aF.uuidIgnored = tA.uuid WHERE fA.name = ? AND tA.name = ?;";

    public IgnoreRepository(JavaPlugin plugin)
    {
        super(DBPool.getAccount());
    }

    public boolean addIgnore(final Player caller, String name)
    {
        int rowsAffected = executeUpdate(ADD_IGNORE_RECORD, new ColumnVarChar("name", 100, name), new ColumnVarChar("name", 100,
                caller.getName()));

        return rowsAffected > 0;
    }

    public boolean removeIgnore(String caller, String name)
    {
        int rowsAffected = executeUpdate(DELETE_IGNORE_RECORD, new ColumnVarChar("name", 100, caller), new ColumnVarChar("name",
                100, name));

        return rowsAffected > 0;
    }

    public IgnoreData loadClientInformation(ResultSet resultSet) throws SQLException
    {
        IgnoreData ignoreData = new IgnoreData();

        while (resultSet.next())
        {
            ignoreData.getIgnored().add(resultSet.getString(1));
        }

        return ignoreData;
    }
}