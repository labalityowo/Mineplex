package nautilus.game.pvp.modules.clans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import nautilus.game.pvp.modules.clans.ClansUtility.ClanRelation;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.chiss.Core.Combat.Event.CombatDeathEvent;
import mineplex.minecraft.game.core.classcombat.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import me.chiss.Core.Module.AModule;
import me.chiss.Core.Plugin.IChat;
import me.chiss.Core.Plugin.IRelation;
import mineplex.core.packethandler.INameColorer;
import mineplex.core.server.IRepository;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.account.CoreClient;
import mineplex.core.common.Rank;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.creature.event.CreatureSpawnCustomEvent;
import mineplex.minecraft.punish.PunishChatEvent;

public class Clans extends AModule implements IRelation, IChat, INameColorer
{
	private int _dominanceLimit = 16;
	private int _inviteExpire = 2;
	private int _nameMin = 3;
	private int _nameMax = 10;
	private long _powerTime = 300000;
	private long _reclaimTime = 1800000;
	private long _onlineTime = 1200000;

	private long _generatorTime = 21600000;		//6 Hours
	
	private long _outpostTime = 7200000;		//2 Hours

	private List<ClansClan> _genUpdateList = new ArrayList<ClansClan>();

	private boolean _powerEnabled = false;

	//Clans
	private HashMap<String, ClansClan> _clanMap = new HashMap<String, ClansClan>();
	private HashMap<String, ClansClan> _clanMemberMap = new HashMap<String, ClansClan>();
	private HashMap<String, ClansTerritory> _claimMap = new HashMap<String, ClansTerritory>();
	private HashMap<String, Long> _unclaimMap = new HashMap<String, Long>();
	
	private HashMap<String, ClansOutpost> _clanOutpostMap = new HashMap<String, ClansOutpost>();

	//Clans Modules
	private ClansAdmin _clansAdmin;
	private ClansBlocks _clansBlocks;
	private ClansCommand _clansCommand;
	private ClansDisplay _clansDisplay;
	private ClansGame _clansGame;
	private ClansRepo _clansRepo;
	private ClansTask _clansTask;
	private ClansUtility _clansUtility;

	//Repo
	private IRepository _repository;

	private String _serverName;
	
	public Clans(JavaPlugin plugin, IRepository repository, String serverName) 
	{
		super("Clans", plugin);

		_serverName = serverName;
		
		SetRepository(repository);

		CRepo().loadClans();
	}

	//Module Functions
	@Override
	public void config() 
	{
		_dominanceLimit = Config().getInt(_moduleName, "Dominance Limit", _dominanceLimit);
		_inviteExpire = Config().getInt(_moduleName, "Invitation Expire (Minutes)", _inviteExpire);
		_nameMin = Config().getInt(_moduleName, "Name Minimum Length", _nameMin);
		_nameMax = Config().getInt(_moduleName, "Name Maximum Length", _nameMax);
		_powerTime = Config().getLong(_moduleName, "Power Regeneration Time", _powerTime);
		_reclaimTime = Config().getLong(_moduleName, "Territory Reclaim Time", _reclaimTime);
		_powerEnabled = Config().getBool(_moduleName, "Power Enabled", _powerEnabled);
	}

	@Override
	public void enable() 
	{

	}

	@Override
	public void disable()
	{
		//runs in parallel, nothing to do here.
	}

	@Override
	public void commands() 
	{
		AddCommand("c");
		AddCommand("clans");
		AddCommand("f");
		AddCommand("factions");

		AddCommand("cc");
		AddCommand("fc");
		AddCommand("ac");
	}

	@Override
	public void command(Player caller, String cmd, String[] args) 
	{
		if (cmd.equals("cc") || cmd.equals("fc"))
			CCommand().commandChat(caller, args);
		
		else if (cmd.equals("ac"))
			CCommand().commandAllyChat(caller, args);

		else
			CCommand().command(caller, args);
	}

	public ClansClan getClan(String name)
	{
		return GetClanMap().get(name);
	}

