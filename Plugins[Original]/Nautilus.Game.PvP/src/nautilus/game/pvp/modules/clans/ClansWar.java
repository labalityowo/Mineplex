package nautilus.game.pvp.modules.clans;

public class ClansWar 
{
	private Clans Clans;
	
	private ClansClan _clanA;
	private ClansClan _clanB;
	private int _dom;
	private long _created;
	
	public ClansWar(Clans clans, ClansClan a, ClansClan b, int dom, long created)
	{
		Clans = clans;
		
		_clanA = a;
		_clanB = b;
		_dom = dom;
		_created = created;
	}
	
	public ClansClan GetClanA()
	{
		return _clanA;
	}
	
	public ClansClan GetClanB()
	{
		return _clanB;
	}

	public int GetDominance()
	{
		return _dom;
	}
	
	public long GetCreated()
	{
		return _created;
	}

	public boolean SetDominance(int dom) 
	{
		if (dom < 0)
			dom = 0;

		if (dom > Clans.GetDominanceLimit())
			dom = Clans.GetDominanceLimit();
		
		_dom = dom;
		
		//Save
		Clans.CRepo().EditClan(_clanA.GetToken());
		
		if (dom >= Clans.GetDominanceLimit())
		{
			Clans.EndWar(GetClanA(), GetClanB());
			return false;
		}
		
		return true;
	}
}
