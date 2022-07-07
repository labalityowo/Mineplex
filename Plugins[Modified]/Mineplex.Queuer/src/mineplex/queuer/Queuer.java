package mineplex.queuer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import mineplex.serverdata.Region;
import mineplex.serverdata.commands.ServerTransfer;
import mineplex.serverdata.commands.TransferCommand;
import mineplex.serverdata.data.DedicatedServer;
import mineplex.serverdata.data.ServerGroup;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ServerRepository;
import mineplex.serverprocesses.GenericRunnable;
import mineplex.serverprocesses.ProcessManager;
import mineplex.serverprocesses.ProcessRunner;

public class Queuer
{
	public static final int MIN_QUEUE_WAIT = 0;	// The number of seconds required in queue before creating a new match for a party.
	
	private static QueueRepository _repo;
	private static ServerRepository _serverRepository;
	private static Set<Match> _pendingMatches;	// Set of all matches awaiting players
	private static int _matchId = 0;
	private static int _matchesMade = 0;
	private static int _updates = 0;
	private static int _partyId = 0;
	private static int _serverId = 0;
	
	public static void main (String args[])
	{
		Region region = (!new File("eu.dat").exists()) ? Region.US : Region.EU;
		_serverRepository = ServerManager.getServerRepository(region);
		_repo = new QueueRepository(region);
		_pendingMatches = new HashSet<Match>();
		
		while (true)
		{			
			_updates++;
			updateQueuer();
			ProcessManager.getInstance().updateProcesses();
			
			log("Total pending matches after update: " + _pendingMatches.size());
			log("Total queued parties after update: " + _repo.getQueueParties().size());
			
			try
			{
				if (_matchesMade > 0)
					System.out.println("Made " + _matchesMade + " matches.");
				
				Thread.sleep(1000);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		
	}
		
	/**
	 * Tick & update the Queuer as a whole, making one whole pass through all queued players and pending matches to
	 * assign matches to parties and start matches.
	 */
	private static void updateQueuer()
	{
		// Update the status of each queue party, searching for best matchings and assigning matches to parties.
		for (QueueParty queueParty : _repo.getQueueParties())
		{
			updateParty(queueParty);
		}
		
		// Update all matches, and remove pending matches if they are finished.
		Iterator<Match> iterator = _pendingMatches.iterator();
		while (iterator.hasNext())
		{
			Match match = iterator.next();
			boolean matchFinished = updateMatch(match);
			
			// Remove match if it is completed/finished
			if (matchFinished) iterator.remove();
		}
	}
	
	/**
	 * Update the status of a {@link QueueParty} by attempting to locate the best resulting
	 * {@code Match} available, or creating a new one if required.
	 * @param queueParty - the queue party to be updated for matchmaking purposes.
	 */
	private static void updateParty(QueueParty queueParty)
	{
		int queueDuration = (int) queueParty.getQueueDuration() / 1000;	// Queue duration in seconds
		Match bestMatch = getBestMatch(queueParty);
		
		if (queueParty.hasAssignedMatch())
		{
			// TODO: If player has been waiting too long in current game and there is a better match, join that!
		}
		else
		{
			if (bestMatch != null)						// Assign party into best match!
			{
				bestMatch.joinQueueParty(queueParty);
				_repo.assignMatch(queueParty, bestMatch);
			}
			else if (queueDuration >= MIN_QUEUE_WAIT)	// Create a new match for others to join!
			{
				Match match = new Match(_matchId++, queueParty.getServerGroup(), queueParty);
				_pendingMatches.add(match);
				_repo.assignMatch(queueParty, match);
			}
		}
	}
	
	/**
	 * Update a {@link Match} by verifying it's player statuses, sending out invites
	 * and managing a Match from creation to deletion.
	 * @param match - the match to be updated.
	 * @return true, if the match is no longer required (successful or otherwise) and should be removed, false otherwise.
	 */
	private static boolean updateMatch(Match match)
	{
		// Remove queued parties that have left queue/match
		// Don't give me crap about not using iterator...can't cuz of stupid thing.
		Set<QueueParty> partiesToRemove = new HashSet<QueueParty>();
		for (QueueParty queueParty : match.getParties())
		{
			int partyId = queueParty.getId();
			if (!_repo.queuePartyExists(partyId))
			{
				log("Removing matchStatus : " + queueParty.getId());
				partiesToRemove.add(queueParty);
				
				if (match.isWaitingForInvites())
				{
					_repo.deleteMatch(match.getId());
					match.setWaitingForInvites(false);
				}
			}
		}
		for (QueueParty party : partiesToRemove)
		{
			match.quitQueueParty(party);
		}
		
		// If match took too long to find players, or is empty, quit match.
		if (match.getPlayerCount() == 0)
		{
			return true;	// Match is empty, remove from pending matches.
		}
		
		// If match sent invites and is waiting for too long (15 seconds), kick players who didn't
		// accept and keep looking
		// Otherwise if everyone accepted, start game!
		if (match.isWaitingForInvites())
		{
			boolean matchReady = true;
			for (QueueParty party : _repo.getJoinedQueueParties(match.getId()))
			{
				if (!party.isReady())
				{
					matchReady = false;
				}
			}
			
			if (!matchReady && match.getWaitDuration() > 500)
			{
				matchReady = true;
			}
			
			if (match.isReady())	// Invites accepted, MinecraftServer started, and players transferred.
			{
				return true;
			}
			else if (matchReady)	// Players accepted invites, start match!
			{
				startMatch(match);
				return false;
			}
			else if (match.getWaitDuration() > 15000)
			{
				for (QueueParty queueParty : match.getParties())
				{
					if (!queueParty.isReady())
					{
						_repo.deleteQueueParty(queueParty.getId());
					}
				}
				
				_repo.deleteMatch(match.getId());
				match.setWaitingForInvites(false);
			}
			
			return false;
		}
		
		// Match has filled up, send out invites!
		if (match.getOpenSlots() ==  0)
		{			
			for (QueueParty party : match.getParties())
			{
				_repo.sendInvite(party);
			}
			
			match.setWaitingForInvites(true);
		}
		
		return false;
	}
	
	/**
	 * @param queueParty - the party for whom a match is being searched.
	 * @return the best matching {@link Match} for the {@code queueParty}, if an acceptable {@link Match}
	 * could be found, null otherwise.
	 */
	private static Match getBestMatch(QueueParty queueParty)
	{
		Match best = null;
		int minEloDelta = 0;
		int variance = getSearchVariance(queueParty);
		
		for (Match match : _pendingMatches)
		{
			if (match.getOpenSlots() >= queueParty.getPlayerCount())
			{
				int eloDelta = getEloDelta(queueParty, match);
				if (eloDelta <= variance && (eloDelta < minEloDelta || best == null))
				{
					best = match;
					minEloDelta = eloDelta;
				}
			}
		}
		
		return best;
	}
	
	/**
	 * @param r1 
	 * @param r2
	 * @return the ELO point delta (difference) between two {@link Ranked} objects.
	 */
	private static int getEloDelta(Ranked r1, Ranked r2)
	{
		return Math.abs(r1.getElo() - r2.getElo());
	}
	
	public static boolean startMatch(Match match)
	{
		ServerGroup group = match.getServerGroup();
		DedicatedServer bestServer = getBestDedicatedServer(group);
		startServer(bestServer, group, match);
		return true;
	}
	
	/**
	 * Transfer all players queue'd into a {@link Match} to one server.
	 * @param match - the match whose queue'd players are to be transferred
	 * @param serverName - the name of the server to transfer the players to
	 */
	public static void transferPlayers(Match match, String serverName)
	{
		// Transfer players to the server
		for (QueueParty queueParty : _repo.getJoinedQueueParties(match.getId()))
		{
			for (String playerName : queueParty.getPlayers())
			{
				// Execute a transfer command 
				ServerTransfer serverTransfer = new ServerTransfer(playerName, serverName);
				TransferCommand transferCommand = new TransferCommand(serverTransfer);
				transferCommand.publish();
			}
		}
		
		// Server transfers sent out, match has started!
		_matchesMade++;

		// Delete queue parties for players who've been matched
		for (QueueParty queueParty : match.getParties())
		{
			if (!queueParty.isReady())
			{
				_repo.deleteQueueParty(queueParty.getId());
			}
		}
		match.setReady(true);	// Ready to be deleted/removed
	}
	
	/**
	 * @return newly generated unique server id.
	 */
	public static int generateServerId()
	{
		return _serverId++;
	}
	
	private static void startServer(final DedicatedServer serverSpace, final ServerGroup serverGroup, final Match match)
	{
		String cmd = "/home/mineplex/easyRemoteStartServerCustom.sh";
		final String groupPrefix = serverGroup.getPrefix();
		final String serverName = serverSpace.getName();
		final String serverAddress = serverSpace.getPublicAddress();
		final int serverId = generateServerId();

		ProcessRunner pr = new ProcessRunner(new String[] {"/bin/sh", cmd, serverAddress, serverSpace.getPrivateAddress(), (serverGroup.getPortSection() + serverId) + "", serverGroup.getRequiredRam() + "", serverGroup.getWorldZip(), serverGroup.getPlugin(), serverGroup.getConfigPath(), serverGroup.getName(), serverGroup.getPrefix() + "-" + serverId, serverSpace.isUsRegion() ? "true" : "false", serverGroup.getAddNoCheat() + "" });
		pr.start(new GenericRunnable<Boolean>()		
		{
			public void run(Boolean error)
			{
				if (!error)
				{
					// Successfully started server, now transfer players
					transferPlayers(match, serverName);
				}
				else
				{
					// TODO: Error in starting server for ELO match, try again or disband queued match?
					log("[" + serverName + ":" + serverAddress + " Free Resources; CPU " + serverSpace.getAvailableCpu() + " RAM " + serverSpace.getAvailableRam() + "MB] Errored " + serverName + "(" + groupPrefix+ "-" + serverId + ")");
				}

			}
		});
		
		ProcessManager.getInstance().addProcess(pr);		
		serverSpace.incrementServerCount(serverGroup);
	}
	
	private static DedicatedServer getBestDedicatedServer(ServerGroup serverGroup)
	{
		Collection<DedicatedServer> dedicatedServers = _serverRepository.getDedicatedServers();
		DedicatedServer bestServer = null;
		
		for (DedicatedServer serverData : dedicatedServers)
		{
			if (serverData.getAvailableRam() > serverGroup.getRequiredRam() 
					&& serverData.getAvailableCpu() > serverGroup.getRequiredCpu())
			{
				if (bestServer == null || serverData.getServerCount(serverGroup) < bestServer.getServerCount(serverGroup))
				{
					bestServer = serverData;
				}
			}
		}
		
		return bestServer;
	}
	
	/**
	 * @param party - the party whose ELO search variance is being fetched.
	 * @return the variance in ELO search parameters for {@code party}.
	 * I.E: Queuer searches for potential matches in [party.elo - variance, party.elo + variance] ELO range.
	 */
	private static int getSearchVariance(QueueParty party)
	{
		int seconds = (int) party.getQueueDuration() / 1000;	// Duration of queue in seconds
		
		return seconds * 10;	// 5 ELO variance for every second in queue
	}
	
	private static void log(String message)
	{
		System.out.println(message);
	}
}
