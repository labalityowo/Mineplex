package mineplex.core.tournament.data;

import mineplex.core.common.util.NautHashMap;

public class Tournament
{
	public int TournamentId = -1;
	public String Name;
	public String GameType;
	public long Date = 0;
	public NautHashMap<Integer, TournamentTeam> Teams = new NautHashMap<>();
	
}
