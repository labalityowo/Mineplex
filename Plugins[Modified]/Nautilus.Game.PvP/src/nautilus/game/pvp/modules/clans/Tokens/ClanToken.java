package nautilus.game.pvp.modules.clans.Tokens;

import java.util.List;

public class ClanToken 
{
	public int ClanId;
	public String Name;
	public String Description;
	public int Power;
	public String Home;
	public boolean Admin;
	public long DateCreated;
	public long LastTimeOnline;

	public ClanGeneratorToken Generator;
	public List<ClanMemberToken> Members;
	public List<ClanTerritoryToken> Territories;
	
	public List<AllianceToken> Alliances;
	public List<WarToken> Wars;
}
