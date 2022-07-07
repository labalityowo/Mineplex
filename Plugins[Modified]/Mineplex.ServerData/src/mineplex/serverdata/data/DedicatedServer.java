package mineplex.serverdata.data;

import java.util.HashMap;
import java.util.Map;

import mineplex.serverdata.Region;

public class DedicatedServer 
{

	// The default amount of available CPU usage.
	public static final int DEFAULT_CPU = 32;
	
	// The default amount of available ram usage.
	public static final int DEFAULT_RAM = 14000;
	
	// The unique name representing this server
	private String _name;
	public String getName() { return _name; }
	
	// The public I.P address used to connect to this server
	private String _publicAddress;
	public String getPublicAddress() { return _publicAddress; }
	
	// The private I.P address of this server
	private String _privateAddress;
	public String getPrivateAddress() { return _privateAddress; }
	
	// The geographical region that this dedicated server is located in
	private Region _region;
	public Region getRegion() { return _region; }
	public boolean isUsRegion() { return _region == Region.US; }
	
	// The amount of available CPU usage on this server box.
	private int _availableCpu;
	public int getAvailableCpu() { return _availableCpu; }
	
	// The amount of available ram usage on this server box.
	private int _availableRam;
	public int getAvailableRam() { return _availableRam; }
	
	// The amount of available CPU usage on this server box.
	private int _maxCpu;
	public int getMaxCpu() { return _maxCpu; }
	
	// The amount of available ram usage on this server box.
	private int _maxRam;
	public int getMaxRam() { return _maxRam; }
	
	// A mapping of server group names (Key) to the number of server instances (Value)
	private Map<String, Integer> _serverCounts;
	
	/**
	 * Class constructor
	 * @param data - the set of serialized data values representing 
	 * the internal state of this DedicatedServer.
	 */
	public DedicatedServer(Map<String, String> data)
	{
		_name = data.get("name");
		_publicAddress = data.get("publicAddress");
		_privateAddress = data.get("privateAddress");
		_region = Region.valueOf(data.get("region").toUpperCase());
		_availableCpu = Integer.valueOf(data.get("cpu"));
		_availableRam = Integer.valueOf(data.get("ram"));
		_maxCpu = Integer.valueOf(data.get("cpu"));
		_maxRam = Integer.valueOf(data.get("ram"));
		_serverCounts = new HashMap<String, Integer>();
	}
	
	/**
	 * Set the number of {@link MinecraftServer} instances on this server
	 * for a specific {@link ServerGroup} type.
	 * @param serverGroup - the {@link ServerGroup} whose server instance count is being set.
	 * @param serverCount - the number of {@link MinecraftServer} instances active on this server.
	 */
	public void setServerCount(ServerGroup serverGroup, int serverCount)
	{
		if (_serverCounts.containsKey(serverGroup.getName()))
		{
			int currentAmount = _serverCounts.get(serverGroup.getName());
			_availableCpu += serverGroup.getRequiredCpu() * currentAmount;
			_availableRam += serverGroup.getRequiredRam() * currentAmount;
		}
		
		_serverCounts.put(serverGroup.getName(), serverCount);
		_availableCpu -= serverGroup.getRequiredCpu() * serverCount;
		_availableRam -= serverGroup.getRequiredRam() * serverCount;
	}
	
	/**
	 * @param serverGroup - the server group whose server count on this dedicated server is being fetched.
	 * @return the number of active {@link MinecraftServer}s on this dedicated server
	 * that belong to {@code serverGroup}.
	 */
	public int getServerCount(ServerGroup serverGroup)
	{
		String groupName = serverGroup.getName();
		return _serverCounts.containsKey(groupName) ? _serverCounts.get(groupName) : 0;
	}
	
	/**
	 * Increment the number of {@link MinecraftServer} instances on this server
	 * for a specific {@link ServerGroup} type by 1.
	 * @param serverGroup - the {@link ServerGroup} whose server instance count is being incremented
	 */
	public void incrementServerCount(ServerGroup serverGroup)
	{
		setServerCount(serverGroup, getServerCount(serverGroup) + 1);
	}
}
