package nautilus.game.arcade.game.games.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Objective;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.common.dominate_data.CapturePointTDM;
import nautilus.game.arcade.game.games.common.dominate_data.Resupply;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class TeamDeathmatch extends TeamGame
{
	private List<CapturePointTDM> _points = new ArrayList<>(); 
	private List<Resupply> _resupply = new ArrayList<>();

	private Map<Player, Integer> _kills = new HashMap<>();

	private CapturePointTDM _beacon = null;
	private long _beaconTime = 180000;
	

	public TeamDeathmatch(ArcadeManager manager, GameType type, Kit[] kits) 
	{
		super(manager, type, kits,

				new String[]
						{
				"Each player has " + C.cRed + C.Bold + "ONE LIFE",
				"Kill the other team to win!",

						});

		this.DeathOut = true;
		this.HungerSet = 20; 
		this.WorldTimeSet = 2000;
		this.AllowParticles = false;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);
		
		Manager.GetDamage().UseSimpleWeaponDamage = false;
	}       

	@Override    
	public void ParseData()  
	{ 
		for (String pointName : WorldData.GetAllCustomLocs().keySet())
		{
			_points.add(new CapturePointTDM(this, pointName, WorldData.GetAllCustomLocs().get(pointName).get(0)));
		}

		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			_resupply.add(new Resupply(this, loc));
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
	public void PlayerKillAward(CombatDeathEvent event)
	{ 
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}
		
		Player killed = (Player)event.GetEvent().getEntity();
		SetPlayerState(killed, PlayerState.OUT);

		GameTeam killedTeam = GetTeam(killed);
		if (killedTeam == null)
		{
			return;
		}

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

			if (killer != null && !killer.equals(killed))
			{
				GameTeam killerTeam = GetTeam(killer);
				if (killerTeam == null)
				{
					return;
				}

				int kills = 1;
				if (killedTeam.equals(killerTeam))
				{
					kills = -1;
				}

				if (_kills.containsKey(killer))
				{
					kills += _kills.get(killer);
				}

				_kills.put(killer, kills);

				ScoreboardUpdate(null);
			} 
		}
	}  
	
	@EventHandler
	public void Health(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			player.setMaxHealth(30);
			player.setHealth(player.getMaxHealth());
		} 
	} 

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return 4;
	}

	public String GetMode()
	{
		return "Team Deathmatch";
	}

	@Override    
	public void ValidateKit(Player player, GameTeam team)
	{ 
		//Set to Default Knight
		if (GetKit(player) == null)
		{
			SetKit(player, GetKits()[2], true);
			UtilPlayer.closeInventoryIfOpen(player);
		}
	}

	@Override
	public DeathMessageType GetDeathMessageType()
	{
		return DeathMessageType.Detailed;
	}

	@EventHandler
	public void WaterArrowCancel(EntityShootBowEvent event)
	{
		if (event.getEntity().getLocation().getBlock().isLiquid())
		{
			UtilPlayer.message(event.getEntity(), F.main("Game", "You cannot use your Bow while swimming."));
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
			for (CapturePointTDM cur : _points)
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
	public void BeaconUpdate(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		if (_beacon != null)
		{
			return;
		}

		if (UtilTime.elapsed(GetStateTime(), _beaconTime))
		{
			_beacon = _points.get(UtilMath.r(_points.size()));
			_beacon.Enable();
			
			Announce(C.cWhite + C.Bold + _beacon.GetName() + " Capture Point is active!");

			for (Player player : UtilServer.getPlayers())
			{
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
			}
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event != null && event.getType() != UpdateType.FAST)
		{
			return;
		}

		ScoreboardWrite();
	}

	public void ScoreboardWrite()
	{
		Scoreboard.reset();

		for (GameTeam team : this.GetTeamList())
		{		
			//Display Individual Players
			if (this.GetPlayers(false).size() <= 10)
			{
				Scoreboard.writeNewLine();
				
				for (Player player : team.GetPlayers(false))
				{
					int kills = 0;
					if (_kills.containsKey(player))
					{
						kills = _kills.get(player);
					}
					
					if (IsAlive(player))
					{
						Scoreboard.write(kills + " " + team.GetColor() + player.getName());
					}
					else
					{
						Scoreboard.write(kills + " " + C.cGray + player.getName());
					}
				}
			}

			//Display Players Alive
			else
			{
				Scoreboard.writeNewLine();

				Scoreboard.write(team.GetColor() + team.GetName() + " Team");
				Scoreboard.write(team.GetColor() + "" + team.GetPlayers(true).size() + " Alive");
			}
		}

		if (_beacon != null)
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cYellow + C.Bold + _beacon.GetName());
			Scoreboard.write(_beacon.GetOwnership());
		}
		else
		{
			long timeLeft = _beaconTime - (System.currentTimeMillis() - GetStateTime());

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cYellow + C.Bold + "Beacon");
			Scoreboard.write(UtilTime.MakeStr(timeLeft, 0));
		}

		Scoreboard.draw();
	}

	public void EndCheckScore()   
	{
		if (!IsLive())
		{
			return;
		}

		GameTeam winner = _beacon.GetWinner();
		
		if (winner == null)
		{
			return;
		}

		ScoreboardWrite();

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
		
		//Announce End
		AnnounceEnd(winner);
		
		//End
		SetState(GameState.End);	
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void UsableInteract(PlayerInteractEvent event)
	{
		if (UtilBlock.usable(event.getClickedBlock()))
		{
			event.setCancelled(true);
		}
	}
}