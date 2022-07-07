package mineplex.game.clans.clans;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.Material;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;
import mineplex.game.clans.clans.tntgenerator.TntGenerator;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.tokens.ClanAllianceToken;
import mineplex.game.clans.core.repository.tokens.ClanMemberToken;
import mineplex.game.clans.core.repository.tokens.ClanTerritoryToken;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.core.repository.tokens.ClanWarToken;
import mineplex.game.clans.core.war.ClanWarData;

public class ClanInfo
{
	private int _id = -1;
	private String _name = "";
	private String _desc = "";
	private Location _home = null;
	private int _energy = 4320;

	// Stats
	private int _kills;
	private int _murder;
	private int _deaths;
	private int _warWins;
	private int _warLosses;
	private int _eloRating;

	private boolean _admin = false;

	private Timestamp _dateCreated;
	private Timestamp _lastOnline = null;

	private TntGenerator _generator;

	// Loaded from Client
	private NautHashMap<UUID, ClansPlayer> _memberMap = new NautHashMap<UUID, ClansPlayer>();
	private NautHashMap<String, Boolean> _allyMap = new NautHashMap<String, Boolean>();
	private Set<ClaimLocation> _claimSet = new HashSet<>();

	// Wars Initiated By Others
	private HashMap<String, ClanWarData> _warIn = new HashMap<String, ClanWarData>();
	// Wars Initiated By Self
	private HashMap<String, ClanWarData> _warOut = new HashMap<String, ClanWarData>();

	// Temporary
	private NautHashMap<String, Long> _recentlyLeft = new NautHashMap<>();
	private NautHashMap<String, Long> _inviteeMap = new NautHashMap<String, Long>();
	private NautHashMap<String, String> _inviterMap = new NautHashMap<String, String>();
	private List<UUID> _onlinePlayers = new ArrayList<UUID>();

	private NautHashMap<String, Long> _requestMap = new NautHashMap<String, Long>();

	public ClansManager Clans;
	
	private BedStatus _bedStatus = null;
	

	public ClanInfo(ClansManager clans, ClanToken token)
	{
		Clans = clans;

		_id = token.Id;
		_name = token.Name;
		_desc = token.Description;
		_eloRating = token.EloRating;

		try
		{
			_home = UtilWorld.strToLoc(token.Home);
		}
		catch (Exception e)
		{

		}
		
		if (_home != null)
		{
			if (UtilBlock.isValidBed(_home))
			{
				if (_home.clone().add(0, 1,0).getBlock().getType().equals(Material.AIR))
				{
					_bedStatus = BedStatus.EXISTS_AND_UNOBSTRUCTED;
				}
				else
				{
					_bedStatus = BedStatus.EXISTS_AND_OBSTRUCTED;
				}
			}
			else
			{
				_bedStatus = BedStatus.DESTROYED;
			}
		}
		else
		{
			_bedStatus = BedStatus.DOESNT_EXIST;
		}

		_energy = token.Energy;
		_admin = token.Admin;

		_dateCreated = (token.DateCreated != null) ? token.DateCreated : new Timestamp(System.currentTimeMillis());
		_lastOnline = (token.LastOnline != null) ? token.LastOnline : new Timestamp(System.currentTimeMillis());
		
		for (ClanMemberToken memberToken : token.Members)
		{
			ClanRole role = ClanRole.valueOf(memberToken.ClanRole);
			_memberMap.put(memberToken.PlayerUUID, new ClansPlayer(memberToken.Name, memberToken.PlayerUUID, role));
		}
		
		for (ClanTerritoryToken territoryToken : token.Territories)
		{
			_claimSet.add(ClaimLocation.fromStoredString(territoryToken.Chunk));
		}
		
		for (ClanAllianceToken allianceToken : token.Alliances)
		{
			_allyMap.put(allianceToken.ClanName, allianceToken.Trusted);
		}

		for (ClanWarToken warToken : token.WarsIn)
		{
			ClanWarData warData = warToken.WarData;
			_warIn.put(warData.getClanA(), warData);
		}

		for (ClanWarToken warToken : token.WarsOut)
		{
			ClanWarData warData = warToken.WarData;
			_warOut.put(warData.getClanB(), warData);
		}
		
		try
		{
			if (token.GeneratorBuyer != null && token.GeneratorBuyer.length() > 0)
			{
				if (token.GeneratorBuyer.contains(","))
				{
					// Convert to new generator format;
					
					for (ClanMemberToken memberToken : token.Members)
					{
						ClanRole role = ClanRole.valueOf(memberToken.ClanRole);
						
						if (role.equals(ClanRole.LEADER))
						{
							token.GeneratorBuyer = memberToken.PlayerUUID.toString();
							break;
						}
					}
					
					System.out.println("Clans> Converted " + _name + "'s generator to the new format.");
				}
				
				_generator = new TntGenerator(token.GeneratorBuyer);
				_generator.setStock(token.GeneratorStock);
				
				Clans.getClanDataAccess().updateGenerator(this, null);
			}
		}
		catch (Exception e)
		{

		}
	}

