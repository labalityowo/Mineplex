package nautilus.game.arcade.game.games.minecraftleague.data;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DefenderAI
{
	private TowerManager _manager;
	private TeamTowerBase _tower;
	private int _number;
	private long _lastAttack;
	private long _procTime;
	private DefenseAnimation _animation;
	
	public DefenderAI(TowerManager manager, TeamTowerBase tower)
	{
		_manager = manager;
		_tower = tower;
		
		if (tower instanceof TeamTower)
			_number = ((TeamTower)tower).Number;
		else
			_number = 3;
		
		_lastAttack = System.currentTimeMillis();
		_procTime = -1;
		_animation = new DefenseAnimation();
	}
	
	public void update()
	{
		if (!_tower.Alive)
			return;
		
		attack();
		_animation.update();
	}
	
	private void animate()
	{
		_animation.activate();
	}
	
	private void attack()
	{
		if (!_tower.Vulnerable)
			return;
		
		if (_procTime != -1)
		{
			if (System.currentTimeMillis() >= _procTime)
			{
				_procTime = -1;
				_lastAttack = System.currentTimeMillis();
				attackProc();
				return;
			}
		}
		if (!_manager.Attack)
			return;
		if (!UtilTime.elapsed(_lastAttack, UtilTime.convert(5, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			return;
		/*if (UtilMath.random.nextDouble() < .75)
			return;*/
		_lastAttack = System.currentTimeMillis();
		_procTime = System.currentTimeMillis() + UtilTime.convert(4, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
		animate();
	}
	
	private void attackProc()
	{
		_animation.deactivate();
		for (LivingEntity le : UtilEnt.getInRadius(_tower.getLocation(), 7).keySet())
		{
			if (!(le instanceof Player))
				continue;
			
			Player player = (Player)le;
			if (_manager.Host.GetTeam(player).GetColor() == _tower.getTeam().GetColor())
				continue;
			if (UtilPlayer.isSpectator(player))
				continue;
			
			_manager.Host.storeGear(player);
			player.getWorld().strikeLightningEffect(player.getLocation());
			player.damage(Math.min(6 * 2, player.getHealth()));
		}
	}
	
	private class DefenseAnimation
	{
		private Location _base;
		private double _step;
		//private final double _baseRadius;
		private double _radius;
		private long _lastStepIncrease;
		private boolean _active;
		
		public DefenseAnimation()
		{
			_step = 0;
			_lastStepIncrease = System.currentTimeMillis();
			_base = _tower.getLocation().clone();
			/*if (_tower instanceof TeamTower)
			{
				//_baseRadius = -1;
				_base = _tower.getLocation().clone().add(0, 10, 0);
			}
			else
			{
				//_baseRadius = 11;
				_base = _tower.getLocation().clone();
			}*/
			_radius = /*_baseRadius*/2;
		}
		
		public void activate()
		{
			_active = true;
		}
		
		public void deactivate()
		{
			_active = false;
		}
		
		public void update()
		{
			if (_number != 3)
				drawBeam();
			
			if (!_active)
				return;
			
			if (UtilTime.elapsed(_lastStepIncrease, UtilTime.convert(1, TimeUnit.SECONDS, TimeUnit.MILLISECONDS)))
			{
				_step++;
				_lastStepIncrease = System.currentTimeMillis();
			}
			drawHelix();
		}
		
		private void drawHelix()
		{
			double height = Math.min(_step * 2, 15D);
			
			for (double y = 0; y <= height; y += .5)
			{
				double x = _radius * Math.cos(y);
				double z = _radius * Math.sin(y);
				Location play = new Location(_base.getWorld(), _base.getX() + x, _base.getY() + y, _base.getZ() + z);
				
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, play, null, 0, 3, ViewDist.MAX);
			}
		}
		
		private void drawBeam()
		{
			Location base = _base.clone().add(0, 10, 0);
			Location target = _manager.getTeamTowers(_tower.getTeam()).get(_number).getLocation().clone().add(0, 10, 0);
			Location display = base.clone();
			while (UtilMath.offset(base, target) > UtilMath.offset(base, display))
			{
				Vector v = UtilAlg.getTrajectory(display, target);
				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, display, null, 0, 1, ViewDist.MAX);
				display.add(v);
			}
		}
	}
}
