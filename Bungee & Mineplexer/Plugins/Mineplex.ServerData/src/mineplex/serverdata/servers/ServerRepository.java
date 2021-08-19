package mineplex.serverdata.servers;

import java.util.Collection;
import java.util.List;

import mineplex.serverdata.data.DedicatedServer;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.data.ServerGroup;

/**
 * The ServerRepository is used for storing/retrieving active sessions
 * for {@link MinecraftServer}s, {@link DedicatedServer}s, and {@link ServerGroup}s
 * from a persistent database/repoistory.
 * @author Ty
 *
 */
public interface ServerRepository 
{
	
	
	/**
	 * @return a newly instanced snapshot {@link Collection} of all currently active
	 * {@link MinecraftServer}s in the repository.
	 */
	public Collection<MinecraftServer> getServerStatuses();
	
	public Collection<MinecraftServer> getServerStatusesByPrefix(String prefix);
	
	public Collection<MinecraftServer> getServersByGroup(String serverGroup);
	
	/**
	 * @param serverName - the name of the {@link MinecraftServer} to be fetched.
	 * @return the currently active {@link MinecraftServer} with a matching {@code serverName},
	 * if an active one exists, null otherwise. 
	 */
	public MinecraftServer getServerStatus(String serverName);
	
	/**
	 * Update (or add, if it doesn't already exist) a {@link MinecraftServer}s data
	 * in the repository.
	 * 
	 * A {@link MinecraftServer} must be updated within {@code timeout} milliseconds before
	 * it expires and is removed from the repository.
	 * @param serverData - the {@link MinecraftServer} to add/update in the repository.
	 * @param timeout - the timeout (in milliseconds) before the {@link MinecraftServer} session expires.
	 */
	public void updataServerStatus(MinecraftServer serverData, int timeout);
	
	/**
	 * Remove an active {@link MinecraftServer} from the repository.
	 * @param serverData - the {@link MinecraftServer} to be removed.
	 */
	public void removeServerStatus(MinecraftServer serverData);
	
	/**
	 * @param serverName - the name of the server whose existence is being checked.
	 * @return true, if there exists an active {@link MinecraftServer} session with a
	 * matching {@code serverName}, false otherwise.
	 */
	public boolean serverExists(String serverName);
	
	/**
	 * @return a newly instanced snapshot {@link Collection} of all the
	 * currently active {@link DedicatedServer}s in the repository.
	 */
	public Collection<DedicatedServer> getDedicatedServers();
	
	/**
	 * @return a newly instanced snapshot {@link Collection} of all the 
	 * currently active {@link ServerGroup}s in the repository.
	 */
	public Collection<ServerGroup> getServerGroups(Collection<MinecraftServer> servers);
	
	public ServerGroup getServerGroup(String serverGroup);
	
	public Collection<MinecraftServer> getDeadServers();

	void updateServerGroup(ServerGroup serverGroup);

	public void removeServerGroup(ServerGroup serverGroup);
}
