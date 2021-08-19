package mineplex.queuer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import mineplex.serverdata.Region;
import mineplex.serverdata.Utility;
import mineplex.serverdata.commands.ServerTransfer;
import mineplex.serverdata.commands.TransferCommand;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.data.MinecraftServer;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;

public class QueueRepository 
{

	private DataRepository<QueueParty> _partyRepository;
	
	/**
	 * Class constructor
	 * @param host - the host to connect the QueueRepository to
	 * @param port - the designated port of the QueueRepository database
	 * @param region - the region of server queues to manage
	 */
	public QueueRepository(ConnectionData connectionData, Region region)
	{
		this._partyRepository = new RedisDataRepository<QueueParty>(connectionData, region, 
													QueueParty.class, "queue-parties");
	}
	
	/**
	 * {@code host} defaults to {@value ServerManager#DEFAULT_REDIS_HOST} and
	 * {@code port} defaults to {@value ServerManager#DEFAULT_REDIS_PORT}
	 * 
	 * @see #QueueRepository(String, int, Region)
	 */
	public QueueRepository(Region region)
	{
		this(ServerManager.getMasterConnection(), region);
	}
	
	public QueueParty getQueueParty(int partyId)
	{
		return _partyRepository.getElement(Integer.toString(partyId));
	}
	
	public QueueParty createQueueParty(Collection<String> players, String gameType, int averageElo)
	{
		QueueParty queueParty = new QueueParty(players, gameType, averageElo);
		updateQueueParty(queueParty);
		return queueParty;
	}
	
	public void updateQueueParty(QueueParty queueParty)
	{
		_partyRepository.addElement(queueParty);
	}
	
	public void deleteQueueParty(int partyId)
	{
		_partyRepository.removeElement(Integer.toString(partyId));
	}
	
	public void deleteQueueParty(QueueParty party)
	{
		deleteQueueParty(party.getId());
	}

	public void deleteAssignedParties(int matchId)
	{
		for (QueueParty queueParty : getJoinedQueueParties(matchId))
		{
			deleteQueueParty(queueParty);
		}
	}

	public Collection<QueueParty> getQueueParties()
	{
		return _partyRepository.getElements();
	}
	
	public Collection<QueueParty> getJoinedQueueParties(int matchId)
	{
		Collection<QueueParty> queueParties = new HashSet<QueueParty>();
		
		for (QueueParty queueParty : getQueueParties())
		{
			if (queueParty.getAssignedMatch() == matchId)
			{
				queueParties.add(queueParty);
			}
		}
		
		return queueParties;
	}
	
	public Map<Integer, QueueParty> getMappedQueueParties()
	{
		Map<Integer, QueueParty> queueParties = new HashMap<Integer, QueueParty>();
		
		for (QueueParty queueParty : getQueueParties())
		{
			queueParties.put(queueParty.getId(), queueParty);
		}
		
		return queueParties;
	}
	
	public void assignMatch(QueueParty queueParty, Match match)
	{
		queueParty.setAssignedMatch(match.getId());
		queueParty.setState("Awaiting Confirmation");
		updateQueueParty(queueParty);
	}
	
	public void startMatch(int matchId)
	{
		MinecraftServer emptyServer = getEmptyServer();
		
		if (emptyServer != null)
		{
			for (QueueParty queueParty : getJoinedQueueParties(matchId))
			{
				for (String playerName : queueParty.getPlayers())
				{
					// Execute a transfer command 
					ServerTransfer serverTransfer = new ServerTransfer(playerName, emptyServer.getName());
					TransferCommand transferCommand = new TransferCommand(serverTransfer);
					transferCommand.publish();
				}
			}
		}
	}
	
	protected MinecraftServer getEmptyServer()
	{
		ServerRepository serverRepository = ServerManager.getServerRepository(Region.US);
		Collection<MinecraftServer> servers = serverRepository.getServersByGroup("DominateElo");
		
		for (MinecraftServer server : servers)
		{
			if (server.getPlayerCount() == 0)
			{
				return server;
			}
		}
		
		return null;
	}
	
	public void deleteMatch(int matchId)
	{
		for (QueueParty queueParty : getJoinedQueueParties(matchId))
		{
			queueParty.setAssignedMatch(-1);
			queueParty.setState("Awaiting Match");
			updateQueueParty(queueParty);
		}
	}
}