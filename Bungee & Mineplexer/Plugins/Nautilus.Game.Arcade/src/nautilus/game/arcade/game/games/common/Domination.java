package nautilus.game.arcade.game.games.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.RadarData;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilRadar;
import mineplex.core.common.util.UtilServer;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.champions.ChampionsDominate;
import nautilus.game.arcade.game.games.champions.events.CaptureEvent;
import nautilus.game.arcade.game.games.common.dominate_data.CapturePoint;
import nautilus.game.arcade.game.games.common.dominate_data.Emerald;
import nautilus.game.arcade.game.games.common.dominate_data.PlayerData;
import nautilus.game.arcade.game.games.common.dominate_data.Resupply;
import nautilus.game.arcade.game.modules.SpawnRegenerationModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
  
public class Domination extends TeamGame
{       
	//Configuration
	public boolean EnableEmerald = true;
	public boolean EnableSupply = true;
	
	//Map Data 
	private final List<CapturePoint> _points = new ArrayList<>();
	private final List<Emerald> _emerald = new ArrayList<>();
	private final List<Resupply> _resupply = new ArrayList<>();
	
	//Stats
	private final Map<String, PlayerData> _stats = new HashMap<>();
 
	//Scores   
	private int _victoryScore = 15000, _redScore = 0,  _blueScore = 0;

	public Domination(ArcadeManager manager, GameType type, Kit[] kits)
	{ 
		super(manager, type, kits,   
   
						new String[]
								{ 
				"Capture Beacons for Points", 
				"+300 Points for Emerald Powerups",
				"+50 Points for Kills",
				"First team to 15000 Points wins"

							 	});

		AllowParticles = false;
		this.DeathOut = false;
		this.PrepareFreeze = true;   
		this.HungerSet = 20; 
		this.WorldTimeSet = 2000; 
  
		this.DeathSpectateSecs = 10;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
		
		new SpawnRegenerationModule()
				.register(this);
	} 
   
	@Override   
	public void ParseData()  
	{ 
		for (String pointName : WorldData.GetAllCustomLocs().keySet())
		{
			_points.add(new CapturePoint(this, pointName, WorldData.GetAllCustomLocs().get(pointName).get(0)));
		}
 
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			_resupply.add(new Resupply(this, loc));
		}
 
		for (Location loc : WorldData.GetDataLocs("LIME"))
		{
			_emerald.add(new Emerald(this, loc));
		}
		
		_victoryScore = 3000 * _points.size();