	public int getClaims()
	{
		return getClaimSet().size();
	}

	public int getClaimsMax()
	{
		if (ssAdmin())
			return 1000;

		return Math.min(8, 2 + getMembers().size());
	}

	public int getAllies()
	{
		return getAllyMap().size();
	}

	public int getAlliesMax()
	{
		return getAlliesMaxWithMemberCountOf(_memberMap.size());
	}
	
	public int getAlliesMaxWithMemberCountOf(int memberCount)
	{
		if (ssAdmin())
		{
			return 1000;
		}
		
		return Math.max(2, 6 - memberCount);
	}
	
	public BedStatus getBedStatus()
	{
		return _bedStatus;
	}
	
	public void setBedStatus(BedStatus bedStatus)
	{
		_bedStatus = bedStatus;
	}

	public boolean isRequested(String clan)
	{
		if (!getRequestMap().containsKey(clan))
			return false;

		if (System.currentTimeMillis() > getRequestMap().get(clan) + (Clans.getInviteExpire() * 60000))
			return false;

		return true;
	}

	public boolean isInvited(String player)
	{
		if (!getInviteeMap().containsKey(player))
			return false;

		if (System.currentTimeMillis() > getInviteeMap().get(player) + (Clans.getInviteExpire() * 60000))
			return false;

		return true;
	}

	public boolean isMember(Player player)
	{
		return getMembers().containsKey(player.getUniqueId());
	}
	
	public boolean isMember(UUID uuid)
	{
		return getMembers().containsKey(uuid);
	}

	public boolean isAlly(String other)
	{
		return getAllyMap().containsKey(other);
	}

	public boolean isAlly(ClanInfo other)
	{
		if (other == null)
		{
			return false;
		}
		
		return isAlly(other.getName());
	}
	
	public boolean isSelf(String other)
	{
		return getName().equals(other);
	}

	public boolean isNeutral(String other)
	{
		return (!isAlly(other) && !isSelf(other));
	}

	public long getTimer()
	{
		int penalty = 0;
		return System.currentTimeMillis() + (penalty * 1000);
	}

	public boolean getTrust(String clan)
	{
		if (!getAllyMap().containsKey(clan))
			return false;

		return getAllyMap().get(clan);
	}
	
