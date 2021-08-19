package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;

import mineplex.core.account.CoreClient;
import mineplex.core.common.Rank;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.minecraft.punish.PunishChatEvent;
import nautilus.game.pvp.modules.clans.ClansClan.Role;
import nautilus.game.pvp.modules.clans.ClansUtility.ClanRelation;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class ClansCommand 
{
	private Clans Clans;
	public ClansCommand(Clans clans)
	{
		Clans = clans;
	}

	public String[] denyClan = new String[] {
			"outpost", "neut", "neutral", "sethome", "promote", "demote", "admin", "help", "create", "disband", "delete", "invite", "join", "kick", "ally", "trust", "enemy", "claim", "unclaim", "territory", "home"}; 

	public void command(Player caller, String[] args) 
	{
		if (args.length == 0)
		{
			if (Clans.Clients().Get(caller).Clan().InClan())	
				infoClan(caller, Clans.Clients().Get(caller).Clan().GetClanName());

			else									
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;	
		}

		if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))
			help(caller);

		else if (args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("x"))
			Clans.CAdmin().command(caller, args);

		else if (args[0].equalsIgnoreCase("create"))
			create(caller, args);

		else if (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("delete"))
			delete(caller, args);

		else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("i"))
			invite(caller, args);

		else if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("+"))
			promote(caller, args);

		else if (args[0].equalsIgnoreCase("demote") || args[0].equalsIgnoreCase("-"))
			demote(caller, args);

		else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j"))
			join(caller, args);

		else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l"))
			leave(caller, args);

		else if (args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("k"))
			kick(caller, args);

		else if (args[0].equalsIgnoreCase("ally") || args[0].equalsIgnoreCase("a"))
			ally(caller, args);

		else if (args[0].equalsIgnoreCase("trust"))
			trust(caller, args);

		else if (args[0].equalsIgnoreCase("neutral") || args[0].equalsIgnoreCase("neut") || args[0].equalsIgnoreCase("n"))
			neutral(caller, args);

		else if (args[0].equalsIgnoreCase("enemy") || args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("war") || args[0].equalsIgnoreCase("w"))
			enemy(caller, args);

		else if (args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("c"))
			claim(caller, args);

		else if (args[0].equalsIgnoreCase("unclaim") || args[0].equalsIgnoreCase("uc"))
			unclaim(caller, args);

		else if (args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("m"))
			map(caller, args);

		else if (args[0].equalsIgnoreCase("home") || args[0].equalsIgnoreCase("h"))
			home(caller, args);
		
		else if (args[0].equalsIgnoreCase("outpost"))
			outpost(caller);

		else if (args[0].equalsIgnoreCase("sethome"))
			homeSet(caller);

		else if (args[0].equalsIgnoreCase("territory") || args[0].equalsIgnoreCase("t"))
			infoTerritory(caller, args);

		else if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("w"))
		{
			if (args.length > 1)	infoClan(caller, args[1]);
			else					infoClan(caller, null);
		}

		else
			infoClan(caller, args[0]);
	}

	public void commandChat(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Clans.Clients().Get(caller).Clan().SetClanChat(!Clans.Clients().Get(caller).Clan().IsClanChat());
			UtilPlayer.message(caller, F.main("Clans", "Clan Chat: " + F.oo(Clans.Clients().Get(caller).Clan().IsClanChat())));
			return;
		}

		//Single Clan
		if (!Clans.Clients().Get(caller).Clan().IsClanChat())
		{
			ClansClan clan = Clans.CUtil().getClanByPlayer(caller);
			if (clan == null)	
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			else
				Clans.ChatClan(clan, caller, F.combine(args, 0, null, false), F.combine(args, 0, null, false));
		}
		//Single Normal
		else
		{
			PunishChatEvent event = new PunishChatEvent(caller);
			
			Clans.Plugin().getServer().getPluginManager().callEvent(event);
			
			if (event.isCancelled())
				return;
			
			Clans.ChatGlobal(caller, F.combine(args, 0, null, false), F.combine(args, 0, null, false));
		}
	}
	
	public void commandAllyChat(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Clans.Clients().Get(caller).Clan().SetAllyChat(!Clans.Clients().Get(caller).Clan().IsAllyChat());
			UtilPlayer.message(caller, F.main("Clans", "Ally Chat: " + F.oo(Clans.Clients().Get(caller).Clan().IsAllyChat())));
			return;
		}

		//Single Clan
		if (!Clans.Clients().Get(caller).Clan().IsAllyChat())
		{
			ClansClan clan = Clans.CUtil().getClanByPlayer(caller);
			if (clan == null)	UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			else				Clans.ChatAlly(clan, caller, F.combine(args, 0, null, false), F.combine(args, 0, null, false));
		}
		//Single Normal
		else
		{
			PunishChatEvent event = new PunishChatEvent(caller);
			
			Clans.Plugin().getServer().getPluginManager().callEvent(event);
			
			if (event.isCancelled())
				return;
			
			Clans.ChatGlobal(caller, F.combine(args, 0, null, false), F.combine(args, 0, null, false));
		}
	}

	private void help(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Clans", "Commands List;"));
		UtilPlayer.message(caller, F.help("/c create <clan>", "Create new Clan", Rank.ALL));
		UtilPlayer.message(caller, F.help("/c join <clan>", "Join a Clan", Rank.ALL));
		UtilPlayer.message(caller, F.help("/c leave <clan>", "Leave your Clan", Rank.ALL));
		UtilPlayer.message(caller, F.help("/c map <toggle>", "View Clan Map", Rank.ALL));
		UtilPlayer.message(caller, F.help("/cc (Message)", "Clan Chat (Toggle)", Rank.ALL));

		UtilPlayer.message(caller, F.help("/c promote <player>", "Promote Player in Clan", Rank.MODERATOR));
		UtilPlayer.message(caller, F.help("/c demote <player>", "Demote Player in Clan", Rank.MODERATOR));

		UtilPlayer.message(caller, F.help("/c home (set)", "Teleport to Clan Home", Rank.MODERATOR));

		UtilPlayer.message(caller, F.help("/c invite <player>", "Invite Player to Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c kick <player>", "Kick Player from Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c neutral <clan>", "Request Neutrality with Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c enemy <clan>", "Declare War with Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c ally <clan>", "Send Alliance to Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c trust <clan>", "Give Trust to Clan", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c claim", "Claim Territory", Rank.ADMIN));
		UtilPlayer.message(caller, F.help("/c unclaim (all)", "Unclaim Territory", Rank.ADMIN));

		UtilPlayer.message(caller, F.help("/c delete", "Delete your Clan", Rank.OWNER));

		UtilPlayer.message(caller, F.help("/c <clan>", "View Clan Information", Rank.ALL));
	}

	public void create(Player caller, String[] args)
	{
		CoreClient client = Clans.Clients().Get(caller);

		if (client.Clan().InClan())
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already in a Clan."));
			return;
		}

		if (!client.Clan().CanJoin())
		{
			UtilPlayer.message(caller, F.main("Clans", "You recently left a Clan with low Dominance."));
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + 
					C.mTime + UtilTime.convertString(System.currentTimeMillis() - client.Clan().GetDelay(), 1, TimeUnit.FIT) + 
					C.mBody + "."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan name."));
			return;
		}

		if (!Clans.Util().Input().valid(args[1]))
		{
			UtilPlayer.message(caller, F.main("Clans", "Invalid characters in Clan name."));
			return;
		}

		if (args[1].length() < Clans.GetNameMin())
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name too short. Minimum length is " + (Clans.GetNameMin()) + "."));
			return;
		}

		if (args[1].length() > Clans.GetNameMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name too long. Maximum length is + " + (Clans.GetNameMax()) + "."));
			return;
		}

		for (String cur : denyClan)
		{
			if (cur.equalsIgnoreCase(args[1]))
			{
				UtilPlayer.message(caller, F.main("Clans", "Clan name cannot be a Clan command."));
				return;
			}
		}

		for (String cur : Clans.Clients().GetAll())
		{
			if (cur.equalsIgnoreCase(args[1]))
			{
				UtilPlayer.message(caller, F.main("Clans", "Clan name cannot be a Player name."));
				return;
			}
		}

		for (String cur : Clans.GetClanMap().keySet())
		{
			if (cur.equalsIgnoreCase(args[1]))
			{
				UtilPlayer.message(caller, F.main("Clans", "Clan name is already used by another Clan."));
				return;
			}
		}

		//Inform
		UtilServer.broadcast(F.main("Clans", F.name(caller.getName()) + " formed " + F.elem("Clan " + args[1]) + "."));

		//Create and Join
		Clans.CTask().join(Clans.CTask().create(caller.getName(), args[1], false), caller.getName(), Role.LEADER);
	}

	public void delete(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (Clans.CUtil().getRole(caller) != Role.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader can disband the Clan."));
			return;
		}

		//Task
		Clans.CTask().delete(clan);

		//Inform
		UtilServer.broadcast(F.main("Clans", F.name(caller.getName()) + " disbanded " + F.elem("Clan " + clan.GetName()) + "."));
	}

	public void invite(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (clan.GetMembers().get(caller.getName()) != Role.LEADER && 
				clan.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can send invites."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input an invitee."));
			return;
		}

		Player target = UtilPlayer.searchOnline(caller, args[1], true);
		if (target == null)
			return;

		if (target.getName().equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invite yourself."));
			return;
		}

		//Inform
		clan.inform(F.name(caller.getName()) + " invited " + F.name(target.getName()) + " to join your Clan.", caller.getName());
		UtilPlayer.message(caller, F.main("Clans", "You invited " + F.name(target.getName()) + " to join your Clan."));
		UtilPlayer.message(target, F.main("Clans", F.name(caller.getName()) + " invited you to join " + F.elem("Clan " + clan.GetName()) + "."));
		UtilPlayer.message(target, F.main("Clans", "Type " + F.elem("/c join " + clan.GetName()) + " to accept!"));

		//Task
		Clans.CTask().invite(clan, target.getName(), caller.getName());
	}

	public void join(Player caller, String[] args)
	{
		CoreClient client = Clans.Clients().Get(caller);

		if (client.Clan().InClan())
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already in a Clan."));
			return;
		}

		if (!client.Clan().CanJoin())
		{
			UtilPlayer.message(caller, F.main("Clans", "You recently left a Clan with low " + F.elem("Dominance") + "."));
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + 
					C.mTime + UtilTime.convertString(System.currentTimeMillis() - client.Clan().GetDelay(), 1, TimeUnit.FIT) + 
					C.mBody + "."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan name."));
			return;
		}

		if (!Clans.Util().Input().valid(args[1]))
		{
			UtilPlayer.message(caller, F.main("Clans", "Invalid characters in Clan name."));
			return;
		}

		ClansClan clan = Clans.CUtil().searchClanPlayer(caller, args[1], true);
		if (clan == null)
			return;

		if (!clan.isInvited(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not invited to " + F.elem("Clan " + clan.GetName()) + "."));
			return;
		}

		//Task
		Clans.CTask().join(clan, caller.getName(), Role.RECRUIT);

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You joined " + F.elem("Clan " + clan.GetName()) + "."));
		clan.inform(F.name(caller.getName()) + " has joined your Clan.", caller.getName());
	}

	public void leave(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (clan.GetMembers().get(caller.getName()) == Role.LEADER && clan.GetMembers().size() > 1)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must pass on " + F.elem("Leadership") + " before leaving."));
			return;
		}

		//Leave or Delete
		if (clan.GetMembers().size() > 1)
		{
			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You left " + F.elem("Clan " + clan.GetName()) + "."));

			//Task
			Clans.CTask().leave(clan, caller.getName());

			//Inform
			clan.inform(F.name(caller.getName()) + " has left your Clan.", null);
		}
		else
		{
			delete(caller, args);
		}
	}

	public void kick(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Player to kick."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[1], clan.GetMembers().keySet(), "Clan Member", true);

		if (target == null)
			return;

		if (clan.GetMembers().get(caller.getName()) != Role.LEADER && 
				clan.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can kick members."));
			return;
		}

		if ((clan.GetMembers().get(target) == Role.LEADER && clan.GetMembers().get(caller.getName()) == Role.ADMIN) ||
				(clan.GetMembers().get(target) == Role.ADMIN && clan.GetMembers().get(caller.getName()) == Role.ADMIN))
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(target) + "."));
			return;
		}


		//Task
		Clans.CTask().leave(clan, target);

		//Inform
		UtilPlayer.message(UtilPlayer.searchOnline(null, target, false), F.main("Clans", F.name(caller.getName()) + " kicked you from " + F.elem("Clan " + clan.GetName()) + "."));
		UtilPlayer.message(caller, F.main("Clans", "You kicked " + F.name(target) + " from your Clan."));
		clan.inform(F.main("Clans", F.name(caller.getName()) + " kicked " + F.name(target) + " from your Clan."), caller.getName());
	}

	public void promote(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input player to promote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[1], clan.GetMembers().keySet(), "Clan Member", true);

		if (target == null)
			return;

		if (target.equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot promote yourself."));
			return;
		}

		if (clan.GetMembers().get(caller.getName()).toInt() <= clan.GetMembers().get(target).toInt())
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(target) + "."));
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

			//Give Leader
			Clans.CTask().role(clan, caller.getName(), Role.ADMIN);
		}

		//Inform
		clan.inform(F.name(caller.getName()) + " promoted " + F.name(target) + " to " + F.elem(newRank) + ".", null);
	}

	public void demote(Player caller, String[] args)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input player to demote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[1], clan.GetMembers().keySet(), "Clan Member", true);

		if (target == null)
			return;

		if (target.equals(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot demote yourself."));
			return;
		}

		if (clan.GetMembers().get(caller.getName()).toInt() <= clan.GetMembers().get(target).toInt())
		{
			UtilPlayer.message(caller, F.main("Clans", "You do not outrank " + F.name(target) + "."));
			return;
		}

		if (clan.GetMembers().get(target) == Role.RECRUIT)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot demote " + F.name(target) + " any further."));
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

		//Inform
		clan.inform(F.main("Clans", F.name(caller.getName()) + " demoted " + F.name(target) + " to " + F.elem(newRank) + "."), null);
	}

	public void ally(Player caller, String[] args)
	{
		ClansClan cA = Clans.CUtil().getClanByPlayer(caller);

		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (cA.GetMembers().get(caller.getName()) != Role.LEADER && cA.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Alliances."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to ally."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[1], true);

		if (cB == null)
			return;

		if (cA.isSelf(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot ally with yourself."));
			return;
		}

		if (cA.isAlly(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already allies with " + F.elem("Clan " + cB.GetName()) + "."));
			return;
		}

		if (cA.isEnemy(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You must end war with " + F.elem("Clan " + cB.GetName()) + " first."));
			return;
		}

		if (cA.getAllies() >= cA.getAlliesMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot have any more allies."));
			return;
		}

		if (cB.getAllies() >= cB.getAlliesMax())
		{
			UtilPlayer.message(caller, F.main("Clans", F.elem("Clan " + cB.GetName()) + " cannot have any more allies."));
			return;
		}

		if (cB.isRequested(cA.GetName()))
		{
			//Task
			Clans.CTask().ally(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You accepted alliance with " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " accepted alliance with " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has accepted alliance with you.", null);	
		}
		else
		{
			//Task 
			Clans.CTask().requestAlly(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You requested alliance with " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " has requested alliance with " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has requested alliance with you.", null);	
		}
	}

	public void trust(Player caller, String[] args)
	{
		CoreClient client = Clans.Clients().Get(caller);
		ClansClan cA = Clans.getClan(client.Clan().GetClanName());

		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}		

		if (cA.GetMembers().get(caller.getName()) != Role.LEADER && cA.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Trust."));
			return;
		}

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to enemy."));
			return;
		}

		ClansClan cB = Clans.CUtil().searchClanPlayer(caller, args[1], true);

		if (cB == null)
			return;

		if (!cA.isAlly(cB.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot give trust to enemies."));
			return;
		}

		//Task
		if (Clans.CTask().trust(cA, cB, caller.getName()))
		{
			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You gave trust to " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " has given trust to " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has given trust to you.", null);	
		}
		else
		{
			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You revoked trust to " + F.elem("Clan " + cB.GetName()) + "."));
			cA.inform(F.name(caller.getName()) + " has revoked trust to " + F.elem("Clan " + cB.GetName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.GetName()) + " has revoked trust to you.", null);	
		}
	}

	public void neutral(Player caller, String[] args)
	{
		CoreClient client = Clans.Clients().Get(caller);
		ClansClan cA = Clans.getClan(client.Clan().GetClanName());

		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}	

		if (cA.GetMembers().get(caller.getName()) != Role.LEADER && cA.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage relationships."));
			return;
		}

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
		CoreClient client = Clans.Clients().Get(caller);
		ClansClan cA = Clans.getClan(client.Clan().GetClanName());

		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}	

		if (cA.GetMembers().get(caller.getName()) != Role.LEADER && cA.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage invasions."));
			return;
		}

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

		if (cA.GetEnemyRecharge().containsKey(cB.GetName()))
		{
			long time = cA.GetEnemyRecharge().get(cB.GetName());
			if (System.currentTimeMillis() < time)
			{
				UtilPlayer.message(caller, F.main("Clans", "You cannot invade " + F.elem("Clan " + cB.GetName()) + " for " +
						F.time(UtilTime.convertString(time - System.currentTimeMillis(), 1, TimeUnit.FIT)) + "." ));
				return;
			}
		}

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

	public void claim(Player caller, String args[])
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		if (clan.GetMembers().get(caller.getName()) != Role.LEADER && clan.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can claim Territory."));
			return;
		}

		if (caller.getLocation().getChunk().getX() < -24 ||
				caller.getLocation().getChunk().getX() > 23 ||
				caller.getLocation().getChunk().getZ() < -24 ||
				caller.getLocation().getChunk().getZ() > 23)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim Territory this far away."));
			return;
		}
		
		for (ClansOutpost outpost : Clans.GetOutpostMap().values())
		{
			if (outpost.GetLocation().getChunk().equals(caller.getLocation().getChunk()))
			{
				UtilPlayer.message(caller, F.main("Clans", "You cannot claim Territory containing a Clan Outpost."));
				return;
			}
		}

		String chunk = UtilWorld.chunkToStr(caller.getLocation().getChunk());
		ClansClan ownerClan = Clans.CUtil().getOwner(caller.getLocation());	

		//Try to Steal
		if (ownerClan != null && !ownerClan.equals(clan))
			if (unclaimSteal(caller, clan, ownerClan))
			{
				return;
			}
			else
			{
				UtilPlayer.message(caller, F.main("Clans", "This Territory is owned by " + 
						Clans.CUtil().mRel(Clans.CUtil().relPC(caller.getName(), ownerClan), ownerClan.GetName(), true) + "."));
				return;
			}

		if (clan.getClaims() >= clan.getClaimsMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan cannot claim more Territory."));
			return;
		}

		//Boxed
		if (checkBox(caller.getLocation().getChunk(), 4))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot claim this Territory, it causes a box."));
			UtilPlayer.message(caller, F.main("Clans", "This means a Territory has all sides claimed."));
			return;
		}

		//Adjacent
		boolean selfAdj = false;
		for (int x=-1 ; x<=1 ; x++)
			for (int z=-1 ; z<=1 ; z++)
			{
				if (x== 0 && z == 0)
					continue;

				String other = UtilWorld.chunkToStr(caller.getWorld().getChunkAt(
						caller.getLocation().getChunk().getX()+x, 
						caller.getLocation().getChunk().getZ()+z));

				ClansClan adjClan = Clans.CUtil().getOwner(other);

				if (adjClan == null)
					continue;

				if (x == 0 || z == 0)
				{
					if (checkBox(caller.getWorld().getChunkAt(
							caller.getLocation().getChunk().getX()+x, 
							caller.getLocation().getChunk().getZ()+z), 3))
					{
						UtilPlayer.message(caller, F.main("Clans", "You cannot claim this Territory, it causes a box."));
						UtilPlayer.message(caller, F.main("Clans", "This means a Territory has all sides claimed."));
						return;
					}

					if (Clans.CUtil().rel(clan, adjClan) == ClanRelation.SELF)
					{
						selfAdj = true;
					}

					else if (Clans.CUtil().rel(clan, adjClan) != ClanRelation.SELF)
					{
						UtilPlayer.message(caller, F.main("Clans", "You cannot claim Territory next to " + 
								Clans.CUtil().mRel(Clans.CUtil().rel(ownerClan, adjClan), adjClan.GetName(), true) + "."));
						return;
					}
				}	
			}

		//Not Next to Self
		if (!selfAdj && !clan.GetClaimSet().isEmpty())
		{
			UtilPlayer.message(caller, F.main("Clans", "You must claim next to your other Territory."));
			return;
		}

		//Claim Timer
		if (Clans.GetUnclaimMap().containsKey(chunk))
		{
			if (!UtilTime.elapsed(Clans.GetUnclaimMap().get(chunk), Clans.GetReclaimTime()))
			{
				UtilPlayer.message(caller, F.main("Clans", "This Territory cannot be claimed for " + 
						F.time(UtilTime.convertString(Clans.GetReclaimTime() - (System.currentTimeMillis()-Clans.GetUnclaimMap().get(chunk)), 1, TimeUnit.FIT)) + "."));

				return;
			}
			else
			{
				Clans.GetUnclaimMap().remove(chunk);
			}
		}

		//Enemies in Land
		for (Player cur : UtilServer.getPlayers())
			if (UtilMath.offset(cur, caller) < 16)
				if (Clans.CUtil().playerEnemy(caller.getName(), cur.getName()))
				{
					UtilPlayer.message(caller, F.main("Clans", "You cannot claim while enemies are nearby."));
					return;
				}		

		//Recharge
		if (!Clans.Recharge().use(caller, "Territory Claim", 60000, true))
			return;

		//Task
		Clans.CTask().claim(clan.GetName(), chunk, caller.getName(), false);

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clan.inform(F.name(caller.getName()) + " claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public boolean checkBox(Chunk chunk, int req)
	{
		int boxed = 0;
		for (int x=-1 ; x<=1 ; x++)
			for (int z=-1 ; z<=1 ; z++)
			{
				if (x == 0 && z == 0 || x != 0 && z != 0)
					continue;

				String other = UtilWorld.chunkToStr(chunk.getWorld().getChunkAt(
						chunk.getX()+x, chunk.getZ()+z));

				ClansClan adjClan = Clans.CUtil().getOwner(other);

				if (adjClan == null)
					continue;

				boxed++;
			}

		return (boxed >= req);
	}

	public void unclaim(Player caller, String args[])
	{
		if (args.length > 1)
		{
			if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("a"))
			{
				unclaimall(caller);
				return;
			}
		}

		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		String chunk = UtilWorld.chunkToStr(caller.getLocation().getChunk());
		ClansClan ownerClan = Clans.CUtil().getOwner(caller.getLocation());

		//Try to Steal
		if (ownerClan != null && !ownerClan.equals(clan))
			if (unclaimSteal(caller, clan, ownerClan))
				return;

		//Role
		if (clan.GetMembers().get(caller.getName()) != Role.LEADER && clan.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can unclaim Territory."));
			return;
		}

		//Not Claimed
		if (ownerClan == null || !ownerClan.equals(clan))
		{		
			UtilPlayer.message(caller, F.main("Clans", "This Territory is not owned by you."));
			return;
		}

		//Task
		Clans.CTask().unclaim(chunk, caller.getName(), true);

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clan.inform(F.name(caller.getName()) + " unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public boolean unclaimSteal(Player caller, ClansClan clientClan, ClansClan ownerClan)
	{
		if (!ownerClan.isEnemy(clientClan.GetName()) && !ownerClan.isNeutral(clientClan.GetName()))
			return false;

		if (ownerClan.getClaims() > ownerClan.getClaimsMax())
		{
			//Nothing Extra
		}
		else if (ownerClan.getPower() <= 0)
		{
			//Regen Power
			ownerClan.modifyPower(1);
		}
		else
		{
			return false;
		}

		//Change Inform
		UtilPlayer.message(caller, F.main("Clans", "You can no longer 'steal' territory. " +
				"You simply unclaim it and it can not be reclaimed by anyone for 30 mintes." +
				"This was done to improve gameplay. Enjoy!"));

		//Inform
		UtilServer.broadcast(F.main("Clans", F.elem(clientClan.GetName()) + " unclaimed from " + 
				F.elem(ownerClan.GetName()) + " at " + F.elem(UtilWorld.locToStrClean(caller.getLocation())) + "."));

		//Unclaim
		Clans.CTask().unclaim(UtilWorld.chunkToStr(caller.getLocation().getChunk()), caller.getName(), true);

		return true;
	}

	public void unclaimall(Player caller)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		if (clan.GetMembers().get(caller.getName()) != Role.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader can unclaim all Territory."));
			return;
		}

		//Unclaim
		ArrayList<String> toUnclaim = new ArrayList<String>();

		for (String chunk : clan.GetClaimSet())
			toUnclaim.add(chunk);

		for (String chunk : toUnclaim)
			Clans.CTask().unclaim(chunk, caller.getName(), true);

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You unclaimed all your Clans Territory."));
		clan.inform(F.name(caller.getName()) + " unclaimed all your Clans Territory.", caller.getName());
	}

	public void map(Player caller, String[] args)
	{
		if (args.length > 1)
		{
			if (args[1].equals("toggle") || args[1].equals("t"))
			{
				Clans.Clients().Get(caller).Clan().SetMapOn(!Clans.Clients().Get(caller).Clan().IsMapOn());
				UtilPlayer.message(caller, 
						F.main("Clans", "You toggled Clan Map: " + F.oo(Clans.Clients().Get(caller).Clan().IsMapOn())));
				return;
			}
		}

		//Display
		Clans.CDisplay().displayOwner(caller);
		Clans.CDisplay().displayMap(caller);
	}

	public void home(Player caller, String[] args)
	{
		if (args.length > 1)
		{
			if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("s"))	
			{
				homeSet(caller);
				return;
			}
		}

		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		if (clan.GetHome() == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan has not set a Home."));
			return;
		}

		if (!clan.GetClaimSet().contains(UtilWorld.chunkToStr(clan.GetHome().getChunk())))
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan has lost its Home Territory."));
			return;
		}

		if (!Clans.CUtil().isSafe(caller.getLocation()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You can only use Clan Home from Spawn."));
			return;
		}

		if (!Clans.CUtil().isSpecial(caller.getLocation(), "Spawn"))
		{
			UtilPlayer.message(caller, F.main("Clans", "You can only use Clan Home from Spawn."));
			return;
		}
		
		/*
		CoreClient client = Clans.Clients().Get(caller);
		for (Player cur : clan.GetHome().getWorld().getPlayers())
			if (client.Clan().GetRelation(cur.getName()) == ClanRelation.NEUTRAL)
				if (clan.GetClaimSet().contains(UtilWorld.chunkToStr(cur.getLocation().getChunk())))
				{
					UtilPlayer.message(caller, F.main("Clans", "You cannot use Clan Home with enemies in your Territory."));
					return;
				}
		*/
		
		if (!Clans.Recharge().use(caller, "Clans Teleport", 300000, true))
			return;

		//Do
		Clans.Teleport().TP(caller, clan.GetHome());

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You teleported to your Clan Home " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
	}
	
	public void outpost(Player caller)
	{

		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		if (clan.GetOutpost() == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan does not have an Outpost."));
			return;
		}

		if (!Clans.CUtil().isSafe(caller.getLocation()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You can only use Clan Home from Spawn."));
			return;
		}

		if (!Clans.CUtil().isSpecial(caller.getLocation(), "Spawn"))
		{
			UtilPlayer.message(caller, F.main("Clans", "You can only use Clan Home from Spawn."));
			return;
		}
		
		if (!Clans.Recharge().use(caller, "Clans Teleport", 300000, true))
			return;

		//Do
		Clans.Teleport().TP(caller, clan.GetOutpost().GetLocation());

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You teleported to your Clan Outpost " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
	}

	public void homeSet(Player caller)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(caller);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}

		if (clan.GetMembers().get(caller.getName()) != Role.LEADER && clan.GetMembers().get(caller.getName()) != Role.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Clan Home."));
			return;
		}

		if (Clans.CUtil().getOwner(caller.getLocation()) == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must set your Clan Home in your own Territory."));
			return;
		}

		if (!Clans.CUtil().getOwner(caller.getLocation()).isSelf(clan.GetName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You must set your Clan Home in your own Territory."));
			return;
		}

		//Task
		Clans.CTask().home(clan, caller.getLocation(), caller.getName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans", "You set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
		clan.inform(caller.getName() + " set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + ".", caller.getName());
	}


	public void infoClan(Player caller, String search)
	{
		if (search == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a search parameter."));
			return;
		}

		ClansClan clan = Clans.CUtil().searchClanPlayer(caller, search, true);
		if (clan == null)
			return;

		UtilPlayer.message(caller, clan.mDetails(caller.getName()));
	}

	public void infoTerritory(Player caller, String[] args)
	{
		ClansClan clan;
		if (args.length < 2)
		{
			clan = Clans.CUtil().getClanByPlayer(caller);

			if (clan == null)
			{
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
				return;
			}
		}

		else
			clan = Clans.CUtil().searchClan(caller, args[1], true);

		if (clan == null)
			return;

		UtilPlayer.message(caller, clan.mTerritory());
	}
}
