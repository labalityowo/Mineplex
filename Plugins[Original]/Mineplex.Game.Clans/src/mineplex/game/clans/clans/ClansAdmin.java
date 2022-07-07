package mineplex.game.clans.clans;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInput;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.core.repository.tokens.ClanToken;
import mineplex.game.clans.core.war.ClanWarData;

public class ClansAdmin 
{
	public enum Perm implements Permission
	{
		USE_ADMIN_COMMANDS,
	}

	private ClansManager Clans;
	
	public ClansAdmin(ClansManager clans)
	{
		Clans = clans;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.USE_ADMIN_COMMANDS, true, true);
		PermissionGroup.CMOD.setPermission(Perm.USE_ADMIN_COMMANDS, false, true);
	}

	public void command(Player caller, String[] args) 
	{
		if (!Clans.getClientManager().Get(caller).hasPermission(Perm.USE_ADMIN_COMMANDS) && !caller.isOp())
		{
			UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			return;
		}
		
		if (!UtilServer.isTestServer())
		{
			StringBuilder cmd = new StringBuilder("/c");
			for (String a : args)
			{
				cmd.append(" ").append(a);
			}
			for (int i = 0; i < cmd.length(); i++)
			{
				if (cmd.charAt(i) == '`')
				{
					cmd.setCharAt(i, '\'');
				}
			}
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#clans-commandspy",
					new SlackMessage("Clans Command Logger", "crossed_swords", caller.getName() + " has issued command `" + cmd.toString() + "` on " + UtilServer.getServerName() + "."),
					true);
		}

		if (args.length == 1)
			help(caller);
		
		else if (args[1].equalsIgnoreCase("help") || args[1].equalsIgnoreCase("h"))
			help(caller);

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

