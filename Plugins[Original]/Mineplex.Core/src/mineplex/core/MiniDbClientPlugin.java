package mineplex.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.common.util.UtilServer;

public abstract class MiniDbClientPlugin<DataType> extends MiniClientPlugin<DataType> implements ILoginProcessor
{

	protected final CoreClientManager ClientManager;

	public MiniDbClientPlugin(String moduleName)
	{
		this(moduleName, UtilServer.getPlugin(), Managers.require(CoreClientManager.class));
	}

	public MiniDbClientPlugin(String moduleName, JavaPlugin plugin, CoreClientManager clientManager) 
	{
		super(moduleName, plugin);
		
		ClientManager = clientManager;
		
		clientManager.addStoredProcedureLoginProcessor(this);
	}

	public abstract void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException;
	
	public CoreClientManager getClientManager()
	{
		return ClientManager;
	}
}
