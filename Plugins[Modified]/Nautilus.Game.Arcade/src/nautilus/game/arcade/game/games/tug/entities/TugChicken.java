package nautilus.game.arcade.game.games.tug.entities;

import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.tug.TugTeam;

public class TugChicken extends TugEntity<Chicken>
{

	public TugChicken(TugOfWool host, TugTeam team, Location spawn)
	{
		super(host, team, spawn, 3, 500);
	}

	@Override
	public Chicken spawn(Location location)
	{
		Chicken chicken = location.getWorld().spawn(location, Chicken.class);

		chicken.setCustomNameVisible(true);
		chicken.setMaxHealth(20);
		chicken.setHealth(chicken.getMaxHealth());

		return chicken;
	}

	@Override
	public void attack(LivingEntity other)
	{
		other.damage(4, _entity);
		_entity.setVelocity(new Vector(0, 0.5, 0));
	}

	@Override
	public boolean attemptTarget(LivingEntity other)
	{
		return false;
	}

	@Override
	public void move()
	{
		super.move();

		if (_entity.getTicksLived() % 10 == 0)
		{
			attackNearby();
		}
	}
}
