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
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.siege.repository.tokens.SiegeWeaponToken;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnTimestamp;
import mineplex.serverdata.database.column.ColumnVarChar;

public class SiegeWeaponRepository extends RepositoryBase
{
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS clansSiegeWeapons (uniqueId INT NOT NULL,"
                                                                                       + "serverId INT NOT NULL,"
                                                                                       + "location VARCHAR(30),"
                                                                                       + "ownerClan INT NOT NULL,"
                                                                                       + "weaponType TINYINT NOT NULL,"
                                                                                       + "health INT NOT NULL,"
                                                                                       + "yaw INT NOT NULL,"
                                                                                       + "lastFired LONG,"
                                                                                       + "entities VARCHAR(200),"
                                                                                       + "PRIMARY KEY (uniqueId));";

    private static final String GET_WEAPON_BY_ID = "SELECT * FROM clansSiegeWeapons WHERE uniqueId=?;";
    private static final String GET_WEAPONS_BY_CLAN = "SELECT * FROM clansSiegeWeapons WHERE ownerClan=?;";
    private static final String GET_WEAPONS_BY_SERVER = "SELECT * FROM clansSiegeWeapons WHERE serverId=?;";

    private static final String UPDATE_WEAPON = "UPDATE clansSiegeWeapons SET health=?,yaw=?,lastFired=? WHERE uniqueId=?;";
    private static final String INSERT_WEAPON = "INSERT INTO clansSiegeWeapons VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String DELETE_WEAPON = "DELETE FROM clansSiegeWeapons WHERE uniqueId=?;";

    private SiegeManager _siegeManager;

    public SiegeWeaponRepository(JavaPlugin plugin, SiegeManager siegeManager)
    {
        super(DBPool.getAccount());

        _siegeManager = siegeManager;
    }

    public void deleteWeapon(final int uniqueId)
    {
        System.out.println("Siege Repo> Deleting weapon " + uniqueId);

        _siegeManager.runAsync(() ->
            executeUpdate(DELETE_WEAPON, new ColumnInt("uniqueId", uniqueId))
        );
    }

    public void getWeaponById(final int uniqueId, final Callback<SiegeWeaponToken> callback)
    {
        _siegeManager.runAsync(() ->
            executeQuery(GET_WEAPON_BY_ID, resultSet -> {
                SiegeWeaponToken token = new SiegeWeaponToken();

                resultSet.next();

                load(token, resultSet);

                callback.run(token);
            }, new ColumnInt("uniqueId", uniqueId))
        );
    }

    public void getWeaponsByServer(final int serverId, final Callback<List<SiegeWeaponToken>> callback)
    {
        _siegeManager.runAsync(() ->
            executeQuery(GET_WEAPONS_BY_SERVER, resultSet -> {
                List<SiegeWeaponToken> tokens = Lists.newArrayList();

                while (resultSet.next())
                {
                    SiegeWeaponToken token = new SiegeWeaponToken();

                    load(token, resultSet);

                    tokens.add(token);
                }

                callback.run(tokens);
            }, new ColumnInt("serverId", serverId))
        );
    }

    public void getWeaponsByClan(final ClanInfo clan, final Callback<List<SiegeWeaponToken>> callback)
    {
        _siegeManager.runAsync(() ->
            executeQuery(GET_WEAPONS_BY_CLAN, resultSet -> {
                List<SiegeWeaponToken> tokens = Lists.newArrayList();

                while (resultSet.next())
                {
                    SiegeWeaponToken token = new SiegeWeaponToken();

                    load(token, resultSet);

                    tokens.add(token);
                }

                callback.run(tokens);
            }, new ColumnInt("ownerClan", clan.getId()))
        );
    }

    private void load(SiegeWeaponToken token, ResultSet columns) throws SQLException
    {
        token.UniqueId = columns.getInt("uniqueId");
        token.Location = UtilWorld.strToLoc(columns.getString("location"));
        token.OwnerClan = _siegeManager.getClansManager().getClanUtility().getClanById(columns.getInt("ownerClan"));
        token.WeaponType = columns.getByte("weaponType");
        token.Health = columns.getShort("health");
        token.Yaw = columns.getShort("yaw");
        token.LastFired = columns.getTimestamp("lastFired").getTime();

        System.out.println("Siege Repo> Loaded weapon " + token.UniqueId);
    }

    public void updateWeapon(SiegeWeaponToken token)
    {
//        System.out.println("Siege Repo> Updating weapon " + token.UniqueId);

        _siegeManager.runAsync(() ->
            executeUpdate(UPDATE_WEAPON,
                    new ColumnInt("health", token.Health),
                    new ColumnInt("yaw", token.Yaw),
                    new ColumnTimestamp("lastFired", new Timestamp(token.LastFired)),
                    new ColumnInt("uniqueId", token.UniqueId))
        );
    }

    public void insertWeapon(SiegeWeaponToken token)
    {
        System.out.println("Siege Repo> Inserting new weapon " + token.UniqueId);
        
        executeUpdate(INSERT_WEAPON,
            new ColumnInt("uniqueId", token.UniqueId),
            new ColumnInt("serverId", _siegeManager.getClansManager().getServerId()),
            new ColumnVarChar("location", 30, UtilWorld.locToStr(token.Location)),
            new ColumnInt("ownerClan", token.OwnerClan.getId()),
            new ColumnInt("weaponType", token.WeaponType),
            new ColumnInt("health", token.Health),
            new ColumnInt("yaw", token.Yaw),
            new ColumnTimestamp("lastFired", new Timestamp(token.LastFired)),
            new ColumnVarChar("entities", 100, ""));
    }
}
