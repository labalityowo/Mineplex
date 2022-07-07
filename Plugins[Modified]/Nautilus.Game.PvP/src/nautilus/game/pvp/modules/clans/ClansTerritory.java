package nautilus.game.pvp.modules.clans;


public class ClansTerritory 
{
	public String owner = "";
	public String chunk = "";
	
	public boolean safe = false;
	
	private Clans _clans;
	public ClansTerritory(Clans clans)
	{
		_clans = clans;
	}
	
	public ClansTerritory(Clans clans, String owner, String chunk, boolean safe) 
	{
		_clans = clans;
		this.owner = owner;
		this.chunk = chunk;
		this.safe = safe;
	}
	
	public String getOwner()
	{
		ClansClan clan = _clans.CUtil().getClanByClanName(owner);
		
		if (clan == null || !clan.IsAdmin())
			return "Clan " + owner;
		
		return owner;
	}
}
