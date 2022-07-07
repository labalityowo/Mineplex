package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;

import mineplex.core.account.CoreClient;
import mineplex.core.common.Rank;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import nautilus.game.pvp.modules.clans.ClansClan.Role;

import org.bukkit.entity.Player;

public class ClansAdmin 
{
	private Clans Clans;
	
	public ClansAdmin(Clans clans)
	{
		Clans = clans;
	}

	public void command(Player caller, String[] args) 
	{
		if (args.length == 1)
			help(caller);
		
		else if (args[1].equalsIgnoreCase("help") || args[1].equalsIgnoreCase("h"))
			help(caller);

		else if (!Clans.Clients().Get(caller).Rank().Has(Rank.ADMIN, true))
			return;

		else if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("mimic"))
			setMimic(caller, args);

		else if (args[1].equalsIgnoreCase("create"))
			create(caller, args);

		else if (args[1].equalsIgnoreCase("disband") || args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("d"))
			delete(caller, args);

		else if (args[1].equalsIgnoreCase("invite") || args[1].equalsIgnoreCase("i"))
			invite(caller, args);

		else if (args[1].equalsIgnoreCase("promote"))
			promote(caller, args);
		
		else if (args[1].equalsIgnoreCase("demote"))
			demote(caller, args);
		
		else if (args[1].equalsIgnoreCase("kick") || args[1].equalsIgnoreCase("k"))
			kick(caller, args);

		else if (args[1].equalsIgnoreCase("ally") || args[1].equalsIgnoreCase("a"))
			ally(caller, args);

		else if (args[1].equalsIgnoreCase("trust"))
			trust(caller, args);

		else if (args[0].equalsIgnoreCase("neutral") || args[0].equalsIgnoreCase("neut") || args[0].equalsIgnoreCase("n"))
			neutral(caller, args);
		
		else if (args[0].equalsIgnoreCase("enemy") || args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("war") || args[0].equalsIgnoreCase("w"))
			enemy(caller, args);

		else if (args[1].equalsIgnoreCase("claim") || args[1].equalsIgnoreCase("c"))
			claim(caller);

		else if (args[1].equalsIgnoreCase("unclaim") || args[1].equalsIgnoreCase("uc"))
			unclaim(caller, args);

		else if (args[1].equalsIgnoreCase("home") || args[1].equalsIgnoreCase("h"))
			home(caller, args);
		
		else if (args[1].equalsIgnoreCase("safe"))
			safe(caller);
		
		else if (args[1].equalsIgnoreCase("autoclaim"))
			autoclaim(caller);