	public LinkedList<String> mDetails(Player caller)
	{
		LinkedList<String> stringList = new LinkedList<String>();

		stringList.add(F.main("Clans",	Clans.getClanUtility().mRel(Clans.getClanUtility().relPC(caller, this), getName() + " Information;", true)));
		// stringList.add(F.value("Desc", _desc));

		// Age
		stringList.add(F.value("Age",
				UtilTime.convertString(System.currentTimeMillis() - _dateCreated.getTime(), 1, TimeUnit.FIT)));

		// Home
		if (Clans.getClanUtility().relPC(caller, this) == ClanRelation.SELF)
			stringList.add(F.value("Home", UtilWorld.locToStrClean(getHome())));

		// Land
		stringList.add(F.value("Territory", getClaims() + "/" + getClaimsMax()));
		
		// Member count
		stringList.add(F.value("Members", getSize() + "/" + getMaxSize()));

		// Energy
		int energy = getEnergy();
		int costPerHour = getEnergyCostPerMinute() * 60;
		stringList.add(" ");
		stringList.add(F.value("Clan Energy", "" + energy));
//		stringList.add(F.value("Max Energy", "" + getEnergyMax()));
		stringList.add(F.value("Energy Drain/Hour", "" + costPerHour));
		if (costPerHour > 0)
			stringList.add(F.value("Hours Left", "" +  energy / costPerHour));
		stringList.add(" ");

		// Ally String
		String allySorted = "";
		HashSet<String> allyUnsorted = new HashSet<String>();

		for (String allyName : getAllyMap().keySet())
			allyUnsorted.add(allyName);

		for (String cur : UtilAlg.sortKey(allyUnsorted))
			allySorted += Clans.getClanUtility().mRel(Clans.getClanUtility().relPC(caller, Clans.getClanMap().get(cur)), cur, false)
					+ ", ";

		stringList.add(F.value("Allies", allySorted));

		// Members
		String members = "";
		for (ClansPlayer cur : UtilAlg.sortSet(getMembers().values(), new ClansPlayerComparator()))
		{
			String name = C.listValueOff + cur.getPlayerName();
			if (cur.isOnline())
				name = C.listValueOn + cur.getPlayerName();

			if (cur.getRole() == ClanRole.LEADER)
				members += C.listValue + "L." + name + C.mBody + ", ";

			if (cur.getRole() == ClanRole.ADMIN)
				members += C.listValue + "A." + name + C.mBody + ", ";

			if (cur.getRole() == ClanRole.MEMBER)
				members += C.listValue + "M." + name + C.mBody + ", ";

			if (cur.getRole() == ClanRole.RECRUIT)
				members += C.listValue + "R." + name + C.mBody + ", ";
		}
		stringList.add(F.value("Members", members));

		return stringList;
	}

	public LinkedList<String> mTerritory()
	{
		LinkedList<String> stringList = new LinkedList<String>();

		stringList.add(F.main("Clans", getName() + " Territory;"));

		getClaimSet().stream().map(ClaimLocation::toStoredString).forEach(stringList::add);

		return stringList;
	}

