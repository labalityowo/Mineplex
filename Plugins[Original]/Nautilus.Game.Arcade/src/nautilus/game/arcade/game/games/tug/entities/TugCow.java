package nautilus.game.arcade.game.games.tug.entities;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.tug.TugTeam;

public class TugCow extends TugEntity<Cow>
{

	public TugCow(TugOfWool host, TugTeam team, Location spawn)
	{
		super(host, team, spawn, 4, 400);
	}

	@Override
	public Cow spawn(Location location)
	{
		Cow cow = location.getWorld().spawn(location, Cow.class);

		cow.setCustomNameVisible(true);
		cow.setMaxHealth(60);
		cow.setHealth(cow.getMaxHealth());

		return cow;
	}

	@Override
	public void attack(LivingEntity other)
	{
		other.damage(12, _entity);
		other.getWorld().playSound(other.getLocation(), Sound.COW_IDLE, 1, (float) Math.random());
	}

	@Override
	public boolean attemptTarget(LivingEntity other)
	{
		return false;
	}

	@Override
	public float getSpeed()
	{
		return super.getSpeed() * 1.5F;
	}

	@Override
	public void move()
	{
		super.move();

		UtilParticle.PlayParticleToAll(ParticleType.CRIT, _entity.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0, 1, ViewDist.LONG);

		if (_entity.getTicksLived() % 5 == 0)
		{
			attackNearby();
		}
	}
}
