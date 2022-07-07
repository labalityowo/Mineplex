package mineplex.game.clans.clans.nether;

import mineplex.core.common.util.UtilWorld;
import mineplex.core.database.MinecraftRepository;
import mineplex.serverdata.database.DBPool;
import mineplex.serverdata.database.RepositoryBase;
import mineplex.serverdata.database.column.ColumnBoolean;
import mineplex.serverdata.database.column.ColumnInt;
import mineplex.serverdata.database.column.ColumnVarChar;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Database repository class for nether portals
 */
public class PortalRepository extends RepositoryBase
{
    private static final String CREATE = "CREATE TABLE IF NOT EXISTS clansNetherPortals (id INT NOT NULL AUTO_INCREMENT,"
                                                                                       + "cornerOne VARCHAR(30),"
                                                                                       + "cornerTwo VARCHAR(30),"
                                                                                       + "returnPortal BOOL,"
                                                                                       + "PRIMARY KEY (id));";

    private static final String GET_PORTALS = "SELECT * FROM clansNetherPortals;";
    private static final String INSERT_PORTAL = "INSERT INTO clansNetherPortals (cornerOne, cornerTwo, returnPortal) VALUES (?, ?, ?);";
    private static final String DELETE_PORTAL = "DELETE FROM clansNetherPortals WHERE id=?;";

    private NetherManager _nether;

    public PortalRepository(JavaPlugin plugin, NetherManager portalManager)
    {
        super(DBPool.getAccount());

        _nether = portalManager;
    }
    
    /**
     * Loads all stored portals
     */
    public void loadPortals()
    {
    	_nether.runAsync(() ->
    	{
    		executeQuery(GET_PORTALS, resultSet ->
    		{
    			while (resultSet.next())
    			{
    				final int id = resultSet.getInt("id");
    				final String cornerOne = resultSet.getString("cornerOne");
    				final String cornerTwo = resultSet.getString("cornerTwo");
    				final boolean returnPortal = resultSet.getBoolean("returnPortal");
    				
    				_nether.runSync(() ->
    				{
    					NetherPortal portal = new NetherPortal(id, UtilWorld.strToLoc(cornerOne), UtilWorld.strToLoc(cornerTwo), returnPortal);
    					_nether.addPortal(portal);
    				});
    			}
    		});
    	});
    }
    
    /**
     * Adds a portal into the database and loaded portals
     * @param cornerOne The serialized first corner of the portal
     * @param cornerTwo The serialized second corner of the portal
     * @param returnPortal Whether the portal is a return portal
     */
    public void addPortal(final String cornerOne, final String cornerTwo, final boolean returnPortal)
    {
    	_nether.runAsync(() ->
    	{
    		executeInsert(INSERT_PORTAL, resultSet ->
    		{
    			while (resultSet.next())
    			{
    				final int id = resultSet.getInt(1);
    				_nether.runSync(() ->
    				{
    					NetherPortal portal = new NetherPortal(id, UtilWorld.strToLoc(cornerOne), UtilWorld.strToLoc(cornerTwo), returnPortal);
    					_nether.addPortal(portal);
    				});
    			}
    		}, new ColumnVarChar("cornerOne", 30, cornerOne), new ColumnVarChar("cornerTwo", 30, cornerTwo), new ColumnBoolean("returnPortal", returnPortal));
    	});
    }
    
    /**
     * Deletes the portal with the given id from the database
     * @param id The id of the portal to delete
     */
    public void deletePortal(final int id)
    {
    	_nether.runAsync(() ->
    	{
    		executeUpdate(DELETE_PORTAL, new ColumnInt("id", id));
    	});
    }
}