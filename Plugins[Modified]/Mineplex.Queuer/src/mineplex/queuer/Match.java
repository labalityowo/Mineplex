package mineplex.queuer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Match 
{
	private int _id;
	public int getId() { return _id; }
	
	private Set<QueueParty> _parties;
	public Set<QueueParty> getParties() { return _parties; }
	
	private boolean _waitingForInvites;
	public boolean isWaitingForInvites() { return _waitingForInvites; }
	
	private long _waitingStartTime;
	public long getWaitDuration() { return System.currentTimeMillis() - _waitingStartTime; }
	
	private int _averageElo;
	public int getAverageElo() { return _averageElo; }
	
	public Match(int id, int averageElo, QueueParty... parties)
	{
		this._id = id;
		this._averageElo = averageElo;
		
		for (QueueParty party : parties)
		{
			joinQueueParty(party);
		}
	}
	
	/**
	 * Add a {@link QueueParty} to this match.
	 * @param queueParty
	 */
	public void joinQueueParty(QueueParty queueParty)
	{
		_parties.add(queueParty);
	}

	/**
	 * Remove a {@link QueueParty} from this match.
	 * @param queueParty
	 */
	public void quitQueueParty(QueueParty queueParty)
	{
		_parties.remove(queueParty);
	}
	
	public int getPlayerCount()
	{
		int playerCount = 0;
		
		for (QueueParty party : _parties)
		{
			playerCount += party.getPlayerCount();
		}
		
		return playerCount;
	}
	
	public void setWaitingForInvites(boolean waitingForInvites)
	{ 
		this._waitingForInvites = waitingForInvites;
		
		if (waitingForInvites)
		{
			this._waitingStartTime = System.currentTimeMillis();
		}
	}
}
