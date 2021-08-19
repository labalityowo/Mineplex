package nautilus.game.arcade.game.games.minecraftleague.variation.wither.data;

import java.util.HashMap;
import java.util.LinkedList;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.DataLoc;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamTower;
import nautilus.game.arcade.game.games.minecraftleague.data.TeamTowerBase;
import nautilus.game.arcade.game.games.minecraftleague.data.TowerManager;
import nautilus.game.arcade.game.games.minecraftleague.variation.wither.WitherVariation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;

public class WitherPathfinder
{
	private PathfinderData _pathData;
	private WitherVariation _host;
	private Wither _ent;
	private LinkedList<Location> _waypoints;
	private GameTeam _team;
	private GameTeam _enemy;
	private TowerManager _towerManager;
	private long _lastAttack;
	private long _lastTowerAttack;
	
	private boolean _startedJourney = false;
	
	private double _health;
	
	public WitherPathfinder(WitherVariation host, Wither ent, LinkedList<Location> waypoints, GameTeam team, GameTeam enemy, TowerManager towerManager)
	{
		_host = host;
		_ent = ent;
		_waypoints = waypoints;
		_team = team;
		_enemy = enemy;
		_towerManager = towerManager;
		_lastAttack = -1;
		_lastTowerAttack = -1;
		_pathData = new PathfinderData(ent, waypoints.getFirst());
		_health = ent.getHealth();
	}
	
	private int getWaypointIndex(Location loc)
	{
		int best = -1;
		double bestDist = 0;

		for (int i=0 ; i<_waypoints.size() ; i++) 
		{
			Location waypoint = _waypoints.get(i);

			double dist = UtilMath.offset(waypoint, loc);

			if (best == -1 || dist < bestDist)
			{
				best = i;
				bestDist = dist;
			}
		}

		return best;
	}
	
	private void advance()
	{
		_pathData.Target = _waypoints.get(Math.min(_waypoints.size()-1, (getWaypointIndex(_pathData.Location) + 1)));
		_pathData.move();
	}
	
	private void retarget()
	{
		_ent.setTarget(null);
		
		LivingEntity target = null;
		HashMap<LivingEntity, Double> inside = UtilEnt.getInRadius(_ent.getLocation(), 30);
		for (LivingEntity ent : inside.keySet())
		{
			if (UtilPlayer.isSpectator(ent))
				continue;
			
			if (ent instanceof Player)
			{
				if (_team.HasPlayer((Player)ent))
					continue;
			}
			else
				continue;
			
			if (target == null)
				target = ent;
			
			if (inside.get(target) > inside.get(ent))
				target = ent;
		}
		
		if (target != null)
		{
			if (UtilTime.elapsed(_lastAttack, UtilTime.convert(2, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			{
				_lastAttack = System.currentTimeMillis();
				shootAt(target.getLocation(), false);
			}
		}
	}
	
	private void shootAt(Location loc, boolean charged)
	{
		Location old = _ent.getLocation();
		Location temp = _ent.getLocation();
		temp.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(_ent.getEyeLocation(), loc)));
		temp.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_ent.getEyeLocation(), loc)));
		_ent.teleport(temp);
		_ent.setMetadata("Shooting", new FixedMetadataValue(_host.Manager.getPlugin(), "1"));
		WitherSkull skull = _ent.launchProjectile(WitherSkull.class);
		skull.setCharged(charged);
		_ent.removeMetadata("Shooting", _host.Manager.getPlugin());
		_ent.teleport(old);
		//skull.setDirection(UtilAlg.getTrajectory(_ent.getLocation(), loc).normalize());
	}
	
	private TeamTowerBase getProperTarget()
	{
		for (TeamTowerBase t : _towerManager.getTeamTowers(_enemy))
		{
			if (t.Alive)
				return t;
		}
		return null;
	}
	
	public GameTeam getTeam()
	{
		return _team;
	}
	
	/**
	 * Returns true if the entity is dead or invalid
	 */
	public boolean update()
	{
		if ((_ent == null) || _ent.isDead() || !_ent.isValid())
			return true;
		
		Entity eTower = getProperTarget().getEntity();
		if (eTower == null || !eTower.isValid() || eTower.isDead())
		{
			_ent.remove();
			return true;
		}
		
		if (_ent.getHealth() < _ent.getMaxHealth())
		{
			if (!_startedJourney)
			{
				_health = _ent.getMaxHealth();
				return false;
			}
		}
		_startedJourney = true;
		
		_health = Math.min(_health, _ent.getHealth());
		_ent.setHealth(_health);
		
		for (Entity e : UtilEnt.getAllInRadius(_ent.getLocation(), 3).keySet())
		{
			if (e instanceof Arrow)
			{
				Arrow arrow = (Arrow)e;
				if (arrow.getShooter() instanceof Player)
				{
					if (!_team.HasPlayer((Player)arrow.getShooter()))
					{
						arrow.remove();
						_ent.damage(5, (Player)arrow.getShooter());
					}
				}
			}
		}
		
		if (_ent.getLocation().distance(eTower.getLocation()) <= 10)
		{
			_ent.setTarget(null);
			String tName = "";
			if (_team.GetColor() == ChatColor.RED)
				tName = "RED";
			if (_team.GetColor() == ChatColor.AQUA)
				tName = "BLUE";
			Integer cNumber = -1;
			if (getProperTarget() instanceof TeamTower)
				cNumber = ((TeamTower)getProperTarget()).Number;
			else
				cNumber = 3;
			Location finalize = _host.Host.WorldData.GetCustomLocs(DataLoc.TOWER_WAYPOINT.getKey().replace("$team$", tName).replace("$number$", cNumber + "")).get(0);
			finalize.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(_ent, eTower)));
			finalize.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_ent, eTower)));
			_ent.teleport(finalize);
			if (UtilTime.elapsed(_lastTowerAttack, UtilTime.convert(5, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			{
				_lastTowerAttack = System.currentTimeMillis();
				shootAt(eTower.getLocation(), true);
			}
			return false;
		}
		
		advance();
		retarget();
		
		return false;
	}
}
