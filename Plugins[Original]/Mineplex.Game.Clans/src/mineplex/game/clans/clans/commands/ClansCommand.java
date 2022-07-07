package mineplex.game.clans.clans.commands;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.v1_8_R3.EnumDirection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInput;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.delayedtask.DelayedTask;
import mineplex.core.delayedtask.DelayedTaskClient;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClientClan;
import mineplex.game.clans.clans.event.ClanJoinEvent;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.clans.event.ClansCommandPreExecutedEvent;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.spawn.Spawn;

public class ClansCommand extends CommandBase<ClansManager>
{
	private ClansManager _clansManager;
	
	public ClansCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.CLANS_COMMAND, "c", "clan", "clans", "factions");
		
		_clansManager = plugin;
	}
	
	@Override
	public void Execute(Player caller, String[] args)
	{
		if (UtilServer.CallEvent(new ClansCommandPreExecutedEvent(caller, args)).isCancelled())
			return;

		if (args == null || args.length == 0)
		{
			_clansManager.getClanShop().attemptShopOpen(caller);
			return;
		}
		
		if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))
			help(caller);
			
		else if (args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("x"))
			Plugin.getClanAdmin().command(caller, args);
			
		else if (args[0].equalsIgnoreCase("create"))
			create(caller, args);
			
		else if (args[0].equalsIgnoreCase("disband") || args[0].equalsIgnoreCase("delete"))
			delete(caller, args);
			
		else if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("i"))
			invite(caller, args);
			
		else if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("+"))
			promote(caller, args);
		else if (args[0].equalsIgnoreCase("forcejoin") || args[0].equalsIgnoreCase("fj"))
			forceJoin(caller, args);
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
			