		//Spawn Kits
		if (this instanceof ChampionsDominate)
		{
			CreatureAllowOverride = true;
			
			for (int i = 0; i < GetKits().length && i < WorldData.GetDataLocs("RED").size() && i < WorldData.GetDataLocs("BLUE").size(); i++)
			{
				GetKits()[i].getGameKit().createNPC(WorldData.GetDataLocs("RED").get(i));
				GetKits()[i].getGameKit().createNPC(WorldData.GetDataLocs("BLUE").get(i));
			}
			 
			CreatureAllowOverride = false; 
		}
	}
	
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			if (team.GetColor() == ChatColor.AQUA)
			{
				team.SetColor(ChatColor.BLUE);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockliquidFlow(BlockPhysicsEvent event)
	{
		Material matOfBlock = event.getBlock().getType();

		if (matOfBlock == Material.STATIONARY_WATER || matOfBlock == Material.SAND || matOfBlock == Material.GRAVEL || matOfBlock == Material.STATIONARY_LAVA)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTNTExplode(EntityExplodeEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getEntityType() == EntityType.PRIMED_TNT)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void Updates(UpdateEvent event)
	{  
		if (!IsLive())
		{
			return;
		}

		if (event.getType() == UpdateType.FAST)
		{
			for (CapturePoint cur : _points)
			{
				cur.Update();
			}
		}

		if (event.getType() == UpdateType.FAST)
		{
			for (Emerald cur : _emerald)
			{
				cur.Update();
			}
		}

		if (event.getType() == UpdateType.FAST)
		{
			for (Resupply cur : _resupply)
			{
				cur.Update();
			}
		}
	}

	@EventHandler
	public void PowerupPickup(PlayerPickupItemEvent event)
	{
		for (Emerald cur : _emerald)
		{
			if (EnableEmerald)
			{
				cur.Pickup(event.getPlayer(), event.getItem());
			}
			else
			{
				return;
			}
		}

		for (Resupply cur : _resupply)
		{
			if (EnableSupply)
			{
				cur.Pickup(event.getPlayer(), event.getItem());
			}
			else
			{
				return;
			}
		}
	}

	@EventHandler
	public void KillScore(CombatDeathEvent event)
	{
		Player killed = event.GetEvent().getEntity();

		GameTeam killedTeam = GetTeam(killed);
		if (killedTeam == null) 	return;

		if (event.GetLog().GetKiller() == null)
		{
			return;
		}

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

		if (killer == null)
		{
			return;
		}

		GameTeam killerTeam = GetTeam(killer);
		if (killerTeam == null)	return;

		if (killerTeam.equals(killedTeam))
		{
			return;
		}

		AddScore(killerTeam, 50);
	}

	public void AddScore(GameTeam team, int score)
	{
		if (team.GetColor() == ChatColor.RED)
		{
			_redScore = Math.min(_victoryScore, _redScore + score);
		}
		else
		{
			_blueScore = Math.min(_victoryScore, _blueScore + score);
		}

		EndCheckScore();
	}

	//Dont allow powerups to despawn
	@EventHandler
	public void ItemDespawn(ItemDespawnEvent event)
	{
		event.setCancelled(true);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		ScoreboardWrite();

	}

	private void ScoreboardWrite() 
	{
		if (!InProgress())
		{
			return;
		}

		//Wipe Last
		Scoreboard.reset();

		//Scores
		Scoreboard.writeNewLine();
		Scoreboard.write("First to " + _victoryScore);
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cRed + "Red Team");
		Scoreboard.write(_redScore + C.cRed);
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cAqua + "Blue Team");
		Scoreboard.write(_blueScore + C.cAqua);

		Scoreboard.writeNewLine();

		//Write CPs
		for (CapturePoint point : _points)
		{
			Scoreboard.write(point.GetScoreboardName());
		}  
		
		Scoreboard.draw();
	}  
      
	public void EndCheckScore()   
	{
		if (!IsLive())
		{
			return;
		}
		
		GameTeam winner = null;
 
		if (_redScore >= _victoryScore)
		{
			winner = GetTeam(ChatColor.RED);
		}
		else if (_blueScore >= _victoryScore)
		{
			winner = GetTeam(ChatColor.BLUE);
		}

		if (winner == null)
		{
			return;
		}
 
		ScoreboardWrite();

		//Announce
		AnnounceEnd(winner);

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
				{
					AddGems(player, 10, "Winning Team", false, false);
				}
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}
		}
		
		endElo();
 
		//End
		SetState(GameState.End);	
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return 1; 
	}
	  
	public String GetMode()
	{
		return "Domination";
	}
	
	public PlayerData GetStats(Player player)
	{
		if (!_stats.containsKey(player.getName()))
		{
			_stats.put(player.getName(), new PlayerData(player.getName()));
		}
		
		return _stats.get(player.getName());
	}
	
	@EventHandler
	public void StatsKillAssistDeath(CombatDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;
		
		Player killed = event.GetEvent().getEntity();
		GetStats(killed).Deaths++;

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				GetStats(killer).Kills++;
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
			{
				continue;
			}

			Player assist = UtilPlayer.searchExact(log.GetName());

			//Assist
			if (assist != null)
			{
				GetStats(assist).Assists++;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void StatsKillAssistDeath(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)
		{
			GetStats(damager).DamageDealt += event.GetDamage();
		}
		
		Player damagee = event.GetDamageePlayer();
		if (damagee != null)
		{
			GetStats(damagee).DamageTaken += event.GetDamage();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void UsableInteract(PlayerInteractEvent event)
	{
		if (UtilBlock.usable(event.getClickedBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler 
	public void radarUpdate(UpdateEvent event)     
	{
		if (!InProgress())
		{
			return;
		}
		
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		List<RadarData> data = new ArrayList<>();
		
		for (CapturePoint point : _points)
		{
			data.add(new RadarData(point.getLocation(), point.getRadarTag()));
		}
		
		for (Player player : UtilServer.getPlayers())
		{
			UtilRadar.displayRadar(player, data);
		}
	}

	@EventHandler
	public void capture(CaptureEvent event)
	{
		event.getPlayers().forEach(player -> getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.GAME_CAPTURE_POINT, GetType().getDisplay(), null));
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		GetTeamList().forEach(team ->
		{
			boolean red = team.GetColor() == ChatColor.RED;
			team.GetPlayers(false).forEach(player -> getArcadeManager().getMissionsManager().incrementProgress(player, red ? _redScore : _blueScore, MissionTrackerType.DOM_POINT, GetType().getDisplay(), null));
		});
	}
}