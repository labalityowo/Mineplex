package mineplex.queuer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mineplex.serverdata.Region;
import mineplex.serverdata.data.Data;

public class QueueParty implements Data
{

	private int _id;
	public int getId() { return _id; }
	
	private String _state;
	public String getState() { return _state; }
	public void setState(String state) { this._state = state; }

	private Set<String> _players;
	public Set<String> getPlayers() { return _players; }
	
	private int _assignedMatch;
	public int getAssignedMatch () { return _assignedMatch; }
	public void setAssignedMatch(int assignedMatch) { this._assignedMatch = assignedMatch; }
	
	private int _variance;
	private String _gameType;
	
	private int _averageElo;
	public int getAverageElo() { return _averageElo; }
	
	private int _playerCount;
	public int getPlayerCount() { return _playerCount; }
	
	private long _queueStartTime;
	
	private boolean _prompted;
	public boolean isPrompted() { return _prompted; }
	public void setPrompted(boolean prompted) { this._prompted = prompted; }
	
	private Region _region;
	
	private Set<String> _otherPartyStates;
	public Set<String> getOtherPartyStates() { return _otherPartyStates; }
	public void setOtherPartyStates(Set<String> otherPartyStates) { this._otherPartyStates = otherPartyStates; }
	
	public QueueParty()
	{
		this._id = -1;
		this._state = "Awaiting Match";
		this._assignedMatch = -1;
		this._variance = 25;
		this._prompted = false;
		this._region = Region.US;
		this._players = new HashSet<String>();
		this._otherPartyStates = new HashSet<String>();
		this._queueStartTime = System.currentTimeMillis();
	}
	
	public QueueParty(Collection<String> players, String gameType, int averageElo)
	{
		this._players.addAll(players);
		this._gameType = gameType;
		this._averageElo = averageElo;
	}
	
	public boolean hasAssignedMatch()
	{
		return _assignedMatch != -1;
	}
	
	@Override
	public String getDataId()
	{
		return Integer.toString(_id);
	}
}