//		else if (args[0].equalsIgnoreCase("trust"))
//			trust(caller, args);
			
		else if (args[0].equalsIgnoreCase("neutral") || args[0].equalsIgnoreCase("neut") || args[0].equalsIgnoreCase("n"))
			neutral(caller, args);
			
		else if (args[0].equalsIgnoreCase("claim") || args[0].equalsIgnoreCase("c"))
			claim(caller, args);
			
		else if (args[0].equalsIgnoreCase("unclaim") || args[0].equalsIgnoreCase("uc"))
			unclaim(caller, args);
		else if (args[0].equalsIgnoreCase("home") || args[0].equalsIgnoreCase("h"))
			home(caller, args);
			
		else if (args[0].equalsIgnoreCase("sethome"))
			homeSet(caller);
		else if (args[0].equalsIgnoreCase("stuck"))
			stuck(caller);
			
		/*
		 * else if (args[0].equalsIgnoreCase("enemy") || args[0].equals("e"))
		 * enemy(caller, args);
		 */
			
		else if (args[0].equalsIgnoreCase("who") || args[0].equalsIgnoreCase("w"))
		{
			if (args.length > 1)
				infoClan(caller, args[1]);
			else
				infoClan(caller, null);
		}
		else
			infoClan(caller, args[0]);
	}
	
	private void forceJoin(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "forcejoin", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		if (!Plugin.getClientManager().Get(caller).hasPermission(ClansManager.Perm.FORCE_JOIN_COMMAND))
		{
			UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
			return;
		}
		
		if (args.length > 1)
		{
			ClanInfo clan = Plugin.getClan(args[1]);
			
			if (clan != null)
			{
				_clansManager.getClanUtility().join(caller, clan);
				_clansManager.getClanDataAccess().role(clan, caller.getUniqueId(), ClanRole.LEADER);
				UtilPlayer.message(caller, F.main("Clans", "You have successfully joined " + F.elem(clan.getName()) + " and are now Leader Role."));
			}
			else
			{
				UtilPlayer.message(caller, F.main("Clans", "Clan provided does not exist."));
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Clans", "No clan provided."));
		}
	}

	public void commandChat(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Plugin.Get(caller).setClanChat(!Plugin.Get(caller).isClanChat());
			UtilPlayer.message(caller, F.main("Clans", "Clan Chat: " + F.oo(Plugin.Get(caller).isClanChat())));
			return;
		}
		
		// Single Clan
		if (!Plugin.Get(caller).isClanChat())
		{
			ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
			if (clan == null)
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			else
				Plugin.chatClan(clan, caller, F.combine(args, 0, null, false));
		}
	}
	
	public void commandAllyChat(Player caller, String[] args)
	{
		if (args.length == 0)
		{
			Plugin.Get(caller).setAllyChat(!Plugin.Get(caller).isAllyChat());
			UtilPlayer.message(caller, F.main("Clans", "Ally Chat: " + F.oo(Plugin.Get(caller).isAllyChat())));
			return;
		}
		
		// Single Clan
		if (!Plugin.Get(caller).isAllyChat())
		{
			ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
			if (clan == null)
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			else
				Plugin.chatAlly(clan, caller, F.combine(args, 0, null, false));
		}
	}
	
	private void help(Player caller)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "help");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		UtilPlayer.message(caller, F.main("Clans", "Commands List;"));
		UtilPlayer.message(caller, F.help("/c create <clan>", "Create new Clan", ChatColor.WHITE));
		UtilPlayer.message(caller, F.help("/c join <clan>", "Join a Clan", ChatColor.WHITE));
		UtilPlayer.message(caller, F.help("/c leave <clan>", "Leave your Clan", ChatColor.WHITE));
		UtilPlayer.message(caller, F.help("/cc (Message)", "Clan Chat (Toggle)", ChatColor.WHITE));
		
		UtilPlayer.message(caller, F.help("/c stuck", "Teleports you to the nearest Wilderness location", ChatColor.WHITE));
		
		UtilPlayer.message(caller, F.help("/c promote <player>", "Promote Player in Clan", ChatColor.GOLD));
		UtilPlayer.message(caller, F.help("/c demote <player>", "Demote Player in Clan", ChatColor.GOLD));
		
		UtilPlayer.message(caller, F.help("/c home (set)", "Teleport to Clan Home", ChatColor.GOLD));
		
		UtilPlayer.message(caller, F.help("/c invite <player>", "Invite Player to Clan", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c kick <player>", "Kick Player from Clan", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c neutral <clan>", "Request Neutrality with Clan", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c enemy <clan>", "Declare ClanWar with Clan", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c ally <clan>", "Send Alliance to Clan", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c claim", "Claim Territory", ChatColor.DARK_RED));
		UtilPlayer.message(caller, F.help("/c unclaim (all)", "Unclaim Territory", ChatColor.DARK_RED));
		
		UtilPlayer.message(caller, F.help("/c delete", "Delete your Clan", ChatColor.DARK_RED));
		
		UtilPlayer.message(caller, F.help("/c <clan>", "View Clan Information", ChatColor.WHITE));
	}
	
	public void create(final Player caller, final String[] args)
	{
//		System.out.println("CREATE COMMAND");
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "create", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClientClan client = Plugin.Get(caller);
		
		if (Plugin.getClanMemberUuidMap().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already in a Clan."));
			return;
		}
		
		if (Plugin.leftRecently(caller.getUniqueId(), 5 * 60 * 1000) != null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot create a Clan for " + C.mTime + UtilTime.MakeStr(Plugin.leftRecently(caller.getUniqueId(), 20 * 60 * 1000).getRight()) + C.mBody + "."));
			return;
		}
		
		/*
		 * TODO if (!client.canJoin()) { _manager.getTutorials().sendTutorialMsg(caller,
		 * F.main("Clans", "You cannot join a Clan for " + C.mTime +
		 * UtilTime.convertString(System.currentTimeMillis() -
		 * client.getDelay(), 1, TimeUnit.FIT) + C.mBody + ".")); return; }
		 */
		
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan name."));
			return;
		}
		
		if (!UtilInput.valid(args[1]))
		{
			UtilPlayer.message(caller, F.main("Clans", "Invalid characters in Clan name."));
			return;
		}
		
		if (args[1].length() < Plugin.getNameMin())
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name too short. Minimum length is " + (Plugin.getNameMin()) + "."));
			return;
		}
		
		if (args[1].length() > Plugin.getNameMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name too long. Maximum length is " + (Plugin.getNameMax()) + "."));
			return;
		}
		
		if (Plugin.getChat().filterMessage(caller, args[1]).contains("*"))
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name inappropriate. Please try a different name"));
			return;
		}
		
		if (!Plugin.getBlacklist().allowed(args[1]))
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan name is blacklisted! Please try a different name."));
			return;
		}
		
		Plugin.getClanDataAccess().clanExists(args[1], new Callback<Boolean>()
		{
			@Override
			public void run(Boolean clanExists)
			{
				if (clanExists)
				{
					UtilPlayer.message(caller, F.main("Clans", "Clan name is already in use by another Clan."));
				}
				else
				{
					Plugin.getClanDataAccess().createAndJoin(caller, args[1], new Callback<ClanInfo>()
					{
						@Override
						public void run(ClanInfo data)
						{
							if (data == null)
							{
								// Hopefully shouldn't happen!
								UtilPlayer.message(caller, F.main("Clans", "There was an error creating the clan. Please try again"));
							}
							else
							{
								UtilPlayer.message(caller, F.main("Clans", "You created Clan " + C.cYellow + data.getName() + C.cGray + "."));
							}
						}
					});
				}
			}
		});
	}
	
	public void delete(final Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "disband", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		Plugin.getClanUtility().delete(caller);
	}
	
	public void invite(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "invite", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (Plugin.getTutorial().inTutorial(caller))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot invite others while in a tutorial."));
			return;
		}
		
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input an invitee."));
			return;
		}
		
		Player target = UtilPlayer.searchOnline(caller, args[1], true);
		if (target == null || _clansManager.getIncognitoManager().Get(target).Status) return;

		Plugin.getClanUtility().invite(caller, clan, target);
	}
	
	public void stuck(final Player caller)
	{
//		if (_clansManager.getNetherManager().isInNether(caller))
//		{
//			_clansManager.message(caller, "You are not allowed to free yourself in " + F.clansNether("The Nether") + ".");
//			
//			return;
//		}
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "stuck");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		if (DelayedTask.Instance.HasTask(caller, "Spawn Teleport"))
		{
			_clansManager.message(caller, "You are already unsticking yourself.");
			
			return;
		}
		
		ClanTerritory territory = Plugin.getClanUtility().getClaim(caller.getLocation());
		
		String clanName = Plugin.getClanUtility().getClanByPlayer(caller) == null ? null : Plugin.getClanUtility().getClanByPlayer(caller).getName();
		
		if (territory == null || territory.Safe || territory.Owner.equals(clanName))
		{
			UtilPlayer.message(caller, F.main("Clans", "You must be in another Clan's territory to use this."));
			return;
		}
		
		DelayedTask.Instance.doDelay(caller, "Wilderness Teleport", new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient player)
			{
				// Do
				
				Location loc = getWildLoc(player.getPlayer().getLocation());
				
				if (loc == null)
				{
					UtilPlayer.message(caller, F.main("Clans", "Error whilst finding location to teleport to."));
					return;
				}
				
				player.getPlayer().teleport(loc);
				
				// Inform
				UtilPlayer.message(caller, F.main("Clans", "You have been teleported to the Wilderness."));
			}
		}, new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient client)
			{
				UtilTextMiddle.display("", "Teleporting to Wilderness in " + F.time(UtilTime.MakeStr(Math.max(0, client.getTimeLeft("Wilderness Teleport")))), 0, 5, 0, client.getPlayer());
			}
		}, new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient client)
			{
				UtilPlayer.message(client.getPlayer(), F.main("Clans", "Teleport has been cancelled due to movement."));
			}
		}, 2 * 60 * 1000, false);
		
	}
	
	public Location getWildLoc(Location origin)
	{
		Chunk wildLoc = origin.getChunk();

		List<Chunk> worldChunks = Lists.newArrayList(origin.getWorld().getLoadedChunks());
		
		Collections.sort(
				worldChunks, 
				(c1, c2) ->
				(int) ((int)
						origin.distance(
								UtilBlock.getHighest(origin.getWorld(), 
										c1.getBlock(7, origin.getBlockY(), 7).getLocation()
								).getLocation())
					  -
					    origin.distance(
							    UtilBlock.getHighest(origin.getWorld(),
							    		c2.getBlock(7, origin.getBlockY(), 7).getLocation()
							    ).getLocation())
				)
		);
		
		for (Chunk chunk : worldChunks)
		{
			if (Plugin.getClanUtility().getClaim(chunk) == null)
			{
				return chunk.getBlock(6, UtilBlock.getHighest(origin.getWorld(), chunk.getBlock(6, 0, 6).getLocation()).getY(), 6).getLocation();
			}
		}
		
		return null;
	}
	
	public void join(final Player caller, String[] args)
	{
		ClansCommandExecutedEvent cevent = new ClansCommandExecutedEvent(caller, "join", args);
		UtilServer.getServer().getPluginManager().callEvent(cevent);
		
		if (cevent.isCancelled())
		{
			return;
		}
		
		if (Plugin.getClanMemberUuidMap().containsKey(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already in a Clan."));
			return;
		}
		
		if (Plugin.leftRecently(caller.getUniqueId(), 20 * 60 * 1000) != null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + C.mTime + UtilTime.MakeStr(Plugin.leftRecently(caller.getUniqueId(), 20 * 60 * 1000).getRight()) + C.mBody + "."));
			return;
		}
		
		if (!Plugin.Get(caller).canJoin())
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot join a Clan for " + C.mTime + UtilTime.convertString(System.currentTimeMillis() - Plugin.Get(caller).getDelay(), 1, TimeUnit.FIT) + C.mBody + "."));
			return;
		}
		
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan name."));
			return;
		}
		
		if (!UtilInput.valid(args[1]))
		{
			UtilPlayer.message(caller, F.main("Clans", "Invalid characters in Clan name."));
			return;
		}
		
		final ClanInfo clan = Plugin.getClanUtility().searchClanPlayer(caller, args[1], true);
		if (clan == null) return;
		
		if (!clan.isInvited(caller.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not invited to " + F.elem("Clan " + clan.getName()) + "."));
			return;
		}
		
		if (clan.getSize() >= clan.getMaxSize())
		{
			UtilPlayer.message(caller, F.main("Clans", "The clan " + F.elem("Clan " + clan.getName()) + " is full and cannot be joined!"));
			return;
		}
		
		if (clan.getAllies() > clan.getAlliesMaxWithMemberCountOf(clan.getSize() + 1))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot join " + F.elem("Clan " + clan.getName()) + " until they remove some allies!"));
			return;
		}

		ClanJoinEvent event = new ClanJoinEvent(clan, caller);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		
		// Task
		Plugin.getClanDataAccess().join(clan, caller, ClanRole.RECRUIT, new Callback<Boolean>()
		{
			@Override
			public void run(Boolean data)
			{
				if (data)
				{
					// Inform
					UtilPlayer.message(caller, F.main("Clans", "You joined " + F.elem("Clan " + clan.getName()) + "."));
					clan.inform(F.name(caller.getName()) + " has joined your Clan.", caller.getName());
				}
				else
				{
					UtilPlayer.message(caller, F.main("Clans", "There was an error processing your request"));
				}
			}
		});
		
	}
	
	public void leave(final Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "leave");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		final ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (clan.getMembers().get(caller.getUniqueId()).getRole() == ClanRole.LEADER && clan.getMembers().size() > 1)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must pass on " + F.elem("Leadership") + " before leaving."));
			return;
		}
		
		// Leave or Delete
		if (clan.getMembers().size() > 1)
		{
			// Task
			Plugin.getClanDataAccess().leave(clan, caller, new Callback<Boolean>()
			{
				@Override
				public void run(Boolean data)
				{
					// Inform
					UtilPlayer.message(caller, F.main("Clans", "You left " + F.elem("Clan " + clan.getName()) + "."));
					clan.inform(F.name(caller.getName()) + " has left your Clan.", null);
				}
			});
		}
		else
		{
			delete(caller, args);
		}
	}
	
	public void kick(final Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "kick", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		final ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
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
		
		final String target = UtilPlayer.searchCollection(caller, args[1], clan.getMemberNameSet(), "Clan Member", true);
		
		_clansManager.getClanUtility().kick(caller, target);
	}
	
	public void promote(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "promote", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
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
		
		final String targetName = UtilPlayer.searchCollection(caller, args[1], clan.getMemberNameSet(), "Clan Member", true);
		
		_clansManager.getClanUtility().promote(caller, targetName);
	}
	
	public void demote(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "demote", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
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
		
		final String targetName = UtilPlayer.searchCollection(caller, args[1], clan.getMemberNameSet(), "Clan Member", true);
		
		_clansManager.getClanUtility().demote(caller, targetName);
	}
	
	public void ally(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "ally", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo cA = Plugin.getClanUtility().getClanByPlayer(caller);
		
		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Alliances."));
			return;
		}
		
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to ally."));
			return;
		}
		
		ClanInfo cB = Plugin.getClanUtility().searchClanPlayer(caller, args[1], true);
		
		if (cB == null) return;
		
		if (cA.isSelf(cB.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot ally with yourself."));
			return;
		}
		
		if (cA.isAlly(cB.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already allies with Clan" + F.elem(cB.getName())) + ".");
			return;
		}
		
		if (cA.getAllies() >= cA.getAlliesMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "You cannot have any more allies."));
			return;
		}
		
		if (cB.getAllies() >= cB.getAlliesMax())
		{
			UtilPlayer.message(caller, F.main("Clans", "Clan " + F.elem(cB.getName()) + " cannot have any more allies."));
			return;
		}
		
		if (!Recharge.Instance.usable(caller, "AllyReq" + cB.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "Please do not spam alliance requests."));
			return;
		}
		
		if (cB.isRequested(cA.getName()))
		{
			// Task
			Plugin.getClanDataAccess().ally(cA, cB, caller.getName());
			
			// Inform
			UtilPlayer.message(caller, F.main("Clans", "You accepted alliance with Clan " + F.elem(cB.getName()) + "."));
			cA.inform(F.name(caller.getName()) + " accepted alliance with Clan " + F.elem(cB.getName()) + ".", caller.getName());
			cB.inform("Clan " + F.elem(cA.getName()) + " has accepted alliance with you.", null);
		}
		else
		{
			// Task
			Plugin.getClanDataAccess().requestAlly(cA, cB, caller.getName());
			
			// Inform
			UtilPlayer.message(caller, F.main("Clans", "You requested alliance with Clan " + F.elem(cB.getName()) + "."));
			cA.inform(F.name(caller.getName()) + " has requested alliance with Clan " + F.elem(cB.getName()) + ".", caller.getName());
			cB.inform("Clan " + F.elem(cA.getName()) + " has requested alliance with you.", null);
			
			Recharge.Instance.use(caller, "AllyReq" + cB.getName(), java.util.concurrent.TimeUnit.MINUTES.toMillis(1), false, false);
		}
	}
	