	public void inform(String message, String ignore)
	{
		for (UUID cur : getMembers().keySet())
		{
			Player player = UtilPlayer.searchExact(cur);

			if (player == null)
				continue;

			if (player.getName().equals(ignore))
				continue;

			UtilPlayer.message(player, F.main("Clans", message));
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);
		}
	}
	
	public void inform(String top, String bottom, String ignore)
	{
		for (UUID cur : getMembers().keySet())
		{
			Player player = UtilPlayer.searchExact(cur);

			if (player == null)
				continue;

			if (player.getName().equals(ignore))
				continue;

			UtilTextMiddle.display(top, bottom, 20, 100, 20, player);
		}
	}

	public String getName()
	{
		return _name;
	}

	public String getDesc()
	{
		return _desc;
	}

	public void setDesc(String desc)
	{
		_desc = desc;
	}

	public NautHashMap<UUID, ClansPlayer> getMembers()
	{
		return _memberMap;
	}

	public Set<ClaimLocation> getClaimSet()
	{
		return _claimSet;
	}

	public Location getHome()
	{
		return _home != null ? _home.clone() : null;
	}

	public void setHome(Location loc)
	{
		_home = loc;
	}

	public boolean ssAdmin()
	{
		return _admin;
	}

	public void setAdmin(boolean admin)
	{
		_admin = admin;
	}

	public NautHashMap<String, String> getInviterMap()
	{
		return _inviterMap;
	}

	public NautHashMap<String, Long> getInviteeMap()
	{
		return _inviteeMap;
	}

	public NautHashMap<String, Boolean> getAllyMap()
	{
		return _allyMap;
	}

	public NautHashMap<String, Long> getRequestMap()
	{
		return _requestMap;
	}

	public Timestamp getDateCreated()
	{
		return _dateCreated;
	}

	public Timestamp getLastOnline()
	{
		return _lastOnline;
	}

	public void setLastOnline(Timestamp lastOnline)
	{
		_lastOnline = lastOnline;
	}

	public boolean isOnlineNow()
	{
		return _onlinePlayers.size() > 0;
	}

	public boolean isOnline()
	{
		return isOnlineNow();
	}

	public TntGenerator getGenerator()
	{
		return _generator;
	}

	public void setGenerator(TntGenerator generator)
	{
		_generator = generator;
	}

	public boolean isAdmin()
	{
		return _admin;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public int getEnergy()
	{
		if (_energy > getEnergyMax())
		{
			_energy = getEnergyMax();
		}
		
		return _energy;
	}

	public String getEnergyLeftString()
	{
		if (getEnergyCostPerMinute() > 0)
			return UtilTime.convertString(60000L * getEnergy() / getEnergyCostPerMinute(), 1, TimeUnit.FIT);

		return "Infinite";
	}

	public int getKills()
	{
		return _kills;
	}

	public void addKills(int amount)
	{
		_kills += amount;
	}

	public int getMurder()
	{
		return _murder;
	}

	public void addMurder(int amount)
	{
		_murder += amount;
	}

	public int getDeaths()
	{
		return _deaths;
	}

	public void addDeaths(int amount)
	{
		_deaths += amount;
	}

	public int getWarWins()
	{
		return _warWins;
	}

	public void addWarWins(int amount)
	{
		_warWins += amount;
	}

	public int getWarLosses()
	{
		return _warLosses;
	}

	public void addWarLosses(int amount)
	{
		_warLosses += amount;
	}
	
	public int getEloRating()
	{
		return _eloRating;
	}
	
	public void addEloRating(int amount)
	{
		_eloRating += amount;
	}

	public ClanWarData getWarData(ClanInfo against)
	{
		ClanWarData data = null;

		data = _warIn.get(against.getName());
		if (data == null) data = _warOut.get(against.getName());

		return data;
	}

	public int getWarPoints(ClanInfo against)
	{
		int warPoints = 0;

		if (against != null)
		{
			ClanWarData data = _warIn.get(against.getName());
			if (data != null)
			{
				warPoints = data.getClanBPoints();
			}
			else if ((data = _warOut.get(against.getName())) != null)
			{
				warPoints = data.getClanAPoints();
			}
		}

		return warPoints;
	}

	public String getFormattedWarPoints(ClanInfo clan)
	{
		String warString = "";
		int warPoints = getWarPoints(clan);

		String prefix;
		if (warPoints >= 5) 		prefix = C.cPurple;
		else if (warPoints <= -5)	prefix = C.cRed;
		else 						prefix = C.cWhite;

		warString += prefix;
		if (warPoints > 0) warString += "+";
		warString += warPoints;

		return warString;
	}

	public void addWar(ClanWarData war)
	{
		if (war.getClanA().equals(getName()))
			_warOut.put(war.getClanB(), war);
		else if (war.getClanB().equals(getName()))
			_warIn.put(war.getClanA(), war);
		else throw new RuntimeException("Failed to add war to clan `" + getName() + "` ClanA: " + war.getClanA() + ", ClanB: " + war.getClanB());
	}

	public List<ClanWarData> getWars()
	{
		List<ClanWarData> wars = new LinkedList<ClanWarData>();
		wars.addAll(_warIn.values());
		wars.addAll(_warOut.values());
		return wars;
	}

	public void clearWar(String againstClan)
	{
		_warIn.remove(againstClan);
		_warOut.remove(againstClan);
	}

	public boolean isEnemy(ClanInfo againstClan)
	{
		int warPoints = getWarPoints(againstClan);
		return warPoints >= 5 || warPoints <= -5;
	}

	public void adjustEnergy(int energy)
	{
		_energy += energy;
	}

	public int getClaimCount()
	{
		return _claimSet.size();
	}

	public int getSize()
	{
		return _memberMap.size();
	}
	
	public int getMaxSize()
	{
		if (ssAdmin())
			return 1000;

		return 20;
	}

	public int getEnergyMax()
	{
		// 10080 = 7 days of minutes
		return Math.max(10080, getEnergyCostPerMinute() * 60 * 24 * 7);
	}

	public int getEnergyCostPerMinute()
	{
		return (getSize() * getClaimCount());
	}

	public int getEnergyPurchasableMinutes()
	{
		return getEnergyPurchasable() / getEnergyCostPerMinute();
	}

	public int getEnergyPurchasable()
	{
		return Math.max(getEnergyMax() - getEnergy(), 0);
	}

	public List<Player> getOnlinePlayers()
	{
		ArrayList<Player> players = new ArrayList<Player>(_onlinePlayers.size());
		for (UUID uuid : _onlinePlayers)
		{
			players.add(UtilPlayer.searchExact(uuid));
		}
		return players;
	}

	public Player[] getOnlinePlayersArray()
	{
		Player[] players = new Player[_onlinePlayers.size()];
		for (int i = 0; i < _onlinePlayers.size(); i++)
		{
			players[i] = UtilPlayer.searchExact(_onlinePlayers.get(i));
		}
		return players;
	}

	public int getOnlinePlayerCount()
	{
		return _onlinePlayers.size();
	}

	public void playerOnline(Player player)
	{
		ClansPlayer clansPlayer = _memberMap.get(player.getUniqueId());

		_onlinePlayers.add(player.getUniqueId());
		clansPlayer.setPlayerName(player.getName());
		clansPlayer.setOnline(true);
	}

	public void playerOnline(String player)
	{
		Player p = UtilPlayer.searchExact(player);
		if (p != null) playerOnline(p);
	}

	public void playerOffline(String player)
	{
		Player p = UtilPlayer.searchExact(player);
		if (p != null) playerOffline(p);
	}

	@Override
	public String toString()
	{
		return _id + "," + _name;
	}

	public void playerOffline(Player player)
	{
		ClansPlayer clansPlayer = _memberMap.get(player.getUniqueId());

		if (clansPlayer != null)
		{
			clansPlayer.setOnline(false);
		}

		_onlinePlayers.remove(player.getUniqueId());
	}

	public Set<String> getMemberNameSet()
	{
		HashSet<String> set = new HashSet<String>();

		for (ClansPlayer cp : _memberMap.values())
		{
			set.add(cp.getPlayerName());
		}

		return set;
	}

	public ClansPlayer getClansPlayerFromName(String playerName)
	{
		for (ClansPlayer cp : _memberMap.values())
		{
			if (cp.getPlayerName().equals(playerName))
				return cp;
		}

		return null;
	}

	public String getBedStatusStr()
	{
		return _bedStatus == BedStatus.DESTROYED ? C.cRed + "Destroyed" : (_bedStatus == BedStatus.DOESNT_EXIST ? C.cWhite + "None" : (_bedStatus == BedStatus.EXISTS_AND_OBSTRUCTED ? C.cRed + "Obstructed" : (_bedStatus == BedStatus.EXISTS_AND_UNOBSTRUCTED ? C.cGreen + UtilWorld.locToStrClean(_home) : "N/A")));
	}

	public void left(String name)
	{
		_recentlyLeft.put(name, System.currentTimeMillis());
	}
}
