package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.pvp.modules.clans.ClansUtility.ClanRelation;
import nautilus.game.pvp.modules.clans.Tokens.AllianceToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanMemberToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanRole;
import nautilus.game.pvp.modules.clans.Tokens.ClanTerritoryToken;
import nautilus.game.pvp.modules.clans.Tokens.ClanToken;
import nautilus.game.pvp.modules.clans.Tokens.WarToken;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ClansClan 
{
	public enum Role
	{
		LEADER(4),
		ADMIN(3),
		MEMBER(2),
		RECRUIT(1),
		NONE(0);

		private int i;

		private Role(int value) 
		{
			this.i = value;
		}

		public int toInt()
		{
			return i;
		}
	}

	private ClanToken _token;

	private String _name = "";
	private String _desc = "";
	private Location _home = null;

	private int _power = 0;
	private long _powerTime = 3000000;

	private boolean _admin = false;

	private long _dateCreated = 0;
	private long _lastOnline = 0;

	private Location _generatorBlock = null;
	private long _generatorTime = 0;
	private int _generatorStock = 0;

	private ClansOutpost _outpost = null;

	private NautHashMap<String, Boolean> _allyMap = new NautHashMap<String, Boolean>();

	private NautHashMap<String, ClansWar> _enemyOut = new NautHashMap<String, ClansWar>();
	private NautHashMap<String, ClansWar> _enemyIn = new NautHashMap<String, ClansWar>();

	private NautHashMap<String, Long> _enemyRecharge = new NautHashMap<String, Long>();
	private HashSet<String> _enemyEvent = new HashSet<String>();
	private HashSet<String> _enemyPillage = new HashSet<String>();

	//Loaded from Client
	private NautHashMap<String, Role> _memberMap = new NautHashMap<String, Role>();
	private HashSet<String> _claimSet = new HashSet<String>();

	//Temporary
	private NautHashMap<String, Long> _inviteeMap = new NautHashMap<String, Long>();
	private NautHashMap<String, String> _inviterMap = new NautHashMap<String, String>();

	private NautHashMap<String, Long> _requestMap = new NautHashMap<String, Long>();

	//Clans REfernece
	public Clans Clans;

	public ClansClan(Clans clans, ClanToken token) 
	{
		Clans = clans;
		_token = token;

		_name = token.Name;
		_desc = token.Description;

		try
		{
			_home = UtilWorld.strToLoc(token.Home);
		}
		catch (Exception e)
		{

		}

		_power = token.Power; 
		_admin = token.Admin;

		_dateCreated = token.DateCreated;
		_lastOnline = token.LastTimeOnline;

		if (token.Generator != null)
		{
			if (token.Generator.Location != null && !token.Generator.Location.isEmpty())
				_generatorBlock = UtilWorld.strToLoc(token.Generator.Location);

			_generatorStock = token.Generator.Stock;
			_generatorTime = token.Generator.Time;
		}
	}

	public int getPower()
	{
		if (IsAdmin())
			return 1000;

		return getPowerMax() - _power;
	}

	public int getPowerMax()
	{
		if (IsAdmin())
			return 1000;

		int powerMax = 2 + GetMembers().size();

		if (powerMax > 8)
			powerMax = 8;

		return powerMax;
	}

	public int getClaims()
	{
		return GetClaimSet().size();
	}

	public int getClaimsMax()
	{
		if (IsAdmin())
			return 1000;

		return 2 + GetMembers().size();
	}

	public int getAllies()
	{
		return GetAllyMap().size();
	}

	public int getAlliesMax()
	{
		if (IsAdmin())
			return 1000;

		return Math.max(2, 9 - _memberMap.size());
	}

	public int getWars()
	{
		return GetEnemyOut().size();
	}

	public int getWarsMax()
	{
		return 1 + GetMembers().size();
	}

	public int getEnemies()
	{
		return GetAllyMap().size();
	}

	public boolean isRequested(String clan)
	{
		if (!GetRequestMap().containsKey(clan))
			return false;

		if (System.currentTimeMillis() > GetRequestMap().get(clan) + (Clans.GetInviteExpire() * 60000))
			return false;

		return true;
	}

	public boolean isInvited(String player)
	{
		if (!GetInviteeMap().containsKey(player))
			return false;

		if (System.currentTimeMillis() > GetInviteeMap().get(player) + (Clans.GetInviteExpire() * 60000))
			return false;

		return true;
	}

	public boolean isMember(String clan)
	{
		return GetMembers().containsKey(clan);
	}

	public boolean isAlly(String other)
	{
		return GetAllyMap().containsKey(other);
	}

	public boolean isSelf(String other)
	{
		return this.GetName().equals(other);
	}

	public boolean isNeutral(String other)
	{
		return (!isEnemy(other) && !isAlly(other) && !isSelf(other));
	} 

	public boolean isEnemy(String other)
	{
		return (_enemyOut.containsKey(other) || _enemyIn.containsKey(other));
	}
	
	public boolean isPillage(String other)
	{
		return GetEnemyEvent().contains(other);
	}

	public boolean canPillage(ClansClan other) 
	{
		return _enemyPillage.contains(other.GetName());
	}

	public int getDominance(ClansClan clan)
	{
		if (!GetEnemyOut().containsKey(clan.GetName()))
			return 0;

		ClansWar war = GetEnemyOut().get(clan.GetName());

		return war.GetDominance();
	}

	public String getDominanceString(ClansClan clan)
	{
		if (clan == null)
			return "Unknown";

		//Mutual Invasion
		if (this.GetEnemyOut().containsKey(clan.GetName()) && clan.GetEnemyOut().containsKey(this.GetName()))
		{
			return C.mBody + "(" + C.mElem + 
					C.listValueOn + this.GetEnemyOut().get(clan.GetName()).GetDominance() + 
					C.mBody + ":" +
					C.listValueOff + clan.GetEnemyOut().get(this.GetName()).GetDominance() + 
					C.mBody + ")";
		}

		else if (this.GetEnemyOut().containsKey(clan.GetName()))
		{
			return C.mBody + "(" + C.mElem + 
					C.listValueOn + this.GetEnemyOut().get(clan.GetName()).GetDominance() + 
					C.mBody + ":" +
					C.listValueOff + "-" + 
					C.mBody + ")";
		}

		else if (clan.GetEnemyOut().containsKey(this.GetName()))
		{
			return C.mBody + "(" + C.mElem + 
					C.listValueOn + "-" + 
					C.mBody + ":" +
					C.listValueOff + clan.GetEnemyOut().get(this.GetName()).GetDominance() + 
					C.mBody + ")";
		}

		return "Unknown";	
	}

	public void modifyPower(int mod)
	{
		int newPower = _power - mod;
		setPower(newPower);
	}

	public void setPower(int pow)
	{
		_power = pow;

		//Limits
		if (_power < 0)
			_power = 0;

		if (_power > getPowerMax())
			_power = getPowerMax();

		Clans.CRepo().EditClan(GetToken());
	}


	public void gainDominance(ClansClan other)
	{
		ClansWar war = null;
		
		//Subtract from Enemy
		war = other.GetEnemyOut().get(this.GetName());	
		if (war != null)
		{
			if (war.GetDominance() > 0)
			{
				war.SetDominance(war.GetDominance() - 1);

				//Inform
				other.inform("You lost Dominance against " + 
						Clans.CUtil().mRel(Clans.CUtil().rel(this, other), this.GetName(), false) + " " + other.getDominanceString(this) + ".", null);

				this.inform("You recovered Dominance against " + 
						Clans.CUtil().mRel(Clans.CUtil().rel(this, other), other.GetName(), false) + " " + this.getDominanceString(other) + ".", null);

				return;
			}
		}

		//Add to Self
		war = this.GetEnemyOut().get(other.GetName());	
		if (war != null)
		{
			war.SetDominance(war.GetDominance() + 1);

			//Inform
			other.inform("You lost Dominance against " + 
					Clans.CUtil().mRel(Clans.CUtil().rel(this, other), this.GetName(), false) + " " + other.getDominanceString(this) + ".", null);

			this.inform("You gained Dominance against " + 
					Clans.CUtil().mRel(Clans.CUtil().rel(this, other), other.GetName(), false) + " " + this.getDominanceString(other) + ".", null);
		}
	}

	public long getTimer()
	{
		int penalty = 0;

		/* XXX
		for (int cur : GetWarMap().values())
		{

			if (cur < 0)
				penalty -= cur*2;

			else
				penalty += cur;
		}*/

		return System.currentTimeMillis() + (penalty * 1000);
	}

	public boolean getTrust(String clan)
	{
		if (!GetAllyMap().containsKey(clan))
			return false;

		return GetAllyMap().get(clan);
	}

	public LinkedList<String> mDetails(String caller) 
	{
		LinkedList<String> stringList = new LinkedList<String>();

		stringList.add(F.main("Clans", Clans.CUtil().mRel(Clans.CUtil().relPC(caller, this), GetName() + " Information;", true)));
		//stringList.add(F.value("Desc", _desc));



		if (Clans.IsPowerEnabled())
		{
			stringList.add(F.value("Power", getPower() + "/" + getPowerMax()));
			if (_power > 0)
				stringList.add(F.value("Power Gain", 
						UtilTime.convertString(Clans.GetPowerTime()-GetPowerTime(), 1, TimeUnit.FIT)));
			else
				stringList.add(F.value("Power Gain", "Full Power"));
		}

		//Age
		stringList.add(F.value("Age", UtilTime.convertString(System.currentTimeMillis() - _dateCreated, 1, TimeUnit.FIT)));

		//Home
		if (Clans.CUtil().relPC(caller, this) == ClanRelation.SELF)
			stringList.add(F.value("Home", UtilWorld.locToStrClean(GetHome())));

		//Land
		stringList.add(F.value("Territory", getClaims() + "/" + getClaimsMax()));

		//Ally String
		String allySorted = "";
		HashSet<String> allyUnsorted = new HashSet<String>();

		for (String allyName : GetAllyMap().keySet())
			allyUnsorted.add(allyName);

		for (String cur : UtilAlg.sortKey(allyUnsorted))
			allySorted += Clans.CUtil().mRel(Clans.CUtil().relPC(caller, Clans.GetClanMap().get(cur)), cur, false) + ", ";

		stringList.add(F.value("Allies", allySorted));

		//Wars Out String
		String warsSorted = "";
		HashSet<String> warsUnsorted = new HashSet<String>();

		for (String warName : GetEnemyOut().keySet())
			warsUnsorted.add(warName);

		for (String cur : UtilAlg.sortKey(warsUnsorted))
			warsSorted += Clans.CUtil().mRel(Clans.CUtil().relPC(caller, Clans.GetClanMap().get(cur)), cur, false) + ", ";

		stringList.add(F.value("Invading", warsSorted));

		//Wars In String
		warsSorted = "";
		warsUnsorted = new HashSet<String>();

		for (String warName : GetEnemyIn().keySet())
			warsUnsorted.add(warName);

		for (String cur : UtilAlg.sortKey(warsUnsorted))
			warsSorted += Clans.CUtil().mRel(Clans.CUtil().relPC(caller, Clans.GetClanMap().get(cur)), cur, false) + ", ";

		stringList.add(F.value("Defending", warsSorted));

		//Members
		String members = "";
		for (String cur : UtilAlg.sortKey(GetMembers().keySet()))
		{
			String name = C.listValueOff + cur;
			if (UtilPlayer.isOnline(cur))
				name = C.listValueOn + cur;

			if (GetMembers().get(cur) == Role.LEADER)
				members += C.listValue + "L." + name + C.mBody + ", ";

			if (GetMembers().get(cur) == Role.ADMIN)
				members += C.listValue + "A." + name + C.mBody + ", ";

			if (GetMembers().get(cur) == Role.MEMBER)
				members += C.listValue + "M." + name + C.mBody + ", ";

			if (GetMembers().get(cur) == Role.RECRUIT)
				members += C.listValue + "R." + name + C.mBody + ", ";
		}
		stringList.add(F.value("Members", members));

		if (_generatorBlock == null)
		{
			stringList.add(F.value("TNT Generator", C.cRed + "None"));
		}
		else
		{
			stringList.add(F.value("TNT Generator", _generatorStock + " TNT"));
			stringList.add(F.value("TNT Generation", 
					UtilTime.convertString(Clans.GetGeneratorTime() - (System.currentTimeMillis() - _generatorTime) , 1, TimeUnit.FIT)));
		}

		//Protected
		stringList.add(F.value("TNT Protection", getProtected()));

		//Dominance
		ClansClan callerClan = Clans.CUtil().getClanByPlayer(caller);
		if (callerClan != null)
			if (this.isEnemy(callerClan.GetName()))
				stringList.add(F.value("Dominance", callerClan.getDominanceString(this)));

		return stringList;
	}

	public LinkedList<String> mTerritory()
	{
		LinkedList<String> stringList = new LinkedList<String>();

		stringList.add(F.main("Clans", GetName() + " Territory;"));

		for (String cur : GetClaimSet())
		{
			stringList.add(cur);
		}

		return stringList;
	}

	public void inform(String message, String ignore)
	{
		for (String cur : GetMembers().keySet())
		{
			if (ignore != null && cur.equals(ignore))
				continue;

			Player player = UtilPlayer.searchOnline(null, cur, false);

			if (player == null)
				continue;

			UtilPlayer.message(player, F.main("Clans", message));
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);
		}
	}

	public void chat(Player sender, String message, String filteredMessage)
	{
		for (String cur : GetMembers().keySet())
		{
			Player player = UtilPlayer.searchOnline(null, cur, false);

			if (player == null)
				continue;

			CoreClient client = Clans.Clients().Get(player);

			if (client.Game().GetFilterChat())
				UtilPlayer.message(player, C.cAqua + sender.getName() + C.cDAqua + " " + filteredMessage);
			else
				UtilPlayer.message(player, C.cAqua + sender.getName() + C.cDAqua + " " + message);
		}
	}
	
	public void allyChat(ClansClan senderClan, Player sender, String message, String filteredMessage)
	{
		for (String cur : GetMembers().keySet())
		{
			Player player = UtilPlayer.searchOnline(null, cur, false);

			if (player == null)
				continue;

			CoreClient client = Clans.Clients().Get(player);

			//C.cDGreen + senderClan.GetName() + " " + 
			
			if (client.Game().GetFilterChat())
				UtilPlayer.message(player, C.cDGreen + sender.getName() + C.cGreen + " " + filteredMessage);
			else
				UtilPlayer.message(player, C.cDGreen + sender.getName() + C.cGreen + " " + message);
		}
	}

	public long GetPowerTime() 
	{
		return _powerTime;
	}

	public void SetPowerTime(long powerTime) 
	{
		_powerTime = powerTime;
	}

	public String GetName() 
	{
		return _name;
	}

	public String GetDesc() 
	{
		return _desc;
	}

	public void SetDesc(String desc)
	{
		_desc = desc;
		_token.Description = desc;
	}

	public NautHashMap<String, Role> GetMembers() 
	{
		return _memberMap;
	}

	public HashSet<String> GetClaimSet() 
	{
		return _claimSet;
	}

	public Location GetHome() 
	{
		return _home;
	}

	public void SetHome(Location loc) 
	{
		_home = loc;
		_token.Home = UtilWorld.locToStr(loc);
	}

	public boolean IsAdmin() 
	{
		return _admin;
	}

	public void SetAdmin(boolean admin) 
	{
		_admin = admin;
		_token.Admin = admin;
	}

	public NautHashMap<String, String> GetInviterMap() 
	{
		return _inviterMap;
	}

	public NautHashMap<String, Long> GetInviteeMap() 
	{
		return _inviteeMap;
	}

	public NautHashMap<String, ClansWar> GetEnemyOut() 
	{
		return _enemyOut;
	}

	public NautHashMap<String, ClansWar> GetEnemyIn() 
	{
		return _enemyIn;
	}

	public NautHashMap<String, Long> GetEnemyRecharge() 
	{
		return _enemyRecharge;
	}

	public NautHashMap<String, Long> GetEnemyAccess() 
	{
		return _enemyRecharge;
	}
	
	public HashSet<String> GetEnemyEvent() 
	{
		return _enemyEvent;
	}

	public NautHashMap<String, Boolean> GetAllyMap() 
	{
		return _allyMap;
	}

	public NautHashMap<String, Long> GetRequestMap() 
	{
		return _requestMap;
	}

	public long GetDateCreated() 
	{
		return _dateCreated;
	}

	public long GetLastOnline() 
	{
		return _lastOnline;
	}

	public void SetLastOnline(long lastOnline) 
	{
		_lastOnline = lastOnline;
		_token.LastTimeOnline = lastOnline;
	}

	public ClanToken GetTokenUnupdated()
	{
		return _token;
	}

	public ClanToken GetToken() 
	{
		//Update Members
		_token.Members = new ArrayList<ClanMemberToken>();
		for (String name : GetMembers().keySet())
		{
			ClanMemberToken token = new ClanMemberToken();
			token.Name = name;
			token.ClanRole = new ClanRole();
			token.ClanRole.Name = GetMembers().get(name).toString();

			_token.Members.add(token);
		}

		//Update Territory
		_token.Territories = new ArrayList<ClanTerritoryToken>();
		for (String chunk : GetClaimSet())
		{ 
			ClanTerritoryToken token = new ClanTerritoryToken();
			token.ClanName = _token.Name;
			token.ClanId = _token.ClanId;
			token.ServerName = Clans.GetServerName();
			token.Chunk = chunk;

			if (Clans.GetClaimMap().get(chunk) != null)
				token.Safe = Clans.GetClaimMap().get(chunk).safe;

			_token.Territories.add(token);
		}

		//Update Relations
		_token.Alliances = new ArrayList<AllianceToken>();
		for (String clanName : GetAllyMap().keySet())
		{
			ClansClan clan = Clans.getClan(clanName);
			AllianceToken token = new AllianceToken();
			token.ClanId = clan.GetTokenUnupdated().ClanId;
			token.ClanName = clan.GetTokenUnupdated().Name;

			if (GetAllyMap().get(clanName))
				token.Trusted = true;

			_token.Alliances.add(token);
		}

		//Wars
		_token.Wars = new ArrayList<WarToken>();
		for (ClansWar war : GetEnemyOut().values())
		{
			if (!war.GetClanA().equals(this))
				continue;

			WarToken token = new WarToken();
			token.ClanId = war.GetClanB().GetTokenUnupdated().ClanId;
			token.ClanName = war.GetClanB().GetTokenUnupdated().Name;
			token.Dominance = war.GetDominance();
			token.Created = war.GetCreated();

			token.Ended = false;
			token.Cooldown = 0;

			_token.Wars.add(token);
		}

		//Recharges
		for (String  other : GetEnemyRecharge().keySet())
		{
			ClansClan clan = Clans.getClan(other);

			WarToken token = new WarToken();
			token.ClanId = clan.GetTokenUnupdated().ClanId;
			token.ClanName = clan.GetTokenUnupdated().Name;
			token.Dominance = -1;
			token.Created = -1;

			token.Ended = true;
			token.Cooldown = GetEnemyRecharge().get(other);

			_token.Wars.add(token);
		}

		return _token;
	}

	public boolean isOnlineNow() 
	{
		for (String cur : GetMembers().keySet())
			if (UtilPlayer.isOnline(cur))
				return true;

		return false;
	}

	public boolean isOnline() 
	{
		for (String cur : GetMembers().keySet())
			if (UtilPlayer.isOnline(cur))
				return true;

		return System.currentTimeMillis() - _lastOnline < Clans.GetOnlineTime();
	}

	public String getProtected()
	{
		for (String cur : GetMembers().keySet())
			if (UtilPlayer.isOnline(cur))
				return C.cRed + "No - Clan Members are Online";

		if (System.currentTimeMillis() - _lastOnline > Clans.GetOnlineTime())
			return C.cGreen + "Yes - Clan Members are Offline";

		return C.cGold + "No, " + 
		UtilTime.convertString(Clans.GetOnlineTime() - (System.currentTimeMillis() - _lastOnline), 1, TimeUnit.FIT) + 
		" to Protection";
	}

	public boolean GeneratorUpdate()
	{
		if (_generatorBlock == null)
			return false;

		if (_generatorBlock.getBlock().getType() != Material.BREWING_STAND)
		{
			GeneratorDestroy();
			return false;
		}

		ClansClan owner = Clans.CUtil().getOwner(_generatorBlock);
		if (owner == null || !owner.equals(this))
		{
			GeneratorDestroy();
			return false;
		}

		if (_generatorStock == 4)
			return false;

		if (!UtilTime.elapsed(_generatorTime, Clans.GetGeneratorTime()))
			return false;

		_generatorTime = System.currentTimeMillis();
		_generatorStock += 1;

		//Inform
		inform("Your " + F.item("TNT Generator") + " has " + F.item(_generatorStock + " TNT") + ".", null);
		return true;
	}

	public void GeneratorUse(Player player, Location loc)
	{
		if (_generatorBlock == null)
			return;

		if (!_generatorBlock.equals(loc))
			return;
				
		if (_generatorStock > 0)
		{		
			_generatorStock -= 1;

			Clans.CRepo().Repository.UpdateClanTNTGenerator(_token.Name, _generatorBlock, _generatorStock, _generatorTime);

			//Drop
			_generatorBlock.getWorld().dropItemNaturally(
					_generatorBlock.getBlock().getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.TNT));

			//Inform
			inform(F.name(player.getName()) + " withdrew from " + F.item("TNT Generator") + ".", null);
		}
		else
		{
			UtilPlayer.message(player, F.main("Clans", "The " + F.item("TNT Generator") + " is empty."));
		}
	}

	public boolean GeneratorPlace(Player player, Location loc)
	{
		if (_generatorBlock != null)
		{
			UtilPlayer.message(player, F.main("Clans", "You already have a " + F.item("TNT Generator") + "."));
			return false;
		}

		ClansClan owner = Clans.CUtil().getOwner(loc);
		if (owner == null || !owner.equals(this))
		{
			UtilPlayer.message(player, F.main("Clans", "You must place " + F.item("TNT Generator") + " in your Territory."));
			return false;
		}

		//Inform
		inform(F.name(player.getName()) + " created a " + F.item("TNT Generator") + ".", null);

		_generatorBlock = loc;
		_generatorStock = 0;
		_generatorTime = System.currentTimeMillis();

		Clans.CRepo().Repository.UpdateClanTNTGenerator(owner.GetName(), _generatorBlock, _generatorStock, _generatorTime);

		return true;
	}

	public void GeneratorBreak(Location loc)
	{
		if (_generatorBlock == null)
			return;

		if (!_generatorBlock.equals(loc))
			return;

		GeneratorDestroy();
	}

	public void GeneratorDestroy()
	{
		//Drop
		for (int i=0 ; i< _generatorStock ; i++)
			_generatorBlock.getWorld().dropItemNaturally(
					_generatorBlock.getBlock().getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.TNT));

		//Inform
		inform("Your " + F.item("TNT Generator") + " has been destroyed!", null);

		//Save
		_generatorTime = System.currentTimeMillis();
		_generatorStock = 0;
		_generatorBlock = null;

		Clans.CRepo().Repository.UpdateClanTNTGenerator(GetName(), _generatorBlock, _generatorStock, _generatorTime);
	}

	public Location GetGeneratorBlock()
	{
		return _generatorBlock;
	}

	public int GetGeneratorStock()
	{
		return _generatorStock;
	}

	public long GetGeneratorTime()
	{
		return _generatorTime;
	}

	public HashSet<String> GetPillage() 
	{
		return _enemyPillage;
	}

	public boolean OutpostPlace(Player player, Location loc)
	{
		if (loc.getBlockY() < 10)
		{
			UtilPlayer.message(player, F.main("Clans", "You cannot place " + F.item("Clan Outpost") + " this deep."));
			return false;
		}

		if (
				Clans.CUtil().isClaimed(loc.getBlock().getRelative( 5, 0, 0).getLocation()) ||
				Clans.CUtil().isClaimed(loc.getBlock().getRelative(-5, 0, 0).getLocation()) ||
				Clans.CUtil().isClaimed(loc.getBlock().getRelative(0, 0,  5).getLocation()) ||
				Clans.CUtil().isClaimed(loc.getBlock().getRelative(0, 0, -5).getLocation()))
		{
			UtilPlayer.message(player, F.main("Clans", "You cannot place " + F.item("Clan Outpost") + " near claimed Territory."));
			return false;
		}

		for (ClansOutpost outpost : Clans.GetOutpostMap().values())
		{
			if (UtilMath.offset(loc, outpost.GetLocation()) < 10)
			{
				UtilPlayer.message(player, F.main("Clans", "You cannot place " + F.item("Clan Outpost") + " near other Outposts."));
				return false;
			}
		}

		if (_outpost != null)
		{
			UtilPlayer.message(player, F.main("Clans", "You already have a " + F.item("Clan Outpost") + "."));
			return false;
		}

		ClansClan owner = Clans.CUtil().getOwner(loc);
		if (owner != null)
		{
			UtilPlayer.message(player, F.main("Clans", "You must place " + F.item("Clan Outpost") + " in Wilderness."));
			return false;
		}

		//Inform
		inform(F.name(player.getName()) + " constructed a " + F.item("Clan Outpost") + ".", null);

		_outpost = new ClansOutpost(this, loc, System.currentTimeMillis());

		//XXX WRITE

		return true;
	}

	public void OutpostBreak(Location loc)
	{
		if (_outpost == null)
			return;

		if (!_outpost.GetLocation().equals(loc))
			return;

		OutpostDestroy();
	}

	public void OutpostDestroy()
	{
		//Inform
		inform("Your " + F.item("Clan Outpost") + " has been destroyed!", null);

		_outpost.GetLocation().getWorld().createExplosion(_outpost.GetLocation(), 4f);

		_outpost.Clean();

		Clans.GetOutpostMap().remove(_outpost);
		_outpost = null;

		//XXX WRITE
	}

	public void OutpostUse(Player player, Location loc)
	{
		if (_outpost == null)
			return;

		if (!_outpost.GetLocation().equals(loc))
			return;

		//Inform
		UtilPlayer.message(player, F.main("Clans", F.item("Clan Outpost") + " deconstucts in " + 
				F.time(UtilTime.convertString(Clans.GetOutpostTime() - (System.currentTimeMillis() - _outpost.GetCreated()), 1, TimeUnit.FIT)) + "."));	
	}

	public void OutpostUpdate()
	{
		if (_outpost == null)
			return;

		if (_outpost.GetLocation().getBlock().getType() != Material.BEACON)
		{
			OutpostDestroy();
			return;
		}

		if (!UtilTime.elapsed(_outpost.GetCreated(), Clans.GetOutpostTime()))
			return;

		OutpostDestroy();
	}

	public ClansOutpost GetOutpost() 
	{
		return _outpost;
	}

	
}