		else
			help(caller);
	}

	private void help(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Clans Admin", "Admin Commands List;"));

		UtilPlayer.message(caller, F.help("/c x create <clan>", "Create Admin Clan", Rank.ADMIN));
		
		UtilPlayer.message(caller, F.help("/c x set <clan>", "Set Mimic Clan", Rank.ALL));

		UtilPlayer.message(caller, F.help("/c x home (set)", "Teleport to Mimic Home", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x invite <player>", "Invite Player to Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x promote <player>", "Promote Player in Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x demote <player>", "Demote Player in Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x kick <player>", "Kick Player from Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x ally <clan>", "Send Alliance to Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x trust <clan>", "Give Trust to Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x neutral <clan>", "Set Neutrality", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x enemy <clan>", "Start Invasion", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x claim", "Claim Territory for Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x unclaim (all)", "Unclaim Territory for Mimic", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x delete", "Delete Mimic Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c x autoclaim", "AutoClaim for Mimic Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.main("Mimic Clan", Clans.Clients().Get(caller).Clan().GetMimic()));
	}
	
	private void autoclaim(Player caller) 
	{
		Clans.Clients().Get(caller).Clan().SetAutoClaim(!Clans.Clients().Get(caller).Clan().IsAutoClaim());
		
		UtilPlayer.message(caller, F.main("Clans Admin", F.oo("Auto Claim", Clans.Clients().Get(caller).Clan().IsAutoClaim())));
	}

	public void setMimic(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			if (Clans.Clients().Get(caller).Clan().GetMimic().length() > 0)
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "You are no longer mimicing " + F.elem("Clan " + Clans.Clients().Get(caller).Clan().GetMimic()) + "."));
				Clans.Clients().Get(caller).Clan().SetMimic("");
			}
			else
				UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan/Player."));
			
			return;
		}

		ClansClan clan = Clans.CUtil().searchClanPlayer(caller, args[2], true);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid Clan/Player."));
			return;
		}

		//Set Mimic
		Clans.Clients().Get(caller).Clan().SetMimic(clan.GetName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You are mimicing " + F.elem("Clan " + clan.GetName()) + "."));
	}

	public ClansClan getMimic(Player caller, boolean inform)
	{
		String mimic = Clans.Clients().Get(caller).Clan().GetMimic();
		
		if (mimic.length() == 0)
			return null;
		
		ClansClan clan = Clans.CUtil().searchClanPlayer(caller, mimic, true);

		if (clan == null)
		{
			if (inform)
				UtilPlayer.message(caller, F.main("Clans Admin", "You are not mimicing a Clan."));

			return null;	
		}

		return clan;	
	}

	public void create(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan name."));
			return;
		}

		if (!Clans.Util().Input().valid(args[2]))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid characters in Clan name."));
			return;
		}

		if (args[2].length() < Clans.GetNameMin())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Clan name too short. Minimum length is " + (Clans.GetNameMin()) + "."));
			return;
		}

		if (args[2].length() > Clans.GetNameMax())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Clan name too long. Maximum length is + " + (Clans.GetNameMax()) + "."));
			return;
		}

		for (String cur : Clans.CCommand().denyClan)
		{
			if (cur.equalsIgnoreCase(args[2]))
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "Clan name cannot be a Clan command."));
				return;
			}
		}

		for (String cur : Clans.Clients().GetAll())
		{
			if (cur.equalsIgnoreCase(args[2]))
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "Clan name cannot be a Player name."));
				return;
			}
		}

		if (Clans.getClan(args[2]) != null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", F.elem("Clan " + args[2]) + " already exists."));
			return;
		}

		//Inform
		UtilServer.broadcast(F.main("Clans Admin", caller.getName() + " formed " + F.elem("Admin Clan " + args[2]) + "."));

		// Create and Join
		Clans.CTask().create(caller.getName(), args[2], true);

		// Set Mimic
		Clans.Clients().Get(caller).Clan().SetMimic(args[2]);

		// Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You are mimicing Clan " + args[2] + "."));
	}

	public void delete(Player caller, String[] args)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		//Task
		Clans.CTask().delete(clan);

		//Inform
		UtilServer.broadcast(F.main("Clans Admin", caller.getName() + " disbanded " + F.elem("Clan " + clan.GetName()) + "."));
	}

	public void invite(Player caller, String[] args)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input an invitee."));
			return;
		}

		Player target = UtilPlayer.searchOnline(caller, args[2], true);
		if (target == null)
			return;

		if (target.getName().equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot invite yourself."));
			return;
		}

		//Inform
		clan.inform(caller.getName() + " invited " + target.getName() + " to join Clan " + clan.GetName() + ".", caller.getName());
		UtilPlayer.message(caller, F.main("Clans Admin", "You invited " + target.getName() + " to join " + F.elem("Clan " + clan.GetName()) + "."));
		UtilPlayer.message(target, F.main("Clans Admin", caller.getName() + " invited you to join " + F.elem("Clan " + clan.GetName()) + "."));

		//Task
		Clans.CTask().invite(clan, target.getName(), caller.getName());
	}
	
	public void promote(Player caller, String[] args)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input player to promote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[2], clan.GetMembers().keySet(), "Clan Member", true);

		if (target == null)
			return;

		if (clan.GetMembers().get(target) == Role.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot promote " + F.name(target) + " any further."));
			return;
		}
		
		//Task
		String newRank = "?";
		if (clan.GetMembers().get(target) == Role.RECRUIT)
		{
			Clans.CTask().role(clan, target, Role.MEMBER);
			newRank = "Member";
		}
		else if (clan.GetMembers().get(target) == Role.MEMBER)
		{
			Clans.CTask().role(clan, target, Role.ADMIN);
			newRank = "Admin";
		}
		else if (clan.GetMembers().get(target) == Role.ADMIN)
		{
			Clans.CTask().role(clan, target, Role.LEADER);
			newRank = "Leader";
		}

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You promoted " + target + " to " + newRank + " in Mimic Clan."));
		clan.inform(caller.getName() + " promoted " + target + " to " + newRank + ".", null);
	}
	
	public void demote(Player caller, String[] args)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input player to demote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[2], clan.GetMembers().keySet(), "Clan Member", true);
		if (target == null)
			return;

		if (clan.GetMembers().get(target) == Role.RECRUIT)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot demote " + F.name(target) + " any further."));
			return;
		}
		
		//Task
		String newRank = "?";
		if (clan.GetMembers().get(target) == Role.MEMBER)
		{
			Clans.CTask().role(clan, target, Role.RECRUIT);
			newRank = "Recruit";
		}
		else if (clan.GetMembers().get(target) == Role.ADMIN)
		{
			Clans.CTask().role(clan, target, Role.MEMBER);
			newRank = "Member";
		}
		else if (clan.GetMembers().get(target) == Role.LEADER)
		{
			Clans.CTask().role(clan, target, Role.ADMIN);
			newRank = "Admin";
		}

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You demoted " + target + " to " + newRank + " in Mimic Clan."));
		clan.inform(F.main("Clans Admin", caller.getName() + " demoted " + target + " to " + newRank + "."), null);
	}

	public void kick(Player caller, String[] args)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;		

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Player to kick."));
			return;
		}

		String targetName = UtilPlayer.searchCollection(caller, args[2], clan.GetMembers().keySet(), "Clan Member", true);

		if (targetName == null)
			return;

		CoreClient target = Clans.Clients().Get(targetName);

		//Task
		Clans.CTask().leave(clan, target.GetPlayerName());

		//Inform
		UtilPlayer.message(UtilPlayer.searchOnline(null, target.GetPlayerName(), false), F.main("Clans Admin", caller.getName() + " kicked you from " + F.elem("Clan " + clan.GetName()) + "."));
		UtilPlayer.message(caller, F.main("Clans Admin", "You kicked " + target.GetPlayerName() + " from your Clan."));
		clan.inform(F.main("Clans Admin", caller.getName() + " kicked " + target.GetPlayerName() + " from your Clan."), caller.getName());
	}

	public void ally(Player caller, String[] args)
	{
		ClansClan cA = getMimic(caller, true);

		if (cA == null)
			return;			

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan to ally."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[2], true);

		if (cB == null)
			return;

		if (cA.isSelf(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot ally with yourself."));
			return;
		}

		if (cA.isAlly(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You are already allies with " + F.elem("Clan " + cB.GetName()) + "."));
			return;
		}

		if (cB.isRequested(cA.GetName()))
		{
			//Task
			Clans.CTask().ally(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You accepted alliance with Clan " + cB.GetName() + "."));
			cA.inform(caller.getName() + " accepted alliance with Clan " + cB.GetName() + ".", caller.getName());
			cB.inform("Clan " + cA.GetName() + " has accepted alliance with you.", null);	
		}
		else
		{
			//Task 
			Clans.CTask().requestAlly(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You requested alliance with Clan " + cB.GetName() + "."));
			cA.inform(caller.getName() + " has requested alliance with Clan " + cB.GetName() + ".", caller.getName());
			cB.inform("Clan " + cA.GetName() + " has requested alliance with you.", null);	
		}
	}

	public void trust(Player caller, String[] args)
	{
		ClansClan cA = getMimic(caller, true);

		if (cA == null)
			return;		

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan to enemy."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[2], true);

		if (cB == null)
			return;

		if (!cA.isAlly(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot give trust to enemies."));
			return;
		}

		//Task
		if (Clans.CTask().trust(cA, cB, caller.getName()))
		{
			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You gave trust to Clan " + cB.GetName() + "."));
			cA.inform(caller.getName() + " has given trust to Clan " + cB.GetName() + ".", caller.getName());
			cB.inform("Clan " + cA.GetName() + " has given trust to you.", null);	
		}
		else
		{
			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You revoked trust to Clan " + cB.GetName() + "."));
			cA.inform(caller.getName() + " has revoked trust to Clan " + cB.GetName() + ".", caller.getName());
			cB.inform("Clan " + cA.GetName() + " has revoked trust to you.", null);	
		}
	}

	public void neutral(Player caller, String[] args)
	{
		ClansClan cA = getMimic(caller, true);

		if (cA == null)
			return;		

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to set neutrality with."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[1], true);

		if (cB == null)
			return;

		if (cB.isSelf(cA.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You prefer to think of yourself positively..."));
			return;
		}
		
		if (cB.isNeutral(cA.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already neutral with " + F.elem("Clan " + cB.GetName()) + "."));
			return;
		}

		if (cB.isAlly(cA.GetName()))
		{
			//Task
			Clans.CTask().neutral(cA, cB, caller.getName(), true);

			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You revoked alliance with " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " revoked alliance with " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has revoked alliance with you.", null);
			
			return;
		}
		
		if (cA.GetEnemyOut().containsKey(cB.GetName()))
		{	
			//Task
			Clans.CTask().neutral(cA, cB, caller.getName(), false);

			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You ended invasion on " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " ended invasion on " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has ended invasion on you.", null);
			
			return;
		}
	}
	
	public void enemy(Player caller, String[] args)
	{
		ClansClan cA = getMimic(caller, true);

		if (cA == null)
			return;		

		if (cA.getWars() >= cA.getWarsMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invade more Clans."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to invade."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[1], true);

		if (cB == null)
			return;
		
		if (cA.isSelf(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invade yourself."));
			return;
		}
		
		if (cA.GetEnemyOut().containsKey(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already invading " + F.elem("Clan " + cB.GetName()) + "."));
			return;
		}

		if (cA.isAlly(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You must end alliance with " + F.elem("Clan " + cB.GetName()) + " first."));
			return;
		}
		
		ClansWar war = new ClansWar(Clans, cA, cB, 0, System.currentTimeMillis());
		
		//Task
		Clans.CTask().enemy(war, caller.getName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You started invasion on " + F.elem("Clan " + cB.GetName()) + "."));
		cA.inform(F.name(caller.getName()) + " started invasion on " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
		cB.inform(F.elem("Clan " + cA.GetName()) + " has started an invasion on you.", null);
	}

	public void claim(Player caller)
	{
		ClansClan clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;	

		if (clientClan.getClaims() >= clientClan.getClaimsMax())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Your Clan cannot claim more Territory."));
			return;
		}

		String chunk = UtilWorld.chunkToStr(caller.getLocation().getChunk());
		ClansClan ownerClan = Clans.CUtil().getOwner(caller.getLocation());

		//Already Claimed
		if (ownerClan != null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "This Territory is claimed by " + 
					Clans.CUtil().mRel(Clans.CUtil().relPC(caller.getName(), ownerClan), ownerClan.GetName(), true) + "."));
			return;
		}

		//Task
		Clans.CTask().claim(clientClan.GetName(), chunk, caller.getName(), false);

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clientClan.inform(caller.getName() + " claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public void unclaim(Player caller, String args[])
	{
		if (args.length > 2)
		{
			if (args[2].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("a"))
			{
				unclaimall(caller);
				return;
			}
		}

		ClansClan clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;	

		String chunk = UtilWorld.chunkToStr(caller.getLocation().getChunk());
		ClansClan ownerClan = Clans.CUtil().getOwner(caller.getLocation());

		//Not Claimed
		if (ownerClan == null)
		{		
			UtilPlayer.message(caller, F.main("Clans Admin", "Territory is not claimed."));
			return;
		}

		//Task
		Clans.CTask().unclaim(chunk, caller.getName(), true);

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		ownerClan.inform(caller.getName() + " unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public void unclaimall(Player caller)
	{
		ClansClan clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;	

		//Unclaim
		ArrayList<String> toUnclaim = new ArrayList<String>();

		for (String chunk : clientClan.GetClaimSet())
			toUnclaim.add(chunk);

		for (String chunk : toUnclaim)
			Clans.CTask().unclaim(chunk, caller.getName(), true);

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You unclaimed all your Clans Territory."));
		clientClan.inform(caller.getName() + " unclaimed all your Clans Territory.", caller.getName());
	}

	public void home(Player caller, String[] args)
	{
		if (args.length > 2)
		{
			if (args[2].equalsIgnoreCase("set") || args[2].equalsIgnoreCase("s"))	
			{
				homeSet(caller);
				return;
			}
		}

		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (clan.GetHome() == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Your Clan has not set a Home."));
			return;
		}

		if (!clan.GetClaimSet().contains(UtilWorld.chunkToStr(clan.GetHome().getChunk())))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Your Clan has lost its Home Territory."));
			return;
		}

		//Do
		Clans.Teleport().TP(caller, clan.GetHome());

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You teleported to your Clan Home " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
	}

	public void homeSet(Player caller)
	{
		ClansClan clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (Clans.CUtil().getOwner(caller.getLocation()) == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You must set your Clan Home in your own Territory."));
			return;
		}

		if (!Clans.CUtil().getOwner(caller.getLocation()).isSelf(clan.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You must set your Clan Home in your own Territory."));
			return;
		}

		//Task
		Clans.CTask().home(clan, caller.getLocation(), caller.getName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
		clan.inform(caller.getName() + " set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + ".", caller.getName());
	}
	
	public void safe(Player caller)
	{
		ClansTerritory claim = Clans.CUtil().getClaim(caller.getLocation());

		if (claim == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You can only Safe Zone on Claimed Territory."));
			return;
		}
		
		//Set
		Clans.CTask().safe(claim, caller.getName());
		
		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "Territory Safe Zone: " + F.tf(claim.safe)));
	}
}