//	public void trust(Player caller, String[] args)
//	{
//		ClanInfo cA = Plugin.getClan(Plugin.getClanMemberUuidMap().get(caller.getUniqueId()).getName());
//		
//		if (cA == null)
//		{
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "You are not in a Clan."));
//			return;
//		}
//		
//		if (cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
//		{
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Trust."));
//			return;
//		}
//		
//		if (args.length < 2)
//		{
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "You did not input a Clan to enemy."));
//			return;
//		}
//		
//		ClanInfo cB = Plugin.getClanUtility().searchClanPlayer(caller, args[1], true);
//		
//		if (cB == null) return;
//		
//		if (!cA.isAlly(cB.getName()))
//		{
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "You must be allied to trust a clan!"));
//			return;
//		}
//		
//		// Task
//		if (Plugin.getClanDataAccess().trust(cA, cB, caller.getName()))
//		{
//			// Inform
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "You gave trust to " + F.elem("Clan " + cB.getName()) + "."));
//			cA.inform(F.name(caller.getName()) + " has given trust to " + F.elem("Clan " + cB.getName()) + ".", caller.getName());
//			cB.inform(F.elem("Clan " + cA.getName()) + " has given trust to you.", null);
//		}
//		else
//		{
//			// Inform
//			_manager.getTutorials().sendTutorialMsg(caller, F.main("Clans", "You revoked trust to " + F.elem("Clan " + cB.getName()) + "."));
//			cA.inform(F.name(caller.getName()) + " has revoked trust to " + F.elem("Clan " + cB.getName()) + ".", caller.getName());
//			cB.inform(F.elem("Clan " + cA.getName()) + " has revoked trust to you.", null);
//		}
//	}
	
	public void neutral(Player caller, String[] args)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "neutral", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo cA = Plugin.getClanMemberUuidMap().get(caller.getUniqueId());
		
		if (cA == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
		if (cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && cA.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage relationships."));
			return;
		}
		
		if (args.length < 2)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a Clan to set neutrality with."));
			return;
		}
		
		ClanInfo cB = Plugin.getClanUtility().searchClanPlayer(caller, args[1], true);
		
		if (cB == null) return;
		
		if (cB.isSelf(cA.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You prefer to think of yourself positively..."));
		}
		else if (cB.isNeutral(cA.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You are already neutral with " + F.elem("Clan " + cB.getName()) + "."));
		}
		else if (cB.isAlly(cA.getName()))
		{
			// Task
			Plugin.getClanDataAccess().neutral(cA, cB, caller.getName(), true);
			
			// Inform
			UtilPlayer.message(caller, F.main("Clans", "You revoked alliance with " + F.elem("Clan " + cB.getName()) + "."));
			cA.inform(F.name(caller.getName()) + " revoked alliance with " + F.elem("Clan " + cB.getName()) + ".", caller.getName());
			cB.inform(F.elem("Clan " + cA.getName()) + " has revoked alliance with you.", null);
		}
	}
	
	public void claim(Player caller, String args[])
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "claim", args);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
//		if (_clansManager.getNetherManager().isInNether(caller))
//		{
//			_clansManager.message(caller, "You are not allowed to claim territory in " + F.clansNether("The Nether") + ".");
//			
//			return;
//		}
		
		Plugin.getClanUtility().claim(caller);
	}
	
	public void unclaim(Player caller, String[] args)
	{
//		if (_clansManager.getNetherManager().isInNether(caller))
//		{
//			_clansManager.message(caller, "You are not allowed to unclaim territory in " + F.clansNether("The Nether") + ".");
//			
//			return;
//		}
		
		if (args.length > 1)
		{
			if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("a"))
			{
				unclaimall(caller);
				return;
			}
		}
		
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "unclaim");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		Plugin.getClanUtility().unclaim(caller, caller.getLocation().getChunk());
	}
	
	public boolean unclaimSteal(Player caller, ClanInfo clientClan, ClanInfo ownerClan)
	{
		if (!_clansManager.canUnclaimChunk(clientClan, ownerClan))
		{
			return false;
		}
		
		// Change Inform
		UtilPlayer.message(caller, F.main("Clans", "You can no longer 'steal' territory. " + "You simply unclaim it and it can not be reclaimed by anyone for 30 mintes." + "This was done to improve gameplay. Enjoy!"));
		
		// Inform
		UtilServer.broadcast(F.main("Clans", F.elem(clientClan.getName()) + " unclaimed from " + F.elem(ownerClan.getName()) + " at " + F.elem(UtilWorld.locToStrClean(caller.getLocation())) + "."));
		
		// Unclaim
		Plugin.getClanDataAccess().unclaim(ClaimLocation.of(caller.getLocation().getChunk()), caller.getName(), true);
		
		return true;
	}
	
	public void unclaimall(Player caller)
	{
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "unclaimall");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		Plugin.getClanUtility().unclaimAll(caller);
	}
	