	public void EndWar(final ClansClan cA, final ClansClan cB) 
	{
		final ClansWar war = cA.GetEnemyOut().get(cB.GetName());
		if (war == null)	return;

		//Recharge
		cA.GetEnemyRecharge().put(cB.GetName(), System.currentTimeMillis() + 172800000);
		cB.GetEnemyRecharge().put(cA.GetName(), System.currentTimeMillis() + 172800000);

		//Neutral
		CTask().neutral(cA, cB, "War End", true);

		//Pillage Relation
		CTask().pillage(cA, cB, true);
		
		//Delay
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				//Broadcast
				if (Math.abs(war.GetDominance()) == GetDominanceLimit())
					UtilServer.broadcastSpecial("Clans Invasion", F.name(cA.GetName()) + " conquered " + F.name(cB.GetName()) + 
							" in an invasion, taking " + 
							F.time(UtilTime.convertString(System.currentTimeMillis() - war.GetCreated(), 1, TimeUnit.FIT)) + 
							"." );
			}
		}, 20);

		final long duration = (long) (1200000 * ((double)cB.GetMembers().size() / (double)cA.GetMembers().size()));
		int durationTicks = (int) ((duration * 20) / 1000);

		//To Start
		for (int i = 0 ; i<10 ; i++)
		{
			final int j = i;

			//Delay
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					cA.inform("Pillage on " + F.name("Clan " + cB.GetName()) + " starts in " + F.time((10 - j) + " Minutes") + ".", null);
					cB.inform("Pillage by " + F.name("Clan " + cA.GetName()) + " starts in " + F.time((10 - j) + " Minutes") + ".", null);
				}
			}, 60 + (1200 * i));
		}

		//Start
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				cA.inform("Pillage on " + F.name("Clan " + cB.GetName()) + " has begun!", null);
				cB.inform("Pillage by " + F.name("Clan " + cA.GetName()) + " has begun!", null);
				cA.GetPillage().add(cB.GetName());
			}
		}, 12000);

		//End
		_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
		{
			public void run()
			{
				cA.inform("Pillage on " + F.name("Clan " + cB.GetName()) + " has ended!", null);
				cB.inform("Pillage by " + F.name("Clan " + cA.GetName()) + " has ended!", null);
				cA.GetPillage().remove(cB.GetName());
				
				CTask().pillage(cA, cB, false);
			}
		}, 12000 + durationTicks);

		//To End
		int i = 0;
		while (durationTicks > 1200)
		{
			//Increment
			durationTicks -= 1200;
			i++;

			//Final
			final int count = i;

			//Delay
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					//Inform A	
					cA.inform("Pillage on " + F.name("Clan " + cB.GetName()) + " ending in " + F.time(count + " Minutes") + ".", null);
					cB.inform("Pillage by " + F.name("Clan " + cA.GetName()) + " ending in " + F.time(count + " Minutes") + ".", null);
				}
			}, 12000 + durationTicks);	
		}
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
			Power();

		if (event.getType() == UpdateType.FAST)
			CGame().UpdateSafe();

		if (event.getType() == UpdateType.FASTER)
			CGame().UpdateDisplay();

		if (event.getType() == UpdateType.SEC)
		{
			for (ClansClan clan : _clanMap.values())
			{
				if (clan.GeneratorUpdate())
					_genUpdateList.add(clan);

				clan.OutpostUpdate();
			}
			
			if (_genUpdateList.size() > 0)
			{
				CRepo().Repository.UpdateClanTNTGenerators(_genUpdateList);
				_genUpdateList.clear();		
			}
		}	
		
		if (event.getType() == UpdateType.FAST)
			for (ClansClan clan : _clanMap.values())
				if (clan.GetOutpost() != null)
					clan.GetOutpost().BuildUpdate();
	}

	public long lastPower = System.currentTimeMillis();
	public void Power()
	{
		HashSet<ClansClan> handledSet = new HashSet<ClansClan>();

		for (Player cur : UtilServer.getPlayers())
		{
			ClansClan clan = CUtil().getClanByPlayer(cur);

			if (clan == null)
				continue;

			if (clan.getPower() == clan.getPowerMax())
			{
				clan.SetPowerTime(0);
				continue;
			}

			if (handledSet.contains(clan))
				continue;

			if (CUtil().isSafe(cur))
				continue;	

			if (cur.isDead())
				continue;

			//Add Time
			clan.SetPowerTime(clan.GetPowerTime() + (System.currentTimeMillis() - lastPower));

			if (clan.GetPowerTime() > GetPowerTime())
			{
				clan.SetPowerTime(0);
				clan.modifyPower(1);
				clan.inform("Clan regenerated 1 Power.", null);
			}

			//Set Handled
			handledSet.add(clan);
		}

		lastPower = System.currentTimeMillis();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void BlockBurn(BlockBurnEvent event)
	{
		CGame().BlockBurn(event);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void BlockIgnite(BlockIgniteEvent event)
	{
		CGame().BlockSpread(event);
	}
	
	@EventHandler(priority = EventPriority.LOW) //AFTER Field
	public void BlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;

		CGame().BlockBreak(event);

		if (event.getBlock().getType() == Material.BREWING_STAND)
			for (ClansClan clan : _clanMap.values())
				clan.GeneratorBreak(event.getBlock().getLocation());
		
		if (event.getBlock().getType() == Material.BEACON)
			for (ClansClan clan : _clanMap.values())
				clan.OutpostBreak(event.getBlock().getLocation());
	}

	@EventHandler(priority = EventPriority.LOW) 
	public void BlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;
		
		CGame().BlockPlace(event);

		//TNT Generator
		if (event.getBlock().getType() == Material.BREWING_STAND)
		{
			ClansClan clan = CUtil().getClanByPlayer(event.getPlayer());
			if (clan != null)
			{
				if (!clan.GeneratorPlace(event.getPlayer(), event.getBlock().getLocation()))
					event.setCancelled(true);
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You must place " + F.item("TNT Generator") + " in your Territory."));
				event.setCancelled(true);			
			}
		}

		//Outpost
		if (event.getBlock().getType() == Material.BEACON)
		{
			ClansClan clan = CUtil().getClanByPlayer(event.getPlayer());
			if (clan != null)
			{
				if (!clan.OutpostPlace(event.getPlayer(), event.getBlock().getLocation()))
					event.setCancelled(true);
			}
			else
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You require a Clan to use " + F.item("Clan Outpost") + "."));
				event.setCancelled(true);			
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void BlockCreatureSpawn(CreatureSpawnCustomEvent event)
	{
		ClansClan clan = Clans().CUtil().getOwner(event.GetLocation());
		
		if (clan != null)
			if (!clan.IsAdmin() && !clan.GetName().equals("Spawn"))
				event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		CGame().Damage(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Interact(PlayerInteractEvent event)
	{
		CGame().Interact(event);
		CDisplay().handleInteract(event);

		if (Util().Event().isAction(event, ActionType.R_BLOCK))
			if (event.getClickedBlock().getType() == Material.BREWING_STAND && !event.isCancelled())
			{
				for (ClansClan clan : _clanMap.values())
					clan.GeneratorUse(event.getPlayer(), event.getClickedBlock().getLocation());

				event.setCancelled(true);
			}	
			else if (event.getClickedBlock().getType() == Material.BEACON)
			{
				for (ClansClan clan : _clanMap.values())
					clan.OutpostUse(event.getPlayer(), event.getClickedBlock().getLocation());

				event.setCancelled(true);
			}	
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Piston(BlockPistonExtendEvent event)
	{
		CGame().Piston(event);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void SkillTrigger(SkillTriggerEvent event)
	{
		CGame().SafeSkill(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void Death(CombatDeathEvent event)
	{
		CGame().DeathDominance(event);

		CGame().DeathColor(event);
	}

	@EventHandler
	public void Join(PlayerJoinEvent event)
	{
		CGame().Join(event);
	}

	@EventHandler
	public void Quit(PlayerQuitEvent event)
	{
		CGame().Quit(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Explosion(EntityExplodeEvent event)
	{
		CGame().Explode(event);
	}

	public boolean HandleClanChat(AsyncPlayerChatEvent event, String filteredMessage)
	{
		CoreClient client =	Clients().Get(event.getPlayer());
		
		if (client == null)
			return false;
		
		if (!client.Clan().IsClanChat())
			return false;

		ClansClan clan = CUtil().getClanByPlayer(event.getPlayer());
		if (clan == null)	
		{
			Clients().Get(event.getPlayer()).Clan().SetClanChat(false);
			return false;
		}

		ChatClan(clan, event.getPlayer(), event.getMessage(), filteredMessage);
		return true;
	}
	
	public boolean HandleAllyChat(AsyncPlayerChatEvent event, String filteredMessage)
	{
		if (!Clients().Get(event.getPlayer()).Clan().IsAllyChat())
			return false;

		ClansClan clan = CUtil().getClanByPlayer(event.getPlayer());
		if (clan == null)	
		{
			Clients().Get(event.getPlayer()).Clan().SetAllyChat(false);
			return false;
		}

		ChatAlly(clan, event.getPlayer(), event.getMessage(), filteredMessage);
		return true;
	}

	public void HandleChat(final AsyncPlayerChatEvent event, final String filteredMessage)
	{
		// Call it sync since we are using async and not just modifying the global message(per player tweaks)
		event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask(Plugin(), new Runnable()
		{
			public void run()
			{
				if (HandleClanChat(event, filteredMessage))
					return;

				if (HandleAllyChat(event, filteredMessage))
					return;

				PunishChatEvent chatEvent = new PunishChatEvent(event.getPlayer());
				
				Plugin().getServer().getPluginManager().callEvent(chatEvent);
				
				if (chatEvent.isCancelled())
					return;
				
				ChatGlobal(event.getPlayer(), event.getMessage(), filteredMessage);
			}
		});
	}

	public void ChatClan(ClansClan clan, Player sender, String message, String filteredMessage)
	{
		clan.chat(sender, message, filteredMessage);
	}
	
	public void ChatAlly(ClansClan clan, Player sender, String message, String filteredMessage)
	{
		for (String cur : clan.GetAllyMap().keySet())
		{
			ClansClan ally = CUtil().getClanByClanName(cur);
			if (ally == null)	continue;
			
			ally.allyChat(clan, sender, message, filteredMessage);
		}
		
		clan.allyChat(clan, sender, message, filteredMessage);
	}

	public void ChatGlobal(final Player sender, final String message, String filteredMessage)
	{
		for (Player cur : sender.getServer().getOnlinePlayers())
		{
			String newMessage = message;

			//Get Client
			CoreClient client = Clients().Get(sender.getName());

			if (client.Game().GetFilterChat())
			{
				newMessage = filteredMessage;
			}

			//Prepend Name
			newMessage = _clansUtility.mRel(client.Clan().GetRelation(cur.getName()), sender.getName(), false) + " " + newMessage;

			//Prepend Clan
			if (Clients().Get(sender).Clan().InClan())
				newMessage = _clansUtility.mRel(client.Clan().GetRelation(cur.getName()), Clients().Get(sender).Clan().GetClanName(), true) + " " + newMessage;

			//Prepend NAC / Rank
			StringBuilder builder = new StringBuilder();

			String prefixChar = "*";

			if (client.NAC().IsUsing())
				builder.append(ChatColor.GREEN + prefixChar);
			else
				builder.append(ChatColor.DARK_GRAY + prefixChar);

			if (client.Rank().Has(Rank.OWNER, false))
				builder.append(ChatColor.AQUA + prefixChar + ChatColor.WHITE);
			else if (client.Rank().Has(Rank.MODERATOR, false))
				builder.append(ChatColor.AQUA + prefixChar + ChatColor.WHITE);
			else if (client.Rank().Has(Rank.DIAMOND, false))
				builder.append(ChatColor.AQUA + prefixChar + ChatColor.WHITE);
			else if (client.Rank().Has(Rank.EMERALD, false))
				builder.append(ChatColor.GREEN + prefixChar + ChatColor.WHITE);
			else if (client.Donor().HasDonated())
				builder.append(ChatColor.YELLOW + prefixChar + ChatColor.WHITE);
			else
				builder.append(ChatColor.DARK_GRAY + prefixChar + ChatColor.WHITE);

			String icons = builder.toString();
			newMessage = icons + newMessage;

			UtilPlayer.message(cur, newMessage, true);
		}
	}

	public ClansAdmin CAdmin()
	{
		if (_clansAdmin == null)
			_clansAdmin = new ClansAdmin(this);

		return _clansAdmin;
	}

	public ClansBlocks CBlocks()
	{
		if (_clansBlocks == null)
			_clansBlocks = new ClansBlocks(this);

		return _clansBlocks;
	}

	public ClansCommand CCommand()
	{
		if (_clansCommand == null)
			_clansCommand = new ClansCommand(this);

		return _clansCommand;
	}

	public ClansDisplay CDisplay()
	{
		if (_clansDisplay == null)
			_clansDisplay = new ClansDisplay(this);

		return _clansDisplay;
	}

	public ClansGame CGame()
	{
		if (_clansGame == null)
			_clansGame = new ClansGame(this);

		return _clansGame;
	}

	public ClansRepo CRepo()
	{
		if (_clansRepo == null)
			_clansRepo = new ClansRepo(this);

		return _clansRepo;
	}

	public ClansTask CTask()
	{
		if (_clansTask == null)
			_clansTask = new ClansTask(this);

		return _clansTask;
	}

	public ClansUtility CUtil()
	{
		if (_clansUtility == null)
			_clansUtility = new ClansUtility(this);

		return _clansUtility;
	}

	public int GetDominanceLimit() {
		return _dominanceLimit;
	}

	public int GetInviteExpire() {
		return _inviteExpire;
	}

	public int GetNameMin() {
		return _nameMin;
	}

	public int GetNameMax() {
		return _nameMax;
	}

	public long GetPowerTime() {
		return _powerTime;
	}

	public long GetReclaimTime() {
		return _reclaimTime;
	}

	public long GetGeneratorTime() {
		return _generatorTime;
	}

	public long GetOutpostTime() {
		return _outpostTime;
	}

	public long GetOnlineTime() {
		return _onlineTime;
	}

	public boolean IsPowerEnabled() {
		return _powerEnabled;
	}

	public void SetPowerEnabled(boolean _powerEnabled) {
		this._powerEnabled = _powerEnabled;
	}

	public HashMap<String, ClansClan> GetClanMap() {
		return _clanMap;
	}
	
	public HashMap<String, ClansOutpost> GetOutpostMap() 
	{
		return _clanOutpostMap;
	}

	public HashMap<String, ClansTerritory> GetClaimMap() 
	{
		return _claimMap;
	}

	public HashMap<String, Long> GetUnclaimMap() 
	{
		return _unclaimMap;
	}


	@Override
	public boolean CanHurt(Player a, Player b) 
	{
		if (a.equals(b))
			return false;

		return CUtil().canHurt(a, b);
	}

	@Override
	public boolean CanHurt(String a, String b) 
	{
		if (a.equals(b))
			return false;

		return CUtil().canHurt(UtilPlayer.searchExact(a), UtilPlayer.searchExact(b));
	}
	
	@Override
	public boolean IsSafe(Player a) 
	{
		return CUtil().isSafe(a);
	}

	public IRepository GetRepository() {
		return _repository;
	}

	public void SetRepository(IRepository _repository) {
		this._repository = _repository;
	}

	public HashMap<String, ClansClan> GetClanMemberMap() 
	{
		return _clanMemberMap;
	}

	public ClanRelation GetRelation(String playerA, String playerB) 
	{
		return Clients().Get(playerA).Clan().GetRelation(playerB);
	}

	@Override
	public ChatColor GetColorOfFor(String other, Player player) 
	{
		return CUtil().relChatColor(Clients().Get(player).Clan().GetRelation(other), false);
	}

	public String GetServerName()
	{
		return _serverName;
	}
}
