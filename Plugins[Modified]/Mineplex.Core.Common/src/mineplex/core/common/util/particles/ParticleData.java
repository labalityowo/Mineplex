package mineplex.core.common.util.particles;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParticleData
{
	protected UtilParticle.ParticleType _particleType;
	protected Location _location;

	public ParticleData(UtilParticle.ParticleType particleType, Location location)
	{
		_particleType = particleType;
		_location = location;
	}

	/**
	 * Displays the particles for all the players with a different amount
	 * @param count the amount of particles
	 */
	public void display(int count)
	{
		display(count, UtilParticle.ViewDist.NORMAL, UtilServer.getPlayers());
	}

	/**
	 * Displays the particles for selected players
	 * @param viewDist The distance of the particle view
	 * @param players The players that will receive the particle
	 */
	public void display(UtilParticle.ViewDist viewDist, Player... players)
	{
		UtilParticle.PlayParticle(_particleType, _location, 0f, 0f, 0f, 0f, 1, viewDist, players);
	}

	/**
	 * Displays the particles for the selected players, with a custom amount
	 * @param count the amount of particles
	 * @param viewDist the distance of the particle view
	 * @param players the players that will receive the particle
	 */
	public void display(int count, UtilParticle.ViewDist viewDist, Player... players)
	{
		UtilParticle.PlayParticle(_particleType, _location, 0f, 0f, 0f, 0f, count, viewDist, players);
	}

	/**
	 * Displays the particles for all the players
	 * @param viewDist The distance of the particle view
	 */
	public void display(UtilParticle.ViewDist viewDist)
	{
		display(viewDist, UtilServer.getPlayers());
	}

	/**
	 * Displays the particles for all the players, with a NORMAL view distance
	 */
	public void display()
	{
		display(UtilParticle.ViewDist.NORMAL);
	}

	public void setLocation(Location location)
	{
		_location = location;
	}

	public Location getLocation()
	{
		return _location;
	}
}