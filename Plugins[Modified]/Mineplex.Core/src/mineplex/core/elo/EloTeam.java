package mineplex.core.elo;

import java.util.Collection;

import mineplex.core.common.util.NautHashMap;

public class EloTeam
{
	private NautHashMap<String, EloPlayer> _players = new NautHashMap<>();
	public int TotalElo = 0;
	public boolean Winner = false;
	
	public void addPlayer(EloPlayer player)
	{
		TotalElo += player.getRating();
		
		_players.put(player.getPlayer().getUniqueId().toString(), player);
	}
	
	public EloPlayer getPlayer(String uuid)
	{
		return _players.get(uuid);
	}
	
	public Collection<EloPlayer> getPlayers()
	{
		return _players.values();
	}
}
