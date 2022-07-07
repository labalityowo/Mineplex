package mineplex.core.pet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;

public class TrueLoveData
{

	private Player _player;
	private Zombie _zombie;
	private Villager _villager;
	private int _step = 0;

	private static final int POINTS = 12;
	private static final float RADIUS = 1.75F;

	public TrueLoveData(Player player, Zombie zombie, Villager villager)
	{
		_player = player;
		_zombie = zombie;
		_villager = villager;
	}

	public void update()
	{
		double increment = (2 * Math.PI) / POINTS;

		// Villager
		double angle = _step * increment;
		Vector vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		_villager.setVelocity(new Vector(0,0,0));
		_villager.teleport(_player.getLocation().clone().add(vector));
		// Make villager look at right location
		angle = (_step + 1) * increment;
		vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		Location targetLoc = _player.getLocation().clone().add(vector);
		Vector directionBetweenLocs = targetLoc.toVector().subtract(_villager.getEyeLocation().toVector());
		Location villagerLoc = _villager.getLocation().setDirection(directionBetweenLocs);
		_villager.teleport(villagerLoc);

		// Zombie
		angle = (_step - 3) * increment;
		vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		_zombie.setVelocity(new Vector(0, 0, 0));
		_zombie.teleport(_player.getLocation().clone().add(vector));
		// Make zombie look at right location
		angle = (_step - 2) * increment;
		vector = new Vector(Math.cos(angle) * RADIUS, 0, Math.sin(angle) * RADIUS);
		targetLoc = _player.getLocation().clone().add(vector);
		directionBetweenLocs = targetLoc.toVector().subtract(_zombie.getEyeLocation().toVector());
		Location zombieLoc = _zombie.getLocation().setDirection(directionBetweenLocs);
		_zombie.teleport(zombieLoc);

		_step++;
	}

	public void remove()
	{
		_villager.remove();
		_zombie.remove();
	}

}
