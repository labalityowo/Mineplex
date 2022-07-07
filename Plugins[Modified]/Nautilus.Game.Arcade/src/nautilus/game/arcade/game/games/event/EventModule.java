package nautilus.game.arcade.game.games.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.creature.event.CreatureKillEntitiesEvent;
import mineplex.core.event.StackerEvent;
import mineplex.core.gadget.gadgets.morph.MorphBlock;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.give.Give;
import mineplex.core.punish.PunishClient;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.games.bridge.Bridge;
import nautilus.game.arcade.game.games.smash.SuperSmash;
import nautilus.game.arcade.kit.Kit;

public class EventModule extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SET_HOST_COMMAND,
	}

	private ArcadeManager Manager;
	
	private NautHashMap<PotionEffectType, Long> _potionEffectsDuration = new NautHashMap<>();
	private NautHashMap<PotionEffectType, Integer> _potionEffectsMult = new NautHashMap<>();
	
	private boolean _mobGriefing;
	
	private HashSet<Entity> _tempStackShift = new HashSet<Entity>();
	
	private ArrayList<Player> _stacker;
	private ArrayList<Player> _damage;
	private boolean _allowStacker;
	
	private boolean _keepInventory;
	
	public EventModule(ArcadeManager manager, JavaPlugin plugin)
	{
		super("EventModule", plugin);
		Manager = manager;
		_mobGriefing = true;
		_stacker = new ArrayList<>();
		_damage = new ArrayList<>();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.SET_HOST_COMMAND, true, true);
		if (_plugin.getConfig().getString("serverstatus.name").equals("SMTestServer-1"))
		{
			PermissionGroup.SRMOD.setPermission(Perm.SET_HOST_COMMAND, true, true);
		}
	}
	
	@EventHandler
	public void mobGriefing(EntityChangeBlockEvent event)
	{
		if(!_mobGriefing)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void setHostDebug(PlayerCommandPreprocessEvent event)
	{
		if (!event.getMessage().toLowerCase().startsWith("/sethost "))
			return;
		
		if (!Manager.GetClients().Get(event.getPlayer()).hasPermission(Perm.SET_HOST_COMMAND))
		{
			return;
		}
				
		Manager.GetServerConfig().HostName = event.getMessage().split(" ")[1];

		event.getPlayer().sendMessage("Set host to: " + event.getMessage().split(" ")[1]);

		Manager.GetGameHostManager().setHost(Bukkit.getPlayerExact(Manager.GetServerConfig().HostName));
		if (Manager.GetGameHostManager().getHost() != null)
		{
			Manager.GetGameHostManager().setHostRank(Manager.GetClients().Get(Manager.GetGameHostManager().getHost()).getPrimaryGroup());
		}
		
		Manager.GetGameHostManager().setDefaultConfig();
		
		event.setCancelled(true);
	}
	
	public void commandHelp(Player player) 
	{
		UtilPlayer.message(player, F.main("Event", "Displaying Commands;"));

		UtilPlayer.message(player, F.value("/e settings", "View Settings Help"));

		UtilPlayer.message(player, F.value("/e tp <Player>", "Teleport to Target"));
		UtilPlayer.message(player, F.value("/e tp here <Player>", "Teleport Target to Self"));
		UtilPlayer.message(player, F.value("/e tp here all", "Teleport Everyone to Self"));

		UtilPlayer.message(player, F.value("/e gadget", "Toggle Gadgets"));
		UtilPlayer.message(player, F.value("/e gadget list", "Lists Gadgets (Shows Whitelist)"));
		UtilPlayer.message(player, F.value("/e gadget <Gadget>", "Toggles Whitelist for Gadget"));
		UtilPlayer.message(player, F.value("/e gadget clear", "Clears Gadget Whitelist"));

		UtilPlayer.message(player, F.value("/e silence [Time]", "Silence Chat"));

		UtilPlayer.message(player, F.value("/e admin [Player]", "Toggle Event Admin"));

		UtilPlayer.message(player, F.value("/e gm [Player]", "Toggle Creative Mode"));

		UtilPlayer.message(player, F.value("/e radius [Radius]", "Set Forcefield Radius"));

		UtilPlayer.message(player, F.value("/e give <item> <amount>", "Give Item"));
		UtilPlayer.message(player, F.value("/e give <player> <item> <amount> [e:#,e:#...]", "Give Item"));
		
		UtilPlayer.message(player, F.value("/e doublejump", "Toggles Double Jump"));
		UtilPlayer.message(player, F.value("/e bc", "Broadcast a message with colorcodes"));
		UtilPlayer.message(player, F.value("/e tempgadget", "Activates gadget for all player"));
		UtilPlayer.message(player, F.value("/e stacker [Player]", "toggles stacker global or for Players"));
		UtilPlayer.message(player, F.value("/e kick", "Remove a player from the event"));
		UtilPlayer.message(player, F.value("/e area PVP|ALL|PVE|EVP|Gadget|Effect / add", "Create and edit areas"));

		UtilPlayer.message(player, F.value("/e scoreboard <Line #> [Text]", "Sets Scoreboard Text"));

		UtilPlayer.message(player, F.value("/e mob <type> [#Amount] n[Name] s[Size] [angry] [baby]", ""));
		UtilPlayer.message(player, F.value("/e mob kill <type>", "Kill Mobs"));

		UtilPlayer.message(player, F.value("/e kit set", "Sets Player Kit to your Hotbar"));
		UtilPlayer.message(player, F.value("/e kit apply", "Gives Kit to Players"));
		UtilPlayer.message(player, F.value("/e kit clear", "Gives Kit to Players"));

		UtilPlayer.message(player, F.value("/e effect <player> <type> <mult> <seconds>", ""));
		UtilPlayer.message(player, F.value("/e effect <player> clear", ""));

		UtilPlayer.message(player, F.value("/e announce <text>", "Broadcasts a 1.8 announcement"));
		UtilPlayer.message(player, F.value("/e clear <player>", "Clears player’s inventory"));
		UtilPlayer.message(player, F.value("/e clear all", "Clears all inventories"));
		UtilPlayer.message(player, F.value("/e bridge", "Forces Bridges countdown to 10 seconds"));
		UtilPlayer.message(player, F.value("/e revive", "Revive a Player"));
		UtilPlayer.message(player, F.value("/e keepinventory", ""));
		UtilPlayer.message(player, F.value("/e gamekit", "Change kit of a Player"));
		UtilPlayer.message(player, F.value("/e smash", "spawn smash Crystal in Smash"));
		UtilPlayer.message(player, F.value("/e dm", "start dm in SG"));
		UtilPlayer.message(player, F.value("/e supplydrop", "spawn supply Drop in SG"));
		UtilPlayer.message(player, F.value("/ea", "Talk in event chat"));
	}

	public void commandHelpSettings(Player player) 
	{
		UtilPlayer.message(player, F.main("Event", "Displaying Settings Commands;"));
		UtilPlayer.message(player, F.value("/e damage all", "Toggles All Damage"));
		UtilPlayer.message(player, F.value("/e damage pvp", "Toggles PvP Damage"));
		UtilPlayer.message(player, F.value("/e damage pve", "Toggles PvE Damage"));
		UtilPlayer.message(player, F.value("/e damage pve", "Toggles EvP Damage"));
		UtilPlayer.message(player, F.value("/e damage fall", "Toggles Fall Damage"));
		UtilPlayer.message(player, F.value("/e health <-1 to 20>", "Locks Players Health"));
		UtilPlayer.message(player, F.value("/e hunger <-1 to 20>", "Locks Players Hunger"));
		UtilPlayer.message(player, F.value("/e item drop", "Toggles Item Drop"));
		UtilPlayer.message(player, F.value("/e item pickup", "Toggles Item Pickup"));
		UtilPlayer.message(player, F.value("/e blockplacecreative", "Toggles Block Placing in Creative (On/Off)"));
		UtilPlayer.message(player, F.value("/e blockbreakcreative", "Toggles Block Breaking in Creative (On/Off)"));
		UtilPlayer.message(player, F.value("/e blockplace", "Toggles Block Placing (On/Off)"));
		UtilPlayer.message(player, F.value("/e blockplace", "Toggles Block Placing (On/Off)"));
		UtilPlayer.message(player, F.value("/e blockplace whitelist <add/remove/list/clear> <id>", ""));
		UtilPlayer.message(player, F.value("/e blockplace blacklist <add/remove/list/clear> <id>", ""));
		UtilPlayer.message(player, F.value("/e blockbreak", "Toggles Block Breaking (On/Off)"));
		UtilPlayer.message(player, F.value("/e blockbreak whitelist <add/remove/list/clear> <id>", ""));
		UtilPlayer.message(player, F.value("/e blockbreak blacklist <add/remove/list/clear> <id>", ""));
		UtilPlayer.message(player, F.value("/e time <-1 to 24000>", "Sets World Time"));
		UtilPlayer.message(player, F.value("/e mobgriefing", "toggles mobgriefing in games"));
	}

	//Command Handler
	@EventHandler(priority = EventPriority.LOWEST)
	public void commandHandler(PlayerCommandPreprocessEvent event)
	{	
		if (Manager.GetGame() == null)
			return;
		
		boolean eventTestServer = _plugin.getConfig().getString("serverstatus.name").equalsIgnoreCase("SMTestServer-1");
		
		if(!Manager.GetGameHostManager().isEventServer() && !eventTestServer)
			return;
		
		//Trim off /e and split to args
		String[] args = event.getMessage().substring(3, event.getMessage().length()).split(" ");
		
		if (event.getMessage().toLowerCase().startsWith("/ea"))
		{
			commandEventChat(event.getPlayer(), args);
			event.setCancelled(true);
		}

		if (!event.getMessage().toLowerCase().startsWith("/e "))
			return;

		if (!Manager.GetGameHostManager().isAdmin(event.getPlayer(), false))
			return;

		event.setCancelled(true);

		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			commandHelp(event.getPlayer());
		}
		else if (args[0].equalsIgnoreCase("settings"))
		{
			if (args.length >= 2 && args[1].equalsIgnoreCase("list"))
				listSettings(event.getPlayer());
			else
				commandHelpSettings(event.getPlayer());
		}

		else if (args[0].equalsIgnoreCase("tp"))
		{
			commandTeleport(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("gadget"))
		{
			commandGadget(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("silence"))
		{
			commandSilence(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("admin"))
		{
			commandAdmin(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("whitelist"))
		{
			commandWhitelist(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("give"))
		{
			commandGive(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("effect"))
		{
			commandEffect(event.getPlayer(), args, null);
		}
		else if (args[0].equalsIgnoreCase("mob"))
		{
			if (args.length >= 2 && args[1].equalsIgnoreCase("kill"))
				commandMobKill(event.getPlayer(), args);
			else
				commandMob(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("bc"))
		{
			commandBC(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("kick"))
		{
			if(Bukkit.getPlayer(args[1]) == null)
			{
				UtilPlayer.message(event.getPlayer(), "No matches for: " + C.cYellow + args[1]);
				return;	
			}
			if (Manager.GetGameHostManager().getBlacklist().contains(Bukkit.getPlayer(args[1]).getName()))
			{
				Manager.GetGameHostManager().getBlacklist().remove(Bukkit.getPlayer(args[1]).getName());
			}
			else
			{
				Manager.GetGameHostManager().getBlacklist().add(Bukkit.getPlayer(args[1]).getName());
				UtilPlayer.message(event.getPlayer(), F.main("Event", "You removed " + C.cYellow + Bukkit.getPlayer(args[1]).getName()));
			}	
		}
		else if(args[0].equalsIgnoreCase("tempgadget"))
		{
			String gadget = args[1];
			for(int e = 2; e < args.length; e++)
				gadget+= " " + args[e];
			
			try
			{
				for(Player target : UtilServer.getPlayers())
					Manager.GetDonation().Get(target).addOwnedUnknownSalesPackage(gadget);
			}
			catch (Exception e)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "Gadget is not vallid"));
			}
			UtilPlayer.message(event.getPlayer(), F.main("Event", "You gave the gadget " + F.item(gadget) + " to all Players!"));
		}
		else if (args[0].equalsIgnoreCase("announce"))
		{
			String text = args[1];
			
			for (int i = 2; i < args.length; i++)
			{
				text += " " + args[i];
			}
			
			UtilTextMiddle.display(C.cDGreenB + "Announcement", text);
			UtilServer.broadcast(F.main("Event Announcement", text));
		}
		
		if (!Manager.GetGame().InProgress())
			return;

		//XXX Commands
		else if (args[0].equalsIgnoreCase("gm"))
		{
			commandGamemode(event.getPlayer(), args);
		}else if (args[0].equalsIgnoreCase("gamekit"))
		{
			commandGameKit(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("keepinventory"))
		{
			commandKeepInventory(event.getPlayer());
		}
		else if (args[0].equalsIgnoreCase("radius"))
		{
			commandForcefieldRadius(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("doublejump"))
		{
			commandDoubleJump(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("scoreboard"))
		{
			commandScoreboard(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("kit"))
		{
			commandKit(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("revive"))
		{
			commandRevive(event.getPlayer(), args);
		}
		

		//XXX Settings
		else if (event.getMessage().toLowerCase().equals("/e damage all"))
		{
			Manager.GetGame().Damage = !Manager.GetGame().Damage;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Damage All", F.tf(Manager.GetGame().Damage))));
		}
		else if (event.getMessage().toLowerCase().equals("/e damage pvp"))
		{
			Manager.GetGame().DamagePvP = !Manager.GetGame().DamagePvP;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Damage PvP", F.tf(Manager.GetGame().DamagePvP))));
		}
		else if (event.getMessage().toLowerCase().equals("/e damage pve"))
		{
			Manager.GetGame().DamagePvE = !Manager.GetGame().DamagePvE;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Damage PvE", F.tf(Manager.GetGame().DamagePvE))));
		}
		else if (event.getMessage().toLowerCase().equals("/e damage evp"))
		{
			Manager.GetGame().DamageEvP = !Manager.GetGame().DamageEvP;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Damage EvP", F.tf(Manager.GetGame().DamageEvP))));
		}
		else if (event.getMessage().toLowerCase().equals("/e damage fall"))
		{
			Manager.GetGame().DamageFall = !Manager.GetGame().DamageFall;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Damage Fall", F.tf(Manager.GetGame().DamageFall))));
		}
		else if (args[0].equalsIgnoreCase("health"))
		{
			commandHealth(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("hunger"))
		{
			commandHunger(event.getPlayer(), args);
		}
		else if (event.getMessage().toLowerCase().equals("/e item drop"))
		{
			Manager.GetGame().ItemDrop = !Manager.GetGame().ItemDrop;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Item Drop", F.tf(Manager.GetGame().ItemDrop))));
		}
		else if (event.getMessage().toLowerCase().equals("/e item pickup"))
		{
			Manager.GetGame().ItemPickup = !Manager.GetGame().ItemPickup;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Item Pickup", F.tf(Manager.GetGame().ItemPickup))));
		}
		else if (event.getMessage().toLowerCase().equals("/e blockplace"))
		{
			Manager.GetGame().BlockPlace = !Manager.GetGame().BlockPlace;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Block Place", F.tf(Manager.GetGame().BlockPlace))));
		}
		else if (args.length >= 4 && args[0].equalsIgnoreCase("blockplace") 
				&& (args[1].equalsIgnoreCase("whitelist") || args[1].equalsIgnoreCase("blacklist")))
		{
			commandBlockPlace(event.getPlayer(), args, args[1].equalsIgnoreCase("whitelist"), args[2]);
		}
		else if (event.getMessage().toLowerCase().equals("/e blockbreak"))
		{
			Manager.GetGame().BlockBreak = !Manager.GetGame().BlockBreak;
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Block Break", F.tf(Manager.GetGame().BlockBreak))));
		}
		else if (args.length >= 4 && args[0].equalsIgnoreCase("blockbreak") 
				&& (args[1].equalsIgnoreCase("whitelist") || args[1].equalsIgnoreCase("blacklist")))
		{
			commandBlockBreak(event.getPlayer(), args, args[1].equalsIgnoreCase("whitelist"), args[2]);
		}
		else if (args[0].equalsIgnoreCase("time"))
		{
			commandTime(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("blockplacecreative"))
		{
			commandBlockPlaceInCreative(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("blockbreakcreative"))
		{
			commandBlockBreakInCreative(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("stacker"))
		{
			commandStacker(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("playerdamage"))
		{
			if(args[1] == null)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "Insufficient arguments!"));
				return;
			}
			
			if(Bukkit.getPlayer(args[1]) == null)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "This Player is not online!"));
				return;
			}
				
			if(_damage.contains(Bukkit.getPlayer(args[1])))
				_damage.remove(Bukkit.getPlayer(args[1]));
			else
				_damage.add(Bukkit.getPlayer(args[1]));
		}
		else if(args[0].equalsIgnoreCase("area"))
		{
			if(!(Manager.GetGame() instanceof EventGame)) {
				UtilPlayer.message(event.getPlayer(), F.main("Settings", "You can only edit areas in the Event game!"));
				return;
			}
			
			((EventGame) Manager.GetGame()).editArea(event.getPlayer(), args);
		}
		else if(args[0].equalsIgnoreCase("mobgriefing"))
		{
			commandMobGriefing(event.getPlayer(), args);
		}
		else if (args[0].equalsIgnoreCase("clear"))
		{
			String playerName = args[1];
			
			if (playerName.equalsIgnoreCase("@a"))
			{
				for (Player player : UtilServer.getPlayers())
				{
					UtilInv.Clear(player);
				}
				
				UtilPlayer.message(event.getPlayer(), F.main("Event", "Cleared everyone's inventory!"));
			}
			else
			{
				Player player = Bukkit.getPlayer(args[1]);
				
				if (player == null)
				{
					UtilPlayer.message(event.getPlayer(), "No matches for: " + F.elem(args[1]));
				}
				else
				{
					UtilInv.Clear(player);
					UtilPlayer.message(event.getPlayer(), F.main("Event", "Cleared " + F.elem(player.getName() + "'s") + " inventory!"));
				}
			}
		}
		else if (args[0].equalsIgnoreCase("bridge"))
		{
			if (!(Manager.GetGame() instanceof Bridge))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "You can only drop the bridges in Bridges!"));
				return;
			}
			
			if (((Bridge) Manager.GetGame()).isBridgesDown())
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "The bridges have already dropped!"));
				return;
			}
			
			int seconds = 10;
			
			if (args.length > 1)
			{
				try
				{
					seconds = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException e)
				{
					UtilPlayer.message(event.getPlayer(), F.main("Event", "Invalid integer for seconds!"));
					return;
				}
			}
			
			if (seconds < 0)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "Seconds must be greater than 0!"));
				return;
			}
			
			((Bridge) Manager.GetGame()).setBridgeTime((int) ((System.currentTimeMillis() - Manager.GetGame().GetStateTime()) + seconds * 1000));
			UtilPlayer.message(event.getPlayer(), F.main("Event", "Bridges will drop in " + F.elem(seconds + " Seconds") + "!"));
		}
		else if (args[0].equalsIgnoreCase("smash"))
		{
			if (!(Manager.GetGame() instanceof SuperSmash))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "You can only add a Smash Crystal in Smash!"));
				return;
			}
			
			((SuperSmash) Manager.GetGame()).setNextPowerupTime(100);
		}
	}

	public void listSettings(Player player) 
	{
		UtilPlayer.message(player, F.value("Damage All", F.tf(Manager.GetGame().Damage)));
		UtilPlayer.message(player, F.value("Damage PvP", F.tf(Manager.GetGame().DamagePvP)));
		UtilPlayer.message(player, F.value("Damage PvE", F.tf(Manager.GetGame().DamagePvE)));
		UtilPlayer.message(player, F.value("Damage EvP", F.tf(Manager.GetGame().DamageEvP)));
		UtilPlayer.message(player, F.value("Damage Fall", F.tf(Manager.GetGame().DamageFall)));
		UtilPlayer.message(player, F.value("Health Set", Manager.GetGame().HealthSet+""));
		UtilPlayer.message(player, F.value("Hunger Set", Manager.GetGame().HungerSet+""));
		UtilPlayer.message(player, F.value("Item Pickup", F.tf(Manager.GetGame().ItemPickup)));
		UtilPlayer.message(player, F.value("Item Drop", F.tf(Manager.GetGame().ItemDrop)));
		UtilPlayer.message(player, F.value("Block Place Creative", F.tf(Manager.GetGame().BlockPlaceCreative)));
		UtilPlayer.message(player, F.value("Block Break Creative", F.tf(Manager.GetGame().BlockBreakCreative)));
		UtilPlayer.message(player, F.value("Block Place", F.tf(Manager.GetGame().BlockPlace)));
		UtilPlayer.message(player, F.value("Block Place Whitelist", UtilText.listToString(Manager.GetGame().BlockPlaceAllow, true)));
		UtilPlayer.message(player, F.value("Block Place Blacklist", UtilText.listToString(Manager.GetGame().BlockPlaceDeny, true)));
		UtilPlayer.message(player, F.value("Block Break", F.tf(Manager.GetGame().BlockPlace)));
		UtilPlayer.message(player, F.value("Block Break Whitelist", UtilText.listToString(Manager.GetGame().BlockBreakAllow, true)));
		UtilPlayer.message(player, F.value("Block Break Blacklist", UtilText.listToString(Manager.GetGame().BlockBreakDeny, true)));
		UtilPlayer.message(player, F.value("Time Set", Manager.GetGame().WorldTimeSet+""));
		UtilPlayer.message(player, F.value("Mob griefing", F.tf(_mobGriefing)));
	}
	
	public void commandBlockBreakInCreative(Player player, String[] args)
	{
		Manager.GetGame().BlockBreakCreative = !Manager.GetGame().BlockBreakCreative;
		
		UtilPlayer.message(player, F.main("Settings", "BlockBreakCreative: " + F.tf(Manager.GetGame().BlockBreakCreative)));
	}
	
	public void commandKeepInventory(Player player)
	{
		_keepInventory = !_keepInventory;
		
		UtilPlayer.message(player, F.main("Settings", "KeepInventory: " + F.tf(_keepInventory)));
	}
	
	public void commandGameKit(Player player, String[] args)
	{
		Player target = player;
		String kitString = "";
		
		if (args.length >= 3)
		{
			Player newTarget = UtilPlayer.searchOnline(player, args[1], true);
			if (newTarget != null)
			{
				target = newTarget;
				kitString = args[2];
			}
			else
				return;
		}
		else
			return;
		
		Kit[] kits = Manager.GetGame().GetKits();
		Kit kit = null;
		for (Kit otherKit : kits)
			if (otherKit.GetName().replace(" ", "").equalsIgnoreCase(kitString))
				kit = otherKit;
		
		if (kit == null)
		{
			UtilPlayer.message(player, F.main("Event", "No Kit found"));
			return;
		}
		
		Manager.GetGame().SetKit(target, kit, true, true);
		UtilPlayer.message(player, F.main("Event", "Kit [" + kit.GetName() + "] applied"));
	}
	
	public void commandEventChat(Player player, String[] args)
	{
		if (args.length == 1)
			return;
		
		PunishClient client = Manager.getPunishments().GetClient(player.getName());
		if (client != null && (client.IsBanned() || client.IsMuted()))
		{
			return;
		}
		
		String message = ChatColor.YELLOW + "Event Chat " + ChatColor.YELLOW + ChatColor.BOLD + player.getName() + " " + ChatColor.RESET + ChatColor.GOLD + F.combine(args, 1, null, false);
		UtilPlayer.message(player, message);
		for (Player other : UtilServer.getPlayers())
		{
			if (player == other)
				continue;
			
			if (Manager.GetGameHostManager().isAdmin(other, true))
				UtilPlayer.message(other, message);
		}
	}
	
	public void commandRevive(Player player, String[] args)
	{
		Player target = player;

		if (args.length >= 2)
		{
			Player newTarget = UtilPlayer.searchOnline(player, args[1], true);
			if (newTarget != null)
				target = newTarget;
			else
				return;
		}

		Manager.GetGame().SetPlayerState(target, PlayerState.IN);
		Manager.GetGame().RespawnPlayer(target);
		Manager.GetGame().GetScoreboard().setPlayerTeam(target, Manager.GetGame().GetTeam(target));
		UtilPlayer.message(target, F.main("Game", "You have been Respawned!"));
	}
	
	public void commandStacker(Player player, String[] args)
	{
		if(args.length == 1)
		{
			_allowStacker = !_allowStacker;
			UtilPlayer.message(player, F.main("Settings", "Stacker all: " + F.tf(_allowStacker)));
		}
		else
		{
			for(Player target : UtilPlayer.matchOnline(player, args[1], true))
			{
				if(_stacker.contains(target))
				{
					_stacker.remove(target);
					UtilPlayer.message(target, F.main("Settings", "Stacker: " + F.tf(false)));
					UtilPlayer.message(player, F.main("Settings", "Stacker " + target.getName() + ": " + F.tf(false)));
				}
				else
				{
					_stacker.add(target);
					UtilPlayer.message(target, F.main("Settings", "Stacker: " + F.tf(true)));
					UtilPlayer.message(player, F.main("Settings", "Stacker " + target.getName() + ": " + F.tf(true)));
				}
			}
		}
	}
	
	public void commandBlockPlaceInCreative(Player player, String[] args)
	{
		Manager.GetGame().BlockPlaceCreative = !Manager.GetGame().BlockPlaceCreative;
		
		UtilPlayer.message(player, F.main("Settings", "BlockPlaceCreative: " + F.tf(Manager.GetGame().BlockPlaceCreative)));
	}
	
	public void commandMobGriefing(Player player, String[] args)
	{
		_mobGriefing = !_mobGriefing;
		
		UtilPlayer.message(player, F.main("Settings", "Mob Griefing: " + F.tf(_mobGriefing)));
	}

	public void commandBlockPlace(Player player, String[] args, boolean whitelist, String command)
	{
		try
		{
			int blockId = Integer.parseInt(args[3]);

			if (whitelist)
			{
				if (command.equalsIgnoreCase("add"))
				{
					Manager.GetGame().BlockPlaceAllow.add(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Whitelist", "Added " + blockId)));
				}
				else if (command.equalsIgnoreCase("remove"))
				{
					Manager.GetGame().BlockPlaceAllow.remove(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Whitelist", "Removed " + blockId)));
				}
				else if (command.equalsIgnoreCase("clear"))
				{
					Manager.GetGame().BlockPlaceAllow.clear();
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Whitelist", "Cleared")));
				}
				else if (command.equalsIgnoreCase("list"))
				{
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Whitelist", UtilText.listToString(Manager.GetGame().BlockPlaceAllow, true))));
				}
			}
			else
			{
				if (command.equalsIgnoreCase("add"))
				{
					Manager.GetGame().BlockPlaceDeny.add(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Blacklist", "Added " + blockId)));
				}
				else if (command.equalsIgnoreCase("remove"))
				{
					Manager.GetGame().BlockPlaceDeny.remove(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Blacklist", "Removed " + blockId)));
				}
				else if (command.equalsIgnoreCase("clear"))
				{
					Manager.GetGame().BlockPlaceDeny.clear();
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Blacklist", "Cleared")));
				}
				else if (command.equalsIgnoreCase("list"))
				{
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Place Blacklist", UtilText.listToString(Manager.GetGame().BlockPlaceDeny, true))));
				}
			}

			return;
		}
		catch (Exception e)
		{

		}

		commandHelpSettings(player);
	}

	public void commandBlockBreak(Player player, String[] args, boolean whitelist, String command)
	{
		try
		{
			int blockId = Integer.parseInt(args[3]);

			if (whitelist)
			{
				if (command.equalsIgnoreCase("add"))
				{
					Manager.GetGame().BlockBreakAllow.add(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Whitelist", "Added " + blockId)));
				}
				else if (command.equalsIgnoreCase("remove"))
				{
					Manager.GetGame().BlockBreakAllow.remove(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Whitelist", "Removed " + blockId)));
				}
				else if (command.equalsIgnoreCase("clear"))
				{
					Manager.GetGame().BlockBreakAllow.clear();
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Whitelist", "Cleared")));
				}
				else if (command.equalsIgnoreCase("list"))
				{
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Whitelist", UtilText.listToString(Manager.GetGame().BlockBreakAllow, true))));
				}
			}
			else
			{
				if (command.equalsIgnoreCase("add"))
				{
					Manager.GetGame().BlockBreakDeny.add(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Blacklist", "Added " + blockId)));
				}
				else if (command.equalsIgnoreCase("remove"))
				{
					Manager.GetGame().BlockBreakDeny.remove(blockId);
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Blacklist", "Removed " + blockId)));
				}
				else if (command.equalsIgnoreCase("clear"))
				{
					Manager.GetGame().BlockBreakDeny.clear();
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Blacklist", "Cleared")));
				}
				else if (command.equalsIgnoreCase("list"))
				{
					UtilPlayer.message(player, F.main("Event Settings", F.value("Block Break Blacklist", UtilText.listToString(Manager.GetGame().BlockBreakDeny, true))));
				}
			}

			return;
		}
		catch (Exception e)
		{

		}

		commandHelpSettings(player);
	}

	public void commandHealth(Player player, String[] args) 
	{
		try
		{
			if (args.length >= 2)
			{
				int health = Integer.parseInt(args[1]);

				if (health <= 0)
					health = -1;
				if (health > 20)
					health = 20;

				Manager.GetGame().HealthSet = health;

				if (Manager.GetGame().HealthSet == -1)
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Health Set", "Disabled")));
				else
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Health Set", Manager.GetGame().HealthSet + "")));

				return;
			}
		}
		catch (Exception e)
		{

		}

		commandHelpSettings(player);
	}

	public void commandHunger(Player player, String[] args) 
	{
		try
		{
			if (args.length >= 2)
			{
				int hunger = Integer.parseInt(args[1]);

				if (hunger <= 0)
					hunger = -1;
				if (hunger > 20)
					hunger = 20;

				Manager.GetGame().HungerSet = hunger;

				if (Manager.GetGame().HungerSet == -1)
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Hunger Set", "Disabled")));
				else
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Hunger Set", Manager.GetGame().HungerSet + "")));

				return;
			}
		}
		catch (Exception e)
		{

		}

		commandHelpSettings(player);
	}

	public void commandTime(Player player, String[] args) 
	{
		try
		{
			if (args.length >= 2)
			{
				int time = Integer.parseInt(args[1]);

				if (time <= -1)
					time = -1;
				if (time > 24000)
					time = 24000;

				Manager.GetGame().WorldTimeSet = time;

				if (Manager.GetGame().WorldTimeSet == -1)
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Time Set", "Disabled")));
				else
					Manager.GetGame().Announce(F.main("Event Settings", F.value("Time Set", Manager.GetGame().WorldTimeSet + "")));

				return;
			}
		}
		catch (Exception e)
		{

		}

		commandHelpSettings(player);
	}

	//Teleport Command (To, Here, All)
	public void commandTeleport(Player player, String[] args)
	{
		if (args.length >= 3 && args[1].equalsIgnoreCase("here"))
		{
			if (args[2].equalsIgnoreCase("all"))
			{
				for (Player other : UtilServer.getPlayers())
				{
					UtilPlayer.message(other, F.main("Event TP", player.getName() + " teleported everyone to self."));
					other.teleport(player);
				}

				return;
			}

			Player target = UtilPlayer.searchOnline(player, args[2], true);
			if (target != null)
			{
				target.teleport(player);
				UtilPlayer.message(target, F.main("Event TP", player.getName() + " teleported you to self."));
				UtilPlayer.message(player, F.main("Event TP", "Teleported " + target.getName() + " to you."));
			}

			return;
		}

		if (args.length >= 2)
		{
			Player target = UtilPlayer.searchOnline(player, args[1], true);
			if (target != null)
			{
				player.teleport(target);
				UtilPlayer.message(player, F.main("Event TP", "Teleported to " + target.getName() + "."));
			}

			return;
		}

		commandHelp(player);
	}

	//Gadget Commands (Global & Individual)
	public void commandGadget(Player player, String[] args)
	{
		if(!(Manager.GetGame() instanceof EventGame)) 
		{
			UtilPlayer.message(player, F.main("Inventory", "You can only enable/disable gadgets in the Event game!"));
			return;
		}
			
		if (args.length < 2)
		{
			((EventGame) Manager.GetGame()).setAllowGadget(!((EventGame) Manager.GetGame()).isAllowGadget());

			if (!((EventGame) Manager.GetGame()).isAllowGadget())
			{
				Manager.getCosmeticManager().getGadgetManager().disableAll();
			}

			Manager.GetGame().Announce(F.main("Inventory", F.value("Allow All Gadgets", F.ed(((EventGame) Manager.GetGame()).isAllowGadget()))));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("clear"))
		{
			((EventGame) Manager.GetGame()).getGadgetWhitelist().clear();
			Manager.GetGame().Announce(F.main("Inventory", F.value("Gadget Whitelist", "Cleared.")));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("list"))
		{
			ChatColor color = ChatColor.AQUA;

			//Gadgets
			for (GadgetType type : GadgetType.values())
			{
				String items = C.Bold + type + " Gadgets> ";

				for (Gadget gadget : Manager.getCosmeticManager().getGadgetManager().getGadgets(type))
				{
					items += color + gadget.getName().replaceAll(" ", "") + " ";
					color = (color == ChatColor.AQUA ? ChatColor.DARK_AQUA : ChatColor.AQUA);
				}

				UtilPlayer.message(player, items);
			}

			return;
		}

		if (args.length >= 2)
		{
			//Gadgets
			for (GadgetType type : GadgetType.values())
			{
				for (Gadget gadget : Manager.getCosmeticManager().getGadgetManager().getGadgets(type))
				{
					if (gadget.getName().replaceAll(" ", "").equalsIgnoreCase(args[1]))
					{
						if (((EventGame) Manager.GetGame()).getGadgetWhitelist().remove(gadget))
						{
							Manager.GetGame().Announce(F.main("Inventory", F.value(gadget.getName() + " Gadget", F.ed(false))));
							gadget.disableForAll();
						}
						else
						{
							Manager.GetGame().Announce(F.main("Inventory", F.value(gadget.getName() + " Gadget", F.ed(true))));
							((EventGame) Manager.GetGame()).getGadgetWhitelist().add(gadget);
						}

						return;
					}
				}
			}

			UtilPlayer.message(player, F.main("Inventory", args[1] + " is not a valid gadget."));
			return;
		}

		commandHelp(player);
	}

	//setChatSilence
	public void commandSilence(Player player, String[] args)
	{	
		try
		{	
			//Toggle
			if (args.length == 1)
			{
				//Disable
				if (Manager.GetChat().getChatSilence() != 0)
				{
					Manager.GetChat().setChatSilence(0, true);
				}
				//Enable
				else
				{
					Manager.GetChat().setChatSilence(-1, true);
				}
			}
			//Timer
			else
			{
				long time = (long) (Double.valueOf(args[1]) * 3600000);

				Manager.GetChat().setChatSilence(time, true);
			}
		}
		catch (Exception e)
		{
			UtilPlayer.message(player, F.main("Chat", "Invalid Time Parameter."));
		}
	}

	//Gamemode (Self and Others)
	public void commandAdmin(Player player, String[] args)
	{
		Player target = player;

		if (args.length >= 2)
		{
			Player newTarget = UtilPlayer.searchOnline(player, args[1], true);
			if (newTarget != null)
				target = newTarget;
			else
				return;
		}

		if (!Manager.GetGameHostManager().isAdmin(target, false))
			Manager.GetGameHostManager().giveAdmin(target);
		else
			Manager.GetGameHostManager().removeAdmin(target.getName());

		UtilPlayer.message(player, F.main("Event Admin", target.getName() + " Admin: " + F.tf(Manager.GetGameHostManager().isAdmin(target, false))));
	}

	//Gamemode (Self and Others)
	public void commandGamemode(Player player, String[] args)
	{
		Player target = player;

		if (args.length >= 2)
		{
			Player newTarget = UtilPlayer.searchOnline(player, args[1], true);
			if (newTarget != null)
				target = newTarget;
			else
				return;
		}

		if (target.getGameMode() == GameMode.CREATIVE)
			target.setGameMode(GameMode.SURVIVAL);
		else
			target.setGameMode(GameMode.CREATIVE);

		UtilPlayer.message(player, F.main("Event GM", target.getName() + " Creative: " + F.tf(target.getGameMode() == GameMode.CREATIVE)));
	}

	//Forcefield
	public void commandForcefieldRadius(Player player, String[] args)
	{
		
		if(!(Manager.GetGame() instanceof EventGame)) {
			UtilPlayer.message(player, F.main("Inventory", "You can only enable/disable the forcefield in the Event game!"));
			return;
		}
		
		//Toggle
		if (args.length >= 2)
		{
			try
			{
				int range = Integer.parseInt(args[1]);

				((EventGame) Manager.GetGame()).getForcefieldList().put(player.getName(), range);

				UtilPlayer.message(player, F.main("Forcefield", "Enabled with  " + F.elem(range + "") + " radius."));
			}
			catch (Exception e)
			{
				UtilPlayer.message(player, F.main("Forcefield", "Invalid Input."));
			}
		}
		else
		{
			((EventGame) Manager.GetGame()).getForcefieldList().remove(player.getName());
			UtilPlayer.message(player, F.main("Forcefield", "Disabled."));
		}
	}

	//Give 
	public void commandGive(Player player, String[] args)
	{
		String[] newArgs = new String[args.length-1];

		for (int i=0 ; i<newArgs.length ; i++)
			newArgs[i] = args[i+1];

		Give.Instance.parseInput(player, newArgs);  
	}
	
	//Spec 
	public void commandSpectators(Player player, String[] args)
	{
		Manager.GetGame().JoinInProgress = !Manager.GetGame().JoinInProgress;
		
		UtilPlayer.message(player, F.main("Settings", "Spectator Join: " + F.tf(Manager.GetGame().JoinInProgress)));
	}
	
	//Deathout 
	public void commandDeathout(Player player, String[] args)
	{
		Manager.GetGame().DeathOut = !Manager.GetGame().DeathOut;
	
		UtilPlayer.message(player, F.main("Settings", "Deathout: " + F.tf(Manager.GetGame().DeathOut)));
	}
	
	//QuitOut 
	public void commandQuitOut(Player player, String[] args)
	{
		Manager.GetGame().QuitOut = !Manager.GetGame().QuitOut;
		
		UtilPlayer.message(player, F.main("Settings", "QuitOut: " + F.tf(Manager.GetGame().QuitOut)));
	}
	
	//Double Jump
	public void commandDoubleJump(Player player, String[] args)
	{
		
		if(!(Manager.GetGame() instanceof EventGame)) {
			UtilPlayer.message(player, F.main("Settings", "You can only enable/disable the Doublejump in the Event game!"));
			return;
		}
		
		((EventGame) Manager.GetGame()).setDoubleJump(!((EventGame) Manager.GetGame()).isDoubleJump());

		UtilPlayer.message(player, F.main("Settings", "Double Jump: " + F.tf(((EventGame) Manager.GetGame()).isDoubleJump())));

		if (!((EventGame) Manager.GetGame()).isDoubleJump())
			for (Player other : UtilServer.getPlayers())
				other.setAllowFlight(false);
	}

	//Scoreboard
	public void commandScoreboard(Player player, String[] args)
	{
		
		if(!(Manager.GetGame() instanceof EventGame)) {
			UtilPlayer.message(player, F.main("Scoreboard", "You can only edit the Scoreboard in the Event game!"));
			return;
		}
		
		if (args.length >= 2)
		{
			//Line
			int line = 0;
			try
			{
				line = Integer.parseInt(args[1]) - 1;
			}
			catch (Exception e)
			{
				UtilPlayer.message(player, F.main("Scoreboard", "Invalid Line Number."));
				return;
			}

			if (line < 0 || line > 14)
			{
				UtilPlayer.message(player, F.main("Scoreboard", "Invalid Line Number."));
				return;
			}

			//Text
			String lineText = "";

			//Reset String
			if (args.length <= 2)
				for (int i=0 ; i<line ; i++)
					lineText += " ";

			//New String
			for (int i=2 ; i<args.length ; i++)
			{
				lineText += args[i];

				if (i < args.length -1)
					lineText += " ";
			}

			((EventGame) Manager.GetGame()).getSideText()[line] = ChatColor.translateAlternateColorCodes('&', lineText);	

			UtilPlayer.message(player, F.main("Scoreboard", "Set Line " + F.elem((line + 1)+"") + " to " + F.elem(lineText) + "."));

			return;
		}

		//Clear
		if (args.length >= 2 && args[1].equalsIgnoreCase("clear"))
		{
			for (int i=0 ; i<((EventGame) Manager.GetGame()).getSideText().length ; i++)
			{
				String lineText = "";
				for (int j=0 ; j<i ; j++)
					lineText += " ";

				((EventGame) Manager.GetGame()).getSideText()[i] = lineText;
			}

			return;
		}

		for (int i=0 ; i<((EventGame) Manager.GetGame()).getSideText().length ; i++)
		{
			UtilPlayer.message(player, F.value("Line " + i, ((EventGame) Manager.GetGame()).getSideText()[i]));
		}
	}

	//Whitelist
	public void commandWhitelist(Player player, String[] args)
	{
		//On and Off
		if (args.length >= 2)
		{
			if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off"))
			{
				UtilServer.getServer().setWhitelist(args[1].equalsIgnoreCase("on"));

				Manager.GetGame().Announce(F.main("Event Settings", F.value("Whitelist", F.tf(args[1].equalsIgnoreCase("on")))));
				return;
			}
		}

		//Add and Remove
		if (args.length >= 3)
		{
			if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))
			{
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

				if (args[1].equalsIgnoreCase("add"))
				{
					UtilServer.getServer().getWhitelistedPlayers().add(target);
					UtilPlayer.message(player, F.main("Whitelist", "Added " + args[2] + " to the whitelist."));
				}
				else
				{
					UtilServer.getServer().getWhitelistedPlayers().remove(target);
					UtilPlayer.message(player, F.main("Whitelist", "Removed " + args[2] + " to the whitelist."));
				}

				return;
			}
		}

		commandHelp(player);
	}

	//Mob
	public void commandMob(Player caller, String[] args)
	{
		if (args.length == 1)
		{
			HashMap<EntityType, Integer> entMap = new HashMap<EntityType, Integer>();

			int count = 0;
			for (World world : UtilServer.getServer().getWorlds())
			{
				for (Entity ent : world.getEntities())
				{
					if (!entMap.containsKey(ent.getType()))
						entMap.put(ent.getType(), 0);

					entMap.put(ent.getType(), 1 + entMap.get(ent.getType()));
					count++;
				}
			}

			UtilPlayer.message(caller, F.main("Creature", "Listing Entities:"));
			for (EntityType cur : entMap.keySet())
			{
				UtilPlayer.message(caller, F.desc(UtilEnt.getName(cur), entMap.get(cur)+""));
			}

			UtilPlayer.message(caller, F.desc("Total", count+""));
		}
		else
		{
			EntityType type = UtilEnt.searchEntity(caller, args[1], true);

			if (type == null)
				return;

			UtilPlayer.message(caller, F.main("Creature", "Spawning Creature(s);"));

			//Store Args
			HashSet<String> argSet = new HashSet<String>();
			for (int i = 2 ; i < args.length ; i++)
				if (args[i].length() > 0)
					argSet.add(args[i]);


			//Search Count
			int count = 1;
			HashSet<String> argHandle = new HashSet<String>();
			for (String arg : argSet)
			{
				try
				{
					int newCount = Integer.parseInt(arg);

					if (newCount <= 0)
						continue;

					//Set Count
					count = newCount;
					UtilPlayer.message(caller, F.desc("Amount", count+""));

					//Flag Arg
					argHandle.add(arg);
					break;
				}
				catch (Exception e)
				{
					//None
				}
			}
			for (String arg : argHandle)
				argSet.remove(arg);

			//Spawn
			HashSet<Entity> entSet = new HashSet<Entity>();
			for (int i = 0 ; i < count ; i++)
			{
				Manager.GetGame().CreatureAllowOverride = true;
				entSet.add(Manager.GetCreature().SpawnEntity(caller.getTargetBlock((HashSet<Byte>)null, 150).getLocation().add(0.5, 1, 0.5), type));
				Manager.GetGame().CreatureAllowOverride = false;
			}

			//Search Vars
			for (String arg : argSet)
			{
				if (arg.length() == 0)
					continue;

				//Baby
				else if (arg.equalsIgnoreCase("baby") || arg.equalsIgnoreCase("b"))
				{
					for (Entity ent : entSet)
					{
						if (ent instanceof Ageable)
							((Ageable)ent).setBaby();
						else if (ent instanceof Zombie)
							((Zombie)ent).setBaby(true);
					}

					UtilPlayer.message(caller, F.desc("Baby", "True"));
					argHandle.add(arg);
				}

				//Lock
				else if (arg.equalsIgnoreCase("age") || arg.equalsIgnoreCase("lock"))
				{
					for (Entity ent : entSet)
						if (ent instanceof Ageable)
						{
							((Ageable)ent).setAgeLock(true);
							UtilPlayer.message(caller, F.desc("Age", "False"));
						}					

					argHandle.add(arg);
				}

				//Angry
				else if (arg.equalsIgnoreCase("angry") || arg.equalsIgnoreCase("a"))
				{
					for (Entity ent : entSet)
						if (ent instanceof Wolf)
							((Wolf)ent).setAngry(true);

					for (Entity ent : entSet)
						if (ent instanceof Skeleton)
							((Skeleton)ent).setSkeletonType(SkeletonType.WITHER);

					UtilPlayer.message(caller, F.desc("Angry", "True"));
					argHandle.add(arg);
				}

				//Profession
				else if (arg.toLowerCase().charAt(0) == 'p')
				{
					try
					{
						String prof = arg.substring(1, arg.length());

						Profession profession = null;
						for (Profession cur : Profession.values())
							if (cur.name().toLowerCase().contains(prof.toLowerCase()))
								profession = cur;

						UtilPlayer.message(caller, F.desc("Profession", profession.name()));

						for (Entity ent : entSet)
							if (ent instanceof Villager)
								((Villager)ent).setProfession(profession);			
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Profession", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}

				//Size
				else if (arg.toLowerCase().charAt(0) == 's')
				{
					try
					{
						String size = arg.substring(1, arg.length());

						UtilPlayer.message(caller, F.desc("Size", Integer.parseInt(size)+""));

						for (Entity ent : entSet)
							if (ent instanceof Slime)
								((Slime)ent).setSize(Integer.parseInt(size));
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}

				else if (arg.toLowerCase().charAt(0) == 'n' && arg.length() > 1)
				{
					try
					{
						String name = "";

						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							if (c != '_')
								name += c;
							else
								name += " ";
						}

						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.setCustomName(name); 
								cEnt.setCustomNameVisible(true);
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}
				else if (arg.toLowerCase().charAt(0) == 'h' && arg.length() > 1)
				{
					try
					{
						String health = "";

						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							if (c != '_')
								health += c;
							else
								health += " ";
						}
						
						double healthint = Double.parseDouble(health);
						
						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.setMaxHealth(healthint);
								cEnt.setHealth(healthint);
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Health", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}
				else if (arg.toLowerCase().charAt(0) == 'e' && arg.length() > 1)
				{
					try
					{
						String effect = "";

						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							if (c != '_')
								effect += c;
							else
								effect += " ";
						}
						
						PotionEffectType potionType = PotionEffectType.getByName(effect);
						
						if (potionType == null)
						{
							UtilPlayer.message(caller, F.main("Effect", "Invalid Effect Type: " + args[2]));
							UtilPlayer.message(caller, F.value("Valid Types", "http://minecraft.gamepedia.com/Status_effect"));
							return;
						}	
						
						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.addPotionEffect(new PotionEffect(potionType, Integer.MAX_VALUE, 0));
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("PotionEffect", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}
				else if (arg.toLowerCase().charAt(0) == 'i' && arg.length() > 1)
				{
					try
					{
						String item = "";

						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							item += c;
						}
						
						Material mat = Material.getMaterial(item);
						
						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.getEquipment().setItemInHand(new ItemStack(mat));
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Item", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}
				else if (arg.toLowerCase().charAt(0) == 'a' && arg.length() > 1)
				{
					try
					{
						String armor = "";

						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							if (c != '_')
								armor += c;
							else
								armor += " ";
						}
						
						ItemStack head = null;
						ItemStack chest = null;
						ItemStack leggings = null;
						ItemStack boots = null;
						
						
						if(armor.equalsIgnoreCase("leather"))
						{
							head = new ItemStack(Material.LEATHER_HELMET);
							chest = new ItemStack(Material.LEATHER_CHESTPLATE);
							leggings = new ItemStack(Material.LEATHER_LEGGINGS);
							boots = new ItemStack(Material.LEATHER_BOOTS);
						}
						if(armor.equalsIgnoreCase("gold"))
						{
							head = new ItemStack(Material.GOLD_HELMET);
							chest = new ItemStack(Material.GOLD_CHESTPLATE);
							leggings = new ItemStack(Material.GOLD_LEGGINGS);
							boots = new ItemStack(Material.GOLD_BOOTS);
						}
						if(armor.equalsIgnoreCase("chain"))
						{
							head = new ItemStack(Material.CHAINMAIL_HELMET);
							chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
							leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
							boots = new ItemStack(Material.CHAINMAIL_BOOTS);
						}
						if(armor.equalsIgnoreCase("iron"))
						{
							head = new ItemStack(Material.IRON_HELMET);
							chest = new ItemStack(Material.IRON_CHESTPLATE);
							leggings = new ItemStack(Material.IRON_LEGGINGS);
							boots = new ItemStack(Material.IRON_BOOTS);
						}
						if(armor.equalsIgnoreCase("diamond"))
						{
							head = new ItemStack(Material.DIAMOND_HELMET);
							chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
							leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
							boots = new ItemStack(Material.DIAMOND_BOOTS);
						}
						
						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.getEquipment().setHelmet(head);
								cEnt.getEquipment().setChestplate(chest);
								cEnt.getEquipment().setLeggings(leggings);
								cEnt.getEquipment().setBoots(boots);
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Armor", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}
			}
			for (String arg : argHandle)
				argSet.remove(arg);

			for (String arg : argSet)
				UtilPlayer.message(caller, F.desc("Unhandled", arg));

			//Inform
			UtilPlayer.message(caller, F.main("Creature", "Spawned " + count + " " + UtilEnt.getName(type) + "."));
		}
	}

	public void commandMobKill(Player caller, String[] args)
	{
		if (args.length < 3)
		{
			UtilPlayer.message(caller, F.main("Creature", "Missing Entity Type Parameter."));
			return;
		}

		EntityType type = UtilEnt.searchEntity(caller, args[2], true);

		if (type == null && !args[2].equalsIgnoreCase("all"))
			return;

		int count = 0;
		List<Entity> killList = new ArrayList<Entity>();

		for (World world : UtilServer.getServer().getWorlds())
		{
			for (Entity ent : world.getEntities())
			{
				if (ent.getType() == EntityType.PLAYER)
					continue;

				if (type == null || ent.getType() == type)
				{
					killList.add(ent);
				}
			}
		}

		CreatureKillEntitiesEvent event = new CreatureKillEntitiesEvent(killList);
		UtilServer.getServer().getPluginManager().callEvent(event);

		for (Entity entity : event.GetEntities())
		{
			entity.remove();
			count++;
		}

		String target = "ALL";
		if (type != null)
			target = UtilEnt.getName(type);

		UtilPlayer.message(caller, F.main("Creature", "Killed " + target + ". " + count + " Removed."));
	}

	public void commandBC(Player caller, String[] args)
	{
		String message = args[1];
		for(int e = 2; e < args.length; e++)
			message += " " + args[e];
		
		String colored = ChatColor.translateAlternateColorCodes('&', message);
		Manager.GetGame().Announce(F.main("Event", colored), true);
	}
	
	public void commandKit(Player caller, String[] args)
	{
		
		if(!(Manager.GetGame() instanceof EventGame)) {
			UtilPlayer.message(caller, F.main("Inventory", "You can only enable/disable a Kit in the Event game!"));
			return;
		}
		
		if (args.length >= 2 && args[1].equalsIgnoreCase("apply"))
		{
			for (Player player : UtilServer.getPlayers())
				((EventGame) Manager.GetGame()).giveItems(player);

			Manager.GetGame().Announce(F.main("Event Settings", F.value("Player Kit", "Applied to Players")));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("clear"))
		{
			((EventGame) Manager.GetGame()).setKitItems(new ItemStack[6]);
			Manager.GetGame().Announce(F.main("Event Settings", F.value("Player Kit", "Cleared Kit")));
			return;
		}

		if (args.length >= 2 && args[1].equalsIgnoreCase("set"))
		{
			((EventGame) Manager.GetGame()).setKitItems(new ItemStack[6]);

			for (int i=0 ; i<6 ; i++)
			{
				if (caller.getInventory().getItem(i) != null)
					((EventGame) Manager.GetGame()).getKitItems()[i] = caller.getInventory().getItem(i).clone();
				else
					((EventGame) Manager.GetGame()).getKitItems()[i] = null;
			}

			Manager.GetGame().Announce(F.main("Event Settings", F.value("Player Kit", "Updated Items")));
			return;
		}

		commandHelp(caller);
	}

	public void commandEffect(Player caller, String[] args, ArrayList<Player> players)
	{
		//Clear
		if (args.length >= 3 && args[2].equalsIgnoreCase("clear"))
		{
			//Get Targets
			LinkedList<Player> targets = new LinkedList<Player>();

			if (args[1].equalsIgnoreCase("all"))
			{
				for (Player cur : UtilServer.getPlayers())
					targets.add(cur);
				
				_potionEffectsDuration.clear();
				_potionEffectsMult.clear();
			}
			else
			{
				if(players == null)
				{
					targets = UtilPlayer.matchOnline(caller, args[1], true);
					if (targets.isEmpty())			
						return;
				}
				else
				{
					targets = new LinkedList<>();
					for(Player player : players)
						targets.add(player);
						
				}
			}

			for (Player player : targets)
			{
				//Remove all conditions
				Manager.GetCondition().EndCondition(player, null, null);

				//Remove all effects
				player.removePotionEffect(PotionEffectType.ABSORPTION);
				player.removePotionEffect(PotionEffectType.BLINDNESS);
				player.removePotionEffect(PotionEffectType.CONFUSION);
				player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
				player.removePotionEffect(PotionEffectType.FAST_DIGGING);
				player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
				player.removePotionEffect(PotionEffectType.HARM);
				player.removePotionEffect(PotionEffectType.HEAL);
				player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
				player.removePotionEffect(PotionEffectType.HUNGER);
				player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
				player.removePotionEffect(PotionEffectType.JUMP);
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
				player.removePotionEffect(PotionEffectType.POISON);
				player.removePotionEffect(PotionEffectType.REGENERATION);
				player.removePotionEffect(PotionEffectType.SATURATION);
				player.removePotionEffect(PotionEffectType.SLOW);
				player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
				player.removePotionEffect(PotionEffectType.SPEED);
				player.removePotionEffect(PotionEffectType.WATER_BREATHING);
				player.removePotionEffect(PotionEffectType.WEAKNESS);
				player.removePotionEffect(PotionEffectType.WITHER);	
			}

			return;
		}

		//Apply
		if (args.length >= 5)
		{

			//Get Type
			PotionEffectType type = PotionEffectType.getByName(args[2]);
			if (type == null)
			{
				UtilPlayer.message(caller, F.main("Effect", "Invalid Effect Type: " + args[2]));
				UtilPlayer.message(caller, F.value("Valid Types", "http://minecraft.gamepedia.com/Status_effect"));
				return;
			}		

			//Get Multiplier
			int mult = 0;
			try
			{
				mult = Integer.parseInt(args[3]);

				if (mult <= 0)
					mult = 0;
				if (mult > 255)
					mult = 255;				
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main("Effect", "Invalid Effect Level: " + args[3]));
				return;
			}

			//Get Duration
			int dur = 0;
			try
			{
				dur = Integer.parseInt(args[4]);

				if (dur <= 0)
					dur = 0;			
			}
			catch (Exception e)
			{
				UtilPlayer.message(caller, F.main("Effect", "Invalid Effect Duration: " + args[4]));
				return;
			}
			
			//Get Targets
			LinkedList<Player> targets = new LinkedList<Player>();

			if (args[1].equalsIgnoreCase("all"))
			{
				for (Player cur : UtilServer.getPlayers())
					targets.add(cur);
				
				_potionEffectsDuration.put(type, (long) (System.currentTimeMillis() + (dur * 1000)));
				_potionEffectsMult.put(type, mult);
			}
			else
			{		
				if(players == null)
				{
					targets = UtilPlayer.matchOnline(caller, args[1], true);
					if (targets.isEmpty())			
						return;
				}
				else
				{
					targets = new LinkedList<>();
					for(Player player : players)
						targets.add(player);
						
				}
			}

			//Apply
			PotionEffect effect = new PotionEffect(type, dur*20, mult);
			for (Player cur : targets)
			{
				cur.addPotionEffect(effect);
			}

			if (args[1].equalsIgnoreCase("all"))
				Manager.GetGame().Announce(F.main("Effect", F.value("Applied Effect", type.getName() + " " + (mult+1) + " for " + dur + "s")));
			else
				UtilPlayer.message(caller, F.main("Effect", "Applied " + type.getName() + " " + (mult+1) + " for " + dur + "s for Targets."));

			return;
		}

		commandHelp(caller);
	}
	
	@EventHandler
	public void updatePotionEffects(UpdateEvent event)
	{
		if(event.getType() != UpdateType.SEC)
			return;
		
		for(Player player : UtilServer.getPlayers()) 
		{
			for(PotionEffectType effect : _potionEffectsDuration.keySet())
			{
				if(_potionEffectsDuration.get(effect) < 0)
					continue;
				
				player.addPotionEffect(new PotionEffect(effect, (int) (((_potionEffectsDuration.get(effect) - System.currentTimeMillis()) / 1000) * 20), _potionEffectsMult.get(effect)));
			}
		}
	}

	@EventHandler
	public void StackEntity(PlayerInteractEntityEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().IsLive())
			return;

		if (event.getRightClicked().getVehicle() != null)
			return;

		Player player = event.getPlayer();
		Entity other = event.getRightClicked();

		if(!_allowStacker && !_stacker.contains(player))
			return;
		
		if (Manager.isSpectator(event.getPlayer()))
			return;

		if (!Manager.GetGame().IsAlive(event.getPlayer()))
			return;

		if (Manager.getCosmeticManager().getGadgetManager().getActive(player, GadgetType.MORPH) instanceof MorphBlock)
		{
			UtilPlayer.message(player, F.main("Stacker", "You cannot stack while using the Block Morph."));
			return;
		}
		
		StackerEvent stackerEvent = new StackerEvent(player);		
		Bukkit.getServer().getPluginManager().callEvent(stackerEvent);
		if (stackerEvent.isCancelled())
			return;
		
		//Effect
		event.getRightClicked().getWorld().playEffect(event.getRightClicked().getLocation(), Effect.STEP_SOUND, 35);

		//Stack
		player.setPassenger(other);

		//Audio
		player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1f, 1f);

		//Inform
		if ((event.getRightClicked() instanceof Player))
		{
			UtilPlayer.message(other, F.main("Event", F.elem(Manager.GetGame().GetTeam(player).GetColor() + player.getName()) + " picked you up."));
			UtilPlayer.message(player, F.main("Event", "You picked up " + F.elem(Manager.GetGame().GetTeam(((Player) other)).GetColor() + ((Player) other).getName()) + "."));
		}
	}
		
	@EventHandler
	public void ThrowEntity(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Player thrower = event.getPlayer();

		if (thrower.getVehicle() != null)
			return;

		Entity throwee = thrower.getPassenger();
		if (throwee == null)
			return;
		
		StackerEvent stackerEvent = new StackerEvent(thrower);		
		Bukkit.getServer().getPluginManager().callEvent(stackerEvent);
		if (stackerEvent.isCancelled())
			return;

		thrower.eject();

		Entity throweeStack = throwee.getPassenger();
		if (throweeStack != null)
		{
			throwee.eject();
			throweeStack.leaveVehicle();

			final Entity fThrower = thrower;
			final Entity fThroweeStack = throweeStack;

			_tempStackShift.add(throweeStack);

			getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
			{
				public void run()
				{
					fThrower.setPassenger(fThroweeStack);
					_tempStackShift.remove(fThroweeStack);
				}
			}, 2);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void death(PlayerDeathEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		if (!Manager.GetGame().InProgress())
			return;
		
		if (!_keepInventory)
			return;
		
		ArrayList<ItemStack> stacks = new ArrayList<>();
		stacks.addAll(event.getDrops());
		event.getDrops().clear();
		
		Manager.runSyncLater(new Runnable()
		{	
			@Override
			public void run()
			{
				for (ItemStack item : stacks)
					event.getEntity().getInventory().addItem(item);
			}
		}, 40);
	}
	
	public ArrayList<Player> getDamagePlayers()
	{
		return _damage;
	}
	
}
