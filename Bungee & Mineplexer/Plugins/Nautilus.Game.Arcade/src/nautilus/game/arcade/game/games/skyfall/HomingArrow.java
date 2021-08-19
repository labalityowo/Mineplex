package nautilus.game.arcade.game.games.skyfall;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.Game;

/**
 * This Object represents an Arrow which will follow Players in a close range. <br/>
 * <br/>
 * The Object needs to be updated by using {@link #update()} in a prefered time interval,
 * but needs to find a Target player first by using {@link #findPlayer()} in a prefered time interval.
 *
 * @author xXVevzZXx
 */
public class HomingArrow
{
	private Player _shooter;
	
	private Game _host;
	private Arrow _arrow;
	private Player _target;
	private int _range;
	
	private long _spawned;
	private int _timeAlive;
	
	private int _updates;
	
	/**
	 * Standard Constructor of the HomingArrow
	 * 
	 * @param shooter Player who shot the arrow
	 * @param host current game
	 * @param arrow fired arrow entity
	 * @param target Player (may be null)
	 * @param range of the arrow
	 * @param seondsAlive duration the Arrow will be active
	 */
	public HomingArrow(Player shooter, Game host, Arrow arrow, Player target, int range, int seondsAlive)
	{
		_shooter = shooter;
		_host = host;
		_arrow = arrow;
		_target = target;
		_range = range;
		
		_spawned = System.currentTimeMillis();
		_timeAlive = seondsAlive;
	}
	
	/**
	 * @return true if a Player was found
	 */
	public Player findPlayer()
	{
		for (Player player : _host.GetPlayers(true))
		{
			if (UtilMath.offset(player.getLocation(), _arrow.getLocation()) <= _range)
			{
				if (_shooter == player)
					continue;
				
				if (_host.TeamMode && _host.GetTeam(_shooter) == _host.GetTeam(player))
					continue;
					
				if (!UtilPlayer.isGliding(player))
					continue;
					
				_target = player;
				return player;
			}
		}
		return null;
	}
	
	/**
	 * You need to update the arrow as soon as a target is set or found by {@link #findPlayer()}}
	 */
	public void update()
	{
		if (canRemove())
			return;
		
		if (_target == null)
			return;
		
		Vector arrowToPlayer = _target.getLocation().add(0, 1, 0).toVector().subtract(_arrow.getLocation().toVector()).normalize();
		
		int firework = 5;
		if (_updates < (_timeAlive*20)*0.20)
		{
			firework = 15;
			arrowToPlayer.multiply(1.5);
		}
		else if (_updates < (_timeAlive*20)*0.40)
		{
			firework = 10;
			arrowToPlayer.multiply(1.6);
		}
		else if (_updates < (_timeAlive*20)*0.60)
			arrowToPlayer.multiply(1.7);
		else if (_updates < (_timeAlive*20)*0.80)
			arrowToPlayer.multiply(1.8);
		else
			arrowToPlayer.multiply(1.9);
		
		UtilAction.velocity(_arrow, arrowToPlayer);
		
		if (_updates % firework == 0)
			UtilFirework.playFirework(_arrow.getLocation(), Type.BALL, Color.RED, true, false);
		
		_updates++;
	}
	
	public boolean foundPlayer()
	{
		return _target != null;
	}
	
	public boolean canRemove()
	{
		return UtilTime.elapsed(_spawned, 1000 * _timeAlive) || _arrow.isOnGround() || _arrow.isDead();
	}
}
