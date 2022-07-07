package nautilus.game.arcade.game.games.tug.entities;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;

import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.tug.TugTeam;

public class TugSheep extends TugEntity<Sheep>
{

	public TugSheep(TugOfWool host, TugTeam team, Location spawn)
	{
		super(host, team, spawn, 3, 500);
	}

	@Override
	public Sheep spawn(Location location)
	{
		Sheep sheep = location.getWorld().spawn(location, Sheep.class);
		sheep.setColor(_team.getGameTeam().getDyeColor());
		sheep.setMaxHealth(10);
		sheep.setHealth(sheep.getMaxHealth());
		return sheep;
	}

	@Override
	public void attack(LivingEntity other)
	{
		other.damage(4, _entity);
	}
}
