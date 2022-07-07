package mineplex.game.clans.core.repository.tokens;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClanToken
{
	public int Id;
	public String Name;
	public String Description;
	public String Home;
	public boolean Admin;
	public int Energy;
	public int Kills;
	public int Murder;
	public int Deaths;
	public int WarWins;
	public int WarLosses;
	public int EloRating;
	public String GeneratorBuyer;
	public int GeneratorStock;
	public Timestamp DateCreated;
	public Timestamp LastOnline;

	public List<ClanMemberToken> Members = new ArrayList<ClanMemberToken>();
	public List<ClanTerritoryToken> Territories = new ArrayList<ClanTerritoryToken>();
	public List<ClanAllianceToken> Alliances = new ArrayList<ClanAllianceToken>();
	public List<ClanWarToken> WarsIn = new ArrayList<ClanWarToken>();
	public List<ClanWarToken> WarsOut = new ArrayList<ClanWarToken>();
}
