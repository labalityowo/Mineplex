package nautilus.game.minekart.gp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import nautilus.game.minekart.gp.GP.GPState;
import nautilus.game.minekart.gp.command.GpCommand;
import nautilus.game.minekart.gp.command.ItemCommand;
import nautilus.game.minekart.gp.command.KartCommand;
import nautilus.game.minekart.gp.command.VoteCommand;
import nautilus.game.minekart.kart.Kart;
import nautilus.game.minekart.kart.KartType;
import nautilus.game.minekart.kart.KartManager;
import nautilus.game.minekart.track.Track.TrackState;
import nautilus.game.minekart.track.TrackManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.core.teleport.Teleport;
import mineplex.core.updater.event.RestartServerEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.donation.DonationManager;

public class GPManager extends MiniPlugin
{
	private DonationManager _donationManager;
	private Teleport _teleport;
	private Recharge _recharge;
	
	public KartManager KartManager;
	public TrackManager TrackManager;
	public Portal Portal;

	//Queue Data
	private GPSet _set;
	private Location _spawn;
	
	private ArrayList<Player> _players = new ArrayList<Player>();
	private HashMap<Player, Boolean> _playerVote = new HashMap<Player, Boolean>();
	private HashMap<Player, KartType> _kartSelect = new HashMap<Player, KartType>();

	private long _voteTimer = 0;
	private int _startTimer = 0;
	
	//Live Sets
	private HashSet<GP> _live = new HashSet<GP>();

	//Result Maps
	private HashSet<GPResult> _results = new HashSet<GPResult>();

	public GPManager(JavaPlugin plugin, DonationManager donationManager, Teleport teleport, Recharge recharge, KartManager kartManager, TrackManager trackManager) 
	{
		super("Race Manager", plugin);

		_donationManager = donationManager;
		_teleport = teleport;
		_recharge = recharge;
		KartManager = kartManager;
		TrackManager = trackManager;
		Portal = new Portal(plugin);
		
		_spawn = new Location(UtilWorld.getWorld("world"), 8.5, 18, -22.5);
	}
	
	@Override
	public void AddCommands()
	{
		AddCommand(new GpCommand(this));
		AddCommand(new ItemCommand(this));
		AddCommand(new KartCommand(this));
		AddCommand(new VoteCommand(this));
	}

	public void SelectKart(Player player, KartType kart)
	{
		_kartSelect.put(player, kart);
		UtilPlayer.message(player, F.main("MK", "You selected " + F.elem(kart.GetName() + " Kart") + "."));
	}

	public KartType GetSelectedKart(Player player)
	{
		if (!_kartSelect.containsKey(player))
			_kartSelect.put(player, KartType.Sheep);

		return _kartSelect.get(player);
	}
	
	@EventHandler
	public void restartServerCheck(RestartServerEvent event)
	{
		if (_live.size() > 0 || _results.size() > 0)
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void Motd(ServerListPingEvent event)
	{	
		if (_live.size() > 0)
		{
			if (_live.iterator().next() instanceof GPBattle)
			{
				event.setMotd(ChatColor.AQUA + "In Battle...");
			}
			else
			{
				event.setMotd(ChatColor.AQUA + "In Race...");
			}
			
			event.setMaxPlayers(_plugin.getServer().getOnlinePlayers().length);
		}
		else
		{
			event.setMotd(ChatColor.GREEN + GetSet().GetName());
			event.setMaxPlayers(10);
		}
	}
	
	@EventHandler
	public void CheckStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		//Full
		if (_players.size() >= 10)
		{
			StartGP(false);
			return;
		}

		//Votes
		int votes = 0;
		for (boolean vote : _playerVote.values())
		{
			if (vote)
				votes++;
		}
		
		int needed = (_players.size() / 2) + (_players.size()%2);
		
		if (votes >= needed && _players.size() >= 4)
		{
			StartGP(false);
		}
		else
		{
			if (UtilTime.elapsed(_voteTimer, 30000))
			{
				if (_players.size() >= 4)
				{
					Announce(F.main("MK", "Type " + F.elem(C.cGreen + "/vote") + " to start the game with less players."));
					Announce(F.main("MK", F.elem((needed - votes)+"") + " more votes needed..."));
				}
				else
				{
					Announce(F.main("MK", "Waiting for players..."));
				}
				
				
				_voteTimer = System.currentTimeMillis();
			}
		}
	}
	
	public void StartGP(boolean force) 
	{
		if (!force && _startTimer > 0)
		{
			Announce(F.main("MK", "Starting in " + F.time(UtilTime.MakeStr(_startTimer * 1000)) + "."));
			_startTimer--;
			
			return;
		}
		
		GP gp;
		if (GetSet() != GPSet.Battle)	gp = new GP(this, GetSet());
		else							gp = new GPBattle(this, GetSet());

		//Add Players
		int added = 0;
		while (added < 10)
		{
			if (_players.isEmpty())
				break;
			
			Player player = _players.remove(0);
			gp.AddPlayer(player, GetSelectedKart(player));
			
			//Clean Player
			_playerVote.remove(player);
			_kartSelect.remove(player);
			
			added++;
		}

		//Set Live
		gp.SetState(GPState.Live);
		gp.NextTrack();
		_live.add(gp);
	}

	public void Vote(Player caller) 
	{
		if (!_playerVote.containsKey(caller))
			return;
		
		boolean vote = _playerVote.get(caller);
		
		_playerVote.put(caller, !vote);
		
		if (!vote)	Announce(F.main("MK", F.elem(caller.getName()) + " has " + F.elem(C.cGreen + "voted") + " to start the game."));
		else		Announce(F.main("MK", F.elem(caller.getName()) + " has " + F.elem(C.cRed + "unvoted") + " to start the game."));
	}
	