//	public void map(Player caller, String[] args)
//	{
//		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "map");
//		UtilServer.getServer().getPluginManager().callEvent(event);
//		
//		if (event.isCancelled())
//		{
//			return;
//		}
//		
//		Plugin.getItemMapManager().setMap(caller);
//	}
	
	public void home(final Player caller, String[] args)
	{
		if (args.length > 1)
		{
			if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("s"))
			{
				homeSet(caller);
				return;
			}
		}
		
		final ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
		if (clan == null)
		{
			//teleport to spawn point
			Spawn.getInstance().getSpawnLocation();			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			
			return;
		}
		
		if (clan.getHome() == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan has not set a Home"));
			return;
		}
		
		if (!clan.getClaimSet().contains(ClaimLocation.of(clan.getHome().getChunk())))
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan has lost its Home Territory."));
			return;
		}
		
		Location home = clan.getHome();
		
		if (!(home.getBlock().getType().equals(Material.BED_BLOCK) && home.add(0, 1, 0).getBlock().getType().equals(Material.AIR)) && home.add(0, 2, 0).getBlock().getType().equals(Material.AIR))
		{
			UtilPlayer.message(caller, F.main("Clans", "Your Clan's bed has been destroyed, or is obstructed."));
			return;
		}
		
		/*
		 * CoreClient client = _plugin.getClientManager().Get(caller); for
		 * (Player cur : clan.GetHome().getWorld().getPlayers()) if
		 * (client.Clan().GetRelation(cur.getName()) == ClanRelation.NEUTRAL) if
		 * (clan.GetClaimSet().contains(UtilWorld.chunkToStr(cur.getLocation().
		 * getChunk()))) { _manager.getTutorials().sendTutorialMsg(caller, F.main("Clans",
		 * "You cannot use Clan Home with enemies in your Territory.")); return;
		 * }
		 */
		
		if (!Recharge.Instance.usable(caller, "Home Teleport", true))
		{
			return;
		}
		
		if (!UtilTime.elapsed(_clansManager.getCombatManager().getLog(caller).GetLastCombatEngaged(), 15000))
		{
			long remain = _clansManager.getCombatManager().getLog(caller).GetLastCombatEngaged() + 15000 - System.currentTimeMillis();
			UtilPlayer.message(caller, F.main("Clans", "You are combat tagged for " + F.elem(UtilTime.MakeStr(remain)) + "!"));
			return;
		}
		
		// Event
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "tphome");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		DelayedTask.Instance.doDelay(caller, "Home Teleport", new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient player)
			{
				Recharge.Instance.use(caller, "Home Teleport", 30 * 60 * 1000, true, false);
				
				// Do
				Plugin.getTeleport().TP(caller, clan.getHome().add(0, 1, 0));
				
				// Inform
				UtilPlayer.message(caller, F.main("Clans", "You teleported to your Clan Home " + UtilWorld.locToStrClean(caller.getLocation()) + "."));
			}
		}, new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient client)
			{
				UtilTextMiddle.display("", "Teleporting to Clan Home in " + F.time(UtilTime.MakeStr(Math.max(0, client.getTimeLeft("Home Teleport")))), 0, 5, 0, client.getPlayer());
			}
		}, new Callback<DelayedTaskClient>() {
			public void run(DelayedTaskClient client)
			{
				UtilPlayer.message(client.getPlayer(), F.main("Clans", "Teleport has been cancelled due to movement."));
			}
		}, 15 * 1000, false);
	}
	
	public void homeSet(Player caller)
	{
		ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
		
		// Event
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "homeset");
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		if (clan == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			return;
		}
		