//		else if (args[1].equalsIgnoreCase("trust"))
//			trust(caller, args);

		else if (args[0].equalsIgnoreCase("neutral") || args[0].equalsIgnoreCase("neut") || args[0].equalsIgnoreCase("n"))
			neutral(caller, args);

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
		
		else if (args[1].equalsIgnoreCase("wp"))
			wp(caller, args);
		
		else if (args[1].equalsIgnoreCase("rename"))
			rename(caller, args);

		else if (args[1].equalsIgnoreCase("timer"))
			resetTime(caller, args);

		else
			help(caller);
	}

	private void help(Player caller) 
	{
		UtilPlayer.message(caller, F.main("Clans Admin", "Admin Commands List;"));

		UtilPlayer.message(caller, F.help("/c x create <clan>", "Create Admin Clan", ChatColor.GOLD));
		
		UtilPlayer.message(caller, F.help("/c x set <clan>", "Set Mimic Clan", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x wp [remove/set/add] <number> <clan>", "Modify war points", ChatColor.GOLD));

		UtilPlayer.message(caller, F.help("/c x home (set)", "Teleport to Mimic Home", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x invite <player>", "Invite Player to Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x promote <player>", "Promote Player in Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x demote <player>", "Demote Player in Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x kick <player>", "Kick Player from Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x ally <clan>", "Send Alliance to Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x neutral <clan>", "Set Neutrality", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x enemy <clan>", "Start Invasion", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x rename <name>", "Change the name of Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x claim", "Claim Territory for Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x unclaim (all)", "Unclaim Territory for Mimic", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x delete", "Delete Mimic Clan", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x autoclaim", "AutoClaim for Mimic Clan", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c x timer <player>", "Reset Clan Create Timer", ChatColor.GOLD));
		UtilPlayer.message(caller, F.main("Mimic Clan", Clans.Get(caller).getMimic()));
	}

	private void resetTime(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not enter a player name"));
			return;
		}

		Player player = UtilPlayer.searchOnline(caller, args[2], true);

		if (player != null)
		{
			Clans.resetLeftTimer(player.getUniqueId());
			UtilPlayer.message(caller, F.main("Clans Admin", "Reset Clan create timer for " + F.name(player.getName())));
		}
	}
	
	private void autoclaim(Player caller) 
	{
		Clans.Get(caller).setAutoClaim(!Clans.Get(caller).isAutoClaim());
		
		UtilPlayer.message(caller, F.main("Clans Admin", F.oo("Auto Claim", Clans.Get(caller).isAutoClaim())));
	}

	public void setMimic(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			if (Clans.Get(caller).getMimic().length() > 0)
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "You are no longer mimicing " + F.elem("Clan " + Clans.Get(caller).getMimic()) + "."));
				Clans.Get(caller).setMimic("");
			}
			else
				UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan/Player."));
			
			return;
		}

		ClanInfo clan = Clans.getClanUtility().searchClanPlayer(caller, args[2], true);

		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid Clan/Player."));
			return;
		}

		//Set Mimic
		Clans.Get(caller).setMimic(clan.getName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You are mimicing " + F.elem("Clan " + clan.getName()) + "."));
	}

	public ClanInfo getMimic(Player caller, boolean inform)
	{
		String mimic = Clans.Get(caller).getMimic();
		
		if (mimic.length() == 0)
			return null;
		
		ClanInfo clan = Clans.getClanUtility().searchClanPlayer(caller, mimic, true);

		if (clan == null)
		{
			if (inform)
				UtilPlayer.message(caller, F.main("Clans Admin", "You are not mimicing a Clan."));

			return null;	
		}

		return clan;	
	}

	public void create(final Player caller, final String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan name."));
			return;
		}

		if (!UtilInput.valid(args[2]))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid characters in Clan name."));
			return;
		}

		if (args[2].length() < Clans.getNameMin())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Clan name too short. Minimum length is " + (Clans.getNameMin()) + "."));
			return;
		}

		if (args[2].length() > Clans.getNameMax())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Clan name too long. Maximum length is " + (Clans.getNameMax()) + "."));
			return;
		}

		if (!Clans.getBlacklist().allowed(args[2]))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "That name is blacklisted, please choose another one."));
		}

		if (Clans.getClan(args[2]) != null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", F.elem("Clan " + args[2]) + " already exists."));
			return;
		}
		
		Clans.getClientManager().checkPlayerNameExact(new Callback<Boolean>()
		{
			public void run(final Boolean nameExists)
			{
				Bukkit.getServer().getScheduler().runTask(Clans.getPlugin(), new Runnable()
				{
					public void run()
					{
						if (nameExists)
						{
							UtilPlayer.message(caller, F.main("Clans Admin", "Clan name cannot be a Player name."));
						}
						else
						{
							Clans.getClanDataAccess().create(caller.getName(), args[2], true, new Callback<ClanInfo>()
							{
								@Override
								public void run(ClanInfo data)
								{
									if (data != null)
									{
										//Inform
										UtilServer.broadcast(F.main("Clans Admin", caller.getName() + " formed " + F.elem("Admin Clan " + args[2]) + "."));

										// Set Mimic
										Clans.Get(caller).setMimic(args[2]);

										// Inform
										UtilPlayer.message(caller, F.main("Clans Admin", "You are mimicing Clan " + args[2] + "."));
									}
									else
									{
										UtilPlayer.message(caller, F.main("Clans", "There was an error creating a clan! Try again later"));
									}
								}
							});
						}
					}
				});
			}
		}, args[2]);
	}

	public void delete(final Player caller, String[] args)
	{
		final ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		//Task
		Clans.getClanDataAccess().delete(clan, new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				if (data)
				{
					//Inform
					UtilServer.broadcast(F.main("Clans Admin", caller.getName() + " disbanded " + F.elem("Clan " + clan.getName()) + "."));
				}
				else
				{
					UtilPlayer.message(caller, F.main("Clans", "There was an error disbanding your clan! Try again later"));
				}
			}
		});
	}
	
	public void wp(Player caller, String[] args)
	{
		ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 5)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not supply an operation, value, and clan name."));
			return;
		}
		
		ClanInfo clanAgainst = Clans.getClan(args[4]);
		
		if (clanAgainst == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid Clan against provided."));
			return;
		}

		String operation = args[2];
		int value;
		
		try
		{
			value = Integer.parseInt(args[3]);
		}
		catch (Exception e)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid value provided."));
			return;
		}
		
		if (operation.equalsIgnoreCase("remove"))
		{
			ClanWarData war = clan.getWarData(clanAgainst);
			
			if (war != null)
			{
				war.set(clan.getName(), war.getClanAPoints() - value);
			
				Clans.getClanDataAccess().updateWar(clan, clanAgainst, war, success ->
				{
					UtilPlayer.message(caller, F.main("Clans Admin", "Updated war points against " + F.elem(war.getClanB())));
					Clans.messageClan(clan, F.main("Clans", "Your war points with " + F.elem(clanAgainst.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
					Clans.messageClan(clanAgainst, F.main("Clans", "Your war points with " + F.elem(clan.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
				});
			}
			else
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "There is no war between those two clans!"));
				return;
			}
		}
		else if (operation.equalsIgnoreCase("set"))
		{
			ClanWarData war = clan.getWarData(clanAgainst);
			
			if (war != null)
			{
				war.set(clanAgainst.getName(), value);

				Clans.getClanDataAccess().updateWar(clan, clanAgainst, war, success ->
				{
					UtilPlayer.message(caller, F.main("Clans Admin", "Updated war points against " + F.elem(war.getClanB())));
					Clans.messageClan(clan, F.main("Clans", "Your war points with " + F.elem(clanAgainst.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
					Clans.messageClan(clanAgainst, F.main("Clans", "Your war points with " + F.elem(clan.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
				});
			}
			else
			{
				Clans.getClanDataAccess().war(clan, clanAgainst, value, data ->
				{
					UtilPlayer.message(caller, F.main("Clans Admin", "Updated war points against " + F.elem(data.getClanB())));
					Clans.messageClan(clan, F.main("Clans", "Your war points with " + F.elem(clanAgainst.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
					Clans.messageClan(clanAgainst, F.main("Clans", "Your war points with " + F.elem(clan.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
				});
			}
		}
		else if (operation.equalsIgnoreCase("add"))
		{
			ClanWarData war = clan.getWarData(clanAgainst);
			
			if (war != null)
			{
			war.set(clanAgainst.getName(), war.getClanAPoints() + value);

			Clans.getClanDataAccess().updateWar(clan, clanAgainst, war, success ->
			{
				UtilPlayer.message(caller, F.main("Clans Admin", "Updated war points against " + F.elem(war.getClanB())));
				Clans.messageClan(clan, F.main("Clans", "Your war points with " + F.elem(clanAgainst.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
				Clans.messageClan(clanAgainst, F.main("Clans", "Your war points with " + F.elem(clan.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
			});
			}
			else
			{
				Clans.getClanDataAccess().war(clan, clanAgainst, value, data ->
				{
					UtilPlayer.message(caller, F.main("Clans Admin", "Updated war points against " + F.elem(data.getClanB())));
					Clans.messageClan(clan, F.main("Clans", "Your war points with " + F.elem(clanAgainst.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
					Clans.messageClan(clanAgainst, F.main("Clans", "Your war points with " + F.elem(clan.getName()) + " have been edited by " + F.elem(caller.getName()) + "!"));
				});
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Invalid operation provided."));
		}
	}

	public void invite(Player caller, String[] args)
	{
		ClanInfo clan = getMimic(caller, true);

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

		//Inform
		clan.inform(caller.getName() + " invited " + target.getName() + " to join Clan " + clan.getName() + ".", caller.getName());
		UtilPlayer.message(caller, F.main("Clans Admin", "You invited " + target.getName() + " to join " + F.elem("Clan " + clan.getName()) + "."));
		UtilPlayer.message(target, F.main("Clans Admin", caller.getName() + " invited you to join " + F.elem("Clan " + clan.getName()) + "."));

		//Task
		Clans.getClanDataAccess().invite(clan, target.getName(), caller.getName());
	}
	
	public void promote(Player caller, String[] args)
	{
		ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input player to promote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[2], clan.getMemberNameSet(), "Clan Member", true);
		ClansPlayer clansPlayer = clan.getClansPlayerFromName(target);

		if (target == null || clansPlayer == null)
			return;

		if (clansPlayer.getRole() == ClanRole.LEADER)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot promote " + F.name(target) + " any further."));
			return;
		}
		
		//Task
		String newRank = "?";
		if (clansPlayer.getRole() == ClanRole.RECRUIT)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.MEMBER);
			newRank = "Member";
		}
		else if (clansPlayer.getRole() == ClanRole.MEMBER)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.ADMIN);
			newRank = "Admin";
		}
		else if (clansPlayer.getRole() == ClanRole.ADMIN)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.LEADER);
			newRank = "Leader";
		}

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You promoted " + target + " to " + newRank + " in Mimic Clan."));
		clan.inform(caller.getName() + " promoted " + target + " to " + newRank + ".", null);
	}
	
	public void demote(Player caller, String[] args)
	{
		ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input player to demote."));
			return;
		}

		String target = UtilPlayer.searchCollection(caller, args[2], clan.getMemberNameSet(), "Clan Member", true);
		ClansPlayer clansPlayer = clan.getClansPlayerFromName(target);
		if (target == null || clansPlayer == null)
			return;

		if (clansPlayer.getRole() == ClanRole.RECRUIT)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot demote " + F.name(target) + " any further."));
			return;
		}
		
		//Task
		String newRank = "?";
		if (clansPlayer.getRole() == ClanRole.MEMBER)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.RECRUIT);
			newRank = "Recruit";
		}
		else if (clansPlayer.getRole() == ClanRole.ADMIN)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.MEMBER);
			newRank = "Member";
		}
		else if (clansPlayer.getRole() == ClanRole.LEADER)
		{
			Clans.getClanDataAccess().role(clan, clansPlayer.getUuid(), ClanRole.ADMIN);
			newRank = "Admin";
		}

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You demoted " + target + " to " + newRank + " in Mimic Clan."));
		clan.inform(F.main("Clans Admin", caller.getName() + " demoted " + target + " to " + newRank + "."), null);
	}
	
	public void kick(final Player caller, String[] args)
	{
		final ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;		

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Player to kick."));
			return;
		}

		final String target = UtilPlayer.searchCollection(caller, args[2], clan.getMemberNameSet(), "Clan Member", true);
		ClansPlayer clansPlayer = clan.getClansPlayerFromName(target);

		if (target == null || clansPlayer == null)
			return;

		final Player player = UtilPlayer.searchExact(clansPlayer.getUuid());

		Callback<Boolean> callback = new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				if (data)
				{
					//Inform
					if (player != null)
						UtilPlayer.message(player, F.main("Clans Admin", caller.getName() + " kicked you from " + F.elem("Clan " + clan.getName()) + "."));
					UtilPlayer.message(caller, F.main("Clans Admin", "You kicked " + target + " from your Clan."));
					clan.inform(F.main("Clans Admin", caller.getName() + " kicked " + target + " from your Clan."), caller.getName());
				}
				else
				{
					UtilPlayer.message(caller, F.main("Clans Admin", "There was an error processing your request"));
				}
			}
		};

		if (player != null) Clans.getClanDataAccess().leave(clan, player, callback);
		else				Clans.getClanDataAccess().leave(clan, clansPlayer, callback);
	}

	public void ally(Player caller, String[] args)
	{
		ClanInfo cA = getMimic(caller, true);

		if (cA == null)
			return;			

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan to ally."));
			return;
		}

		ClanInfo cB = Clans.getClanUtility().searchClanPlayer(caller, args[2], true);

		if (cB == null)
			return;

		if (cA.isSelf(cB.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You cannot ally with yourself."));
			return;
		}

		if (cA.isAlly(cB.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You are already allies with " + F.elem("Clan " + cB.getName()) + "."));
			return;
		}

		if (cB.isRequested(cA.getName()))
		{
			//Task
			Clans.getClanDataAccess().ally(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You accepted alliance with Clan " + cB.getName() + "."));
			cA.inform(caller.getName() + " accepted alliance with Clan " + cB.getName() + ".", caller.getName());
			cB.inform("Clan " + cA.getName() + " has accepted alliance with you.", null);	
		}
		else
		{
			//Task 
			Clans.getClanDataAccess().requestAlly(cA, cB, caller.getName());

			//Inform
			UtilPlayer.message(caller, F.main("Clans Admin", "You requested alliance with Clan " + cB.getName() + "."));
			cA.inform(caller.getName() + " has requested alliance with Clan " + cB.getName() + ".", caller.getName());
			cB.inform("Clan " + cA.getName() + " has requested alliance with you.", null);	
		}
	}
	

	public void rename(Player caller, String[] args)
	{
		ClanInfo cA = getMimic(caller, true);

		if (cA == null)
			return;			

		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a new name."));
			return;
		}

		String name = args[2];
		
		final ClanToken clan = new ClanToken();
		
		// Populate simple fields
		clan.Id = cA.getId();
		clan.Name = cA.getName();
		clan.Description = cA.getDesc();
		clan.Home = UtilWorld.locToStr(cA.getHome());
		clan.Admin = cA.isAdmin();
		clan.Energy = cA.getEnergy();
		clan.Kills = cA.getKills();
		clan.Murder = cA.getMurder();
		clan.Deaths = cA.getDeaths();
		clan.WarWins = cA.getWarWins();
		clan.WarLosses = cA.getWarLosses();
		clan.GeneratorBuyer = cA.getGenerator().getBuyer().toString();
		clan.GeneratorStock = cA.getGenerator().getStock();
		clan.DateCreated = cA.getDateCreated();
		clan.LastOnline = cA.getLastOnline();
		clan.EloRating = cA.getEloRating();
		
//		clan.Members = cA.getMembers();
		
	}

//	public void trust(Player caller, String[] args)
//	{
//		ClanInfo callerClan = getMimic(caller, true);
//
//		if (callerClan == null)
//			return;		
//
//		if (args.length < 3)
//		{
//			UtilPlayer.message(caller, F.main("Clans Admin", "You did not input a Clan to enemy."));
//			return;
//		}
//
//		ClanInfo otherClan = Clans.getClanUtility().searchClanPlayer(caller, args[2], true);
//
//		if (otherClan == null)
//			return;
//
//		if (!callerClan.isAlly(otherClan.getName()))
//		{
//			UtilPlayer.message(caller, F.main("Clans Admin", "You must be allied to trust a clan!"));
//			return;
//		}
//
//		//Task
//		if (Clans.getClanDataAccess().trust(callerClan, otherClan, caller.getName()))
//		{
//			//Inform
//			UtilPlayer.message(caller, F.main("Clans Admin", "You gave trust to Clan " + otherClan.getName() + "."));
//			callerClan.inform(caller.getName() + " has given trust to Clan " + otherClan.getName() + ".", caller.getName());
//			otherClan.inform("Clan " + callerClan.getName() + " has given trust to you.", null);	
//		}
//		else
//		{
//			//Inform
//			UtilPlayer.message(caller, F.main("Clans Admin", "You revoked trust to Clan " + otherClan.getName() + "."));
//			callerClan.inform(caller.getName() + " has revoked trust to Clan " + otherClan.getName() + ".", caller.getName());
//			otherClan.inform("Clan " + callerClan.getName() + " has revoked trust to you.", null);	
//		}
//	}

	public void neutral(Player caller, String[] args)
	{
		ClanInfo cA = getMimic(caller, true);

		if (cA == null)
			return;		

		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to set neutrality with."));
			return;
		}

		ClanInfo cB = Clans.getClanUtility().searchClanPlayer(caller, args[1], true);

		if (cB == null)
			return;

		if (cB.isSelf(cA.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You prefer to think of yourself positively..."));
			return;
		}
		
		if (cB.isNeutral(cA.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already neutral with " + F.elem("Clan " + cB.getName()) + "."));
			return;
		}

		if (cB.isAlly(cA.getName()))
		{
			//Task
			Clans.getClanDataAccess().neutral(cA, cB, caller.getName(), true);

			//Inform
			UtilPlayer.message(caller, F.main("Clans", "You revoked alliance with " + F.elem("Clan " + cB.getName()) + "."));
			cA.inform(F.name(caller.getName()) + " revoked alliance with " + F.elem("Clan " + cB.getName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.getName()) + " has revoked alliance with you.", null);
			
			return;
		}
	}

	public void claim(Player caller)
	{
		ClanInfo clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;

		ClaimLocation chunk = ClaimLocation.of(caller.getLocation().getChunk());
		ClanInfo ownerClan = Clans.getClanUtility().getOwner(caller.getLocation());

		//Already Claimed
		if (ownerClan != null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "This Territory is claimed by " + 
					Clans.getClanUtility().mRel(Clans.getClanUtility().relPC(caller, ownerClan), ownerClan.getName(), true) + "."));
			return;
		}

		//Task
		Clans.getClanDataAccess().claim(clientClan.getName(), chunk, caller.getName(), false);
		
		//Inform
		if (clientClan.getClaims() >= clientClan.getClaimsMax())
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You have claimed for that clan past their normal limit."));
		}
		UtilPlayer.message(caller, F.main("Clans Admin", "You claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		clientClan.inform(caller.getName() + " claimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public void unclaim(Player caller, String[] args)
	{
		if (args.length > 2)
		{
			if (args[2].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("a"))
			{
				unclaimall(caller);
				return;
			}
		}

		ClanInfo clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;	

		ClaimLocation chunk = ClaimLocation.of(caller.getLocation().getChunk());
		ClanInfo ownerClan = Clans.getClanUtility().getOwner(caller.getLocation());

		//Not Claimed
		if (ownerClan == null)
		{		
			UtilPlayer.message(caller, F.main("Clans Admin", "Territory is not claimed."));
			return;
		}

		//Task
		Clans.getClanDataAccess().unclaim(chunk, caller.getName(), true);

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + "."));
		ownerClan.inform(caller.getName() + " unclaimed Territory " + F.elem(UtilWorld.chunkToStrClean(caller.getLocation().getChunk())) + ".", caller.getName());
	}

	public void unclaimall(Player caller)
	{
		ClanInfo clientClan = getMimic(caller, true);

		if (clientClan == null)
			return;	

		//Unclaim
		ArrayList<ClaimLocation> toUnclaim = new ArrayList<>();

		for (ClaimLocation chunk : clientClan.getClaimSet())
			toUnclaim.add(chunk);

		for (ClaimLocation chunk : toUnclaim)
			Clans.getClanDataAccess().unclaim(chunk, caller.getName(), true);

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

		ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (clan.getHome() == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Your Clan has not set a Home."));
			return;
		}

		if (!clan.getClaimSet().contains(ClaimLocation.of(clan.getHome().getChunk())))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "Your Clan has lost its Home Territory."));
			return;
		}

		//Do
		Clans.getTeleport().TP(caller, clan.getHome().add(0, .6, 0));

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You teleported to your Clan Home " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
	}

	public void homeSet(Player caller)
	{
		ClanInfo clan = getMimic(caller, true);

		if (clan == null)
			return;	

		if (Clans.getClanUtility().getOwner(caller.getLocation()) == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You must set your Clan Home in your own Territory."));
			return;
		}

		if (!Clans.getClanUtility().getOwner(caller.getLocation()).isSelf(clan.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You must set your Clan Home in your own Territory."));
			return;
		}

		//Task
		Clans.getClanDataAccess().setHome(clan, caller.getLocation(), caller.getName());

		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "You set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
		clan.inform(caller.getName() + " set Clan Home to " + UtilWorld.locToStrClean(caller.getLocation()) + ".", caller.getName());
	}
	
	public void safe(Player caller)
	{
		ClanTerritory claim = Clans.getClanUtility().getClaim(caller.getLocation());

		if (claim == null)
		{
			UtilPlayer.message(caller, F.main("Clans Admin", "You can only Safe Zone on Claimed Territory."));
			return;
		}
		
		//Set
		Clans.getClanDataAccess().safe(claim, caller.getName());
		
		//Inform
		UtilPlayer.message(caller, F.main("Clans Admin", "Territory Safe Zone: " + F.tf(claim.isSafe(caller.getLocation()))));
	}
}
