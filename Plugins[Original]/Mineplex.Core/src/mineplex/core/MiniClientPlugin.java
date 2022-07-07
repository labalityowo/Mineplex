package mineplex.core;

import mineplex.core.account.event.ClientUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class MiniClientPlugin<DataType> extends MiniPlugin
{
	private static final Object _clientDataLock = new Object();

	private final Map<UUID, DataType> _clientData = new HashMap<>();

	public MiniClientPlugin(String moduleName, JavaPlugin plugin)
	{
		super(moduleName, plugin);
	}

	public MiniClientPlugin(String moduleName)
	{
		super(moduleName);
	}

	@EventHandler
	public void UnloadPlayer(ClientUnloadEvent event)
	{
		synchronized (_clientDataLock)
		{
			saveData(event.GetName(), event.getUniqueId(), event.getAccountId());
			_clientData.remove(event.getUniqueId());
		}
	}

	@Deprecated
	public DataType Get(String name)
	{
		Player player = Bukkit.getPlayerExact(name);
		if (player == null)
			return null;
		return Get(player);
	}

	public DataType Get(UUID uuid)
	{
		synchronized (_clientDataLock)
		{
			return _clientData.computeIfAbsent(uuid, this::addPlayer);
		}
	}
	
	public void saveData(String name, UUID uuid, int accountId) {}
	
	public DataType Get(Player player)
	{
		return Get(player.getUniqueId());
	}

	protected Collection<DataType> GetValues()
	{
		return _clientData.values();
	}
	
	protected void Set(Player player, DataType data)
	{
		Set(player.getUniqueId(), data);
	}

	protected void Set(UUID uuid, DataType data)
	{
		synchronized (_clientDataLock)
		{
			_clientData.put(uuid, data);
		}
	}
	
	protected abstract DataType addPlayer(UUID uuid);
}