//		if (_clansManager.getNetherManager().isInNether(caller))
//		{
//			_clansManager.message(caller, "You are not allowed to set your Clan Home in " + F.clansNether("The Nether") + ".");
//			
//			return;
//		}
		
		if (clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.LEADER && clan.getMembers().get(caller.getUniqueId()).getRole() != ClanRole.ADMIN)
		{
			UtilPlayer.message(caller, F.main("Clans", "Only the Clan Leader and Admins can manage Clan Home."));
			return;
		}
		
		if (Plugin.getClanUtility().getOwner(caller.getLocation()) == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You must set your Clan Home in your own Territory."));
			return;
		}
		
		if (!Plugin.getClanUtility().getOwner(caller.getLocation()).isSelf(clan.getName()))
		{
			UtilPlayer.message(caller, F.main("Clans", "You must set your Clan Home in your own Territory."));
			return;
		}
		
		if (!(caller.getLocation().add(0, 1, 0).getBlock().getType().equals(Material.AIR) && caller.getLocation().add(0, 2, 0).getBlock().getType().equals(Material.AIR)))
		{
			UtilPlayer.message(caller, F.main("Clans", "This is not a suitable place for a bed."));
			return;
		}
		
		// Place New
		boolean bedPlaced = UtilBlock.placeBed(caller.getLocation(), BlockFace.valueOf(EnumDirection.fromAngle(caller.getLocation().getYaw()).name()), false, false);
		
		if (!bedPlaced)
		{
			UtilPlayer.message(caller, F.main("Clans", "This is not a suitable place for a bed."));
			return;
		}
		
		// Cleanup old
		if (clan.getHome() != null)
		{
			System.out.println("<-old bed cleanup-> <--> " + UtilBlock.deleteBed(clan.getHome()));
		}
		
		// Task
		Plugin.getClanDataAccess().setHome(clan, caller.getLocation(), caller.getName());
		
		Plugin.ClanTips.displayTip(TipType.SETHOME, caller);
		
		// Inform
		UtilPlayer.message(caller, F.main("Clans", "You set Clan Home to " + F.elem(UtilWorld.locToStrClean(caller.getLocation())) + "."));
		clan.inform(caller.getName() + " set Clan Home to " + F.elem(UtilWorld.locToStrClean(caller.getLocation())) + ".", caller.getName());
	}
	
	public void infoClan(Player caller, String search)
	{
//		System.out.println(search);
		
		if (search == null)
		{
			UtilPlayer.message(caller, F.main("Clans", "You did not input a search parameter."));
			return;
		}
		
		ClansCommandExecutedEvent event = new ClansCommandExecutedEvent(caller, "info", search);
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		ClanInfo clan = Plugin.getClanUtility().searchClanPlayer(caller, search, true);
		if (clan == null) return;
		
		_clansManager.getClanShop().openClanWho(caller, clan);
	}
}