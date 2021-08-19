package mineplex.core.pet;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;

public class FlyingPetManager
{

	/**
	 * Makes the Flying pets fly around the player
	 * Copied from {@link mineplex.core.gadget.gadgets.particle.ParticleFairyData}
	 */

	private Player _player;
	private Entity _pet;
	private Location _grimReaperLoc, _target;
	private Vector _direction;
	private double _speed;
	private long _idleTime;

	public FlyingPetManager(Player player, Entity pet)
	{
		_player = player;
		_pet = pet;
		_grimReaperLoc = player.getEyeLocation();
		_target = getNewTarget();
		_speed = 0.2;
		_idleTime = 0;
		_direction = new Vector(1, 0, 0);
	}

	public void update()
	{
		//Update Target
		if (UtilMath.offset(_player.getEyeLocation(), _target) > 3 || UtilMath.offset(_grimReaperLoc, _target) < 1)
			_target = getNewTarget();

		//Pause?
		if (Math.random() > 0.98)
			_idleTime = System.currentTimeMillis() + (long)(Math.random() * 3000);

		//Speed
		if (UtilMath.offset(_player.getEyeLocation(), _grimReaperLoc) < 3)
		{
			if (_idleTime > System.currentTimeMillis())
			{
				_speed = Math.max(0, _speed - 0.005);
			}
			else
			{
				_speed = Math.min(0.15, _speed + 0.005);
			}
		}
		else
		{
			_idleTime = 0;

			_speed = Math.min(0.15 + UtilMath.offset(_player.getEyeLocation(), _grimReaperLoc) * 0.05, _speed + 0.02);
		}


		//Modify Direction
		_direction.add(UtilAlg.getTrajectory(_grimReaperLoc, _target).multiply(0.15));
		if (_direction.length() < 1)
			_speed = _speed * _direction.length();
		UtilAlg.Normalize(_direction);

		//Move
		if (UtilMath.offset(_grimReaperLoc, _target) > 0.1)
			_grimReaperLoc.add(_direction.clone().multiply(_speed));

		_pet.teleport(_grimReaperLoc);
		_pet.setVelocity(new Vector(0, .25, 0));
	}

	private Location getNewTarget()
	{
		return _player.getEyeLocation().add(Math.random() * 6 - 3, Math.random() * 1.5, Math.random() * 6 - 3);
	}

}