	public void Announce(String string)
	{
		for (Player player : _players)
			UtilPlayer.message(player, string);
	}
	
	public GP GetGP(Player player)
	{
		for (GP race : _live)
			if (race.GetPlayers().contains(player))
				return race;

		return null;
	}

	@EventHandler
	public void PlayerLogin(PlayerLoginEvent event)
	{
		if (_live.size() > 0)
		{
			event.disallow(Result.KICK_FULL, ChatColor.AQUA + "A race is already in progress.");
		}	
	}
	
	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		_players.add(player);
		_playerVote.put(player, false);
		_startTimer = 20;
		
		player.teleport(_spawn);
	}
	
	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		//Remove Kart Choice
		_kartSelect.remove(player);

		//Remove Kart
		Kart kart = KartManager.GetKart(player);
		if (kart != null)
			KartManager.RemoveKart(kart.GetDriver());

		//Leave Queue
		_players.remove(player);
		_playerVote.remove(player);
		_kartSelect.remove(player);

		//Leave GP
		GP gp = GetGP(player);
		if (gp != null)	
			gp.RemovePlayer(player, kart);

		//Leave Result
		for (GPResult result : _results)
			result.RemovePlayer(player);
		
		if (gp != null && gp.GetPlayers().size() == 0)
			_plugin.getServer().shutdown();
	}
	
	@EventHandler
	public void TeleportSpawn(EntityDamageEvent event)
	{
		if (event.getCause() != DamageCause.VOID)
			return;
		
		if (!_players.contains(event.getEntity()))
			return;
		
		event.getEntity().teleport(_spawn);
	}

	@EventHandler
	public void UpdateGPScoreboard(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (GP gp : _live) 
		{
			if (gp.GetState() != GPState.Ended && gp.GetTrack().GetState() != TrackState.Countdown)
			{
				gp.UpdateScoreBoard();
			}
		}
	}
	
	@EventHandler
	public void UpdateGP(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		HashSet<GP> remove = new HashSet<GP>();

		for (GP gp : _live) 
		{
			if (gp.GetState() == GPState.Ended)
			{
				remove.add(gp);
				continue;
			}

			if (gp.GetTrack().GetState() == TrackState.Countdown)
			{
				gp.GetTrack().SpawnTeleport();
			}
			
			if (gp instanceof GPBattle)
				((GPBattle)gp).CheckBattleEnd();

			gp.GetTrack().Update();
		}

		for (GP gp : remove)
		{
			gp.Unload();
			_live.remove(gp);
		}
	}

	@EventHandler
	public void UpdateGPResult(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		HashSet<GPResult> remove = new HashSet<GPResult>();

		for (GPResult result : _results) 
		{
			if (result.End())
				remove.add(result);
		}

		for (GPResult result : remove)
		{
			_results.remove(result);
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					_plugin.getServer().shutdown();
				}
			}, 100L);
		}
	}

	@EventHandler
	public void CreatureSpawn(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() == SpawnReason.NATURAL || event.getSpawnReason() == SpawnReason.DEFAULT || event.getSpawnReason() == SpawnReason.EGG)
			event.setCancelled(true);
	}

	@EventHandler
	public void BlockSpread(BlockSpreadEvent event)
	{
		event.setCancelled(true);
	}

	public GPResult CreateResult(GP gp)
	{
		GPResult result = new GPResult(gp, _donationManager);
		_results.add(result);
		return result;
	}

	public void DeleteResult(GPResult result)
	{
		_results.remove(result);
	}

	@EventHandler
	public void HandleChat(AsyncPlayerChatEvent event)
	{
		event.setCancelled(true);

		final String message = event.getMessage();
		final Player sender = event.getPlayer();
		final GP senderGP = GetGP(event.getPlayer());

		//Talk to GP
		if (senderGP != null)
		{
			_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
			{
				public void run()
				{
					senderGP.Announce(C.cYellow + sender.getName() + " " + C.cWhite + message);
				}
			}, 0);
		}
		else
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (GetGP(player) != null)
					continue;

				final Player recipient = player;

				_plugin.getServer().getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
				{
					public void run()
					{
						UtilPlayer.message(recipient, C.cYellow + sender.getName() + " " + C.cWhite + message);
					}
				}, 0);
			}
		}
	}

	public boolean InGame(Player player)
	{
		return GetGP(player) != null;
	}

	public GPSet GetSet() 
	{
		if (_set == null)
		{	
			File file = new File("GPSet.dat");

			//Write If Blank
			if (!file.exists())
			{
				try
				{
					FileWriter fstream = new FileWriter(file);
					BufferedWriter out = new BufferedWriter(fstream);

					out.write("MushroomCup");

					out.close();
				}
				catch (Exception e)
				{
					System.out.println("Error: GP Set Write Exception");
				}
			}

			//Read
			try
			{
				FileInputStream fstream = new FileInputStream(file);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = br.readLine();

				_set = GPSet.valueOf(line);

				in.close();
			}
			catch (Exception e)
			{
				System.out.println("Error: GP Set Read Exception");
			}
		}
		
		if (_set == null)
		{
			return GPSet.MushroomCup;
		}
		
		return _set;
	}

	public Teleport GetTeleport()
	{
		return _teleport;
	}

	public Recharge GetRecharge()
	{
		return _recharge;
	}
}
