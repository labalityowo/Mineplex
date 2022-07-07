package nautilus.game.arcade.game.games.dragons;

import net.minecraft.server.v1_8_R3.EntityEnderDragon;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;

public class MineplexDragon
{

	private static final int MAX_SPARKLER_DISTANCE = 48 * 48;

	private final Dragons _host;
	private final EnderDragon _entity;

	MineplexDragon(Dragons host, EnderDragon entity)
	{
		_host = host;
		_entity = entity;

		targetSky();
	}

	void updateTarget()
	{
		Location location = _entity.getLocation(), spectatorLocation = _host.GetSpectatorLocation();

		// Too low (target fell into void?).
		if (location.getY() < 10)
		{
			targetSky();
			return;
		}

		// Not near target.
		if (UtilMath.offsetSquared(location, getTarget()) > 36)
		{
			return;
		}

		// Too high.
		if (location.getY() > spectatorLocation.getY())
		{
			targetPlayer();
		}
		// In the middle.
		else
		{
			targetSky();
		}
	}

	void targetPlayer()
	{
		Player player = UtilAlg.Random(_host.GetPlayers(true));

		// No alive player found.
		if (player == null)
		{
			targetSky();
		}
		else
		{
			setTargetEntity(player);
		}
	}

	void targetSky()
	{
		setTargetLocation(_host.GetSpectatorLocation().clone().add(UtilMath.rRange(-50, 50), UtilMath.rRange(20, 40), UtilMath.rRange(-50, 50)));
	}

	void targetSparkler(Location location)
	{
		// Too far from sparkler to see
		if (UtilMath.offsetSquared(location, _entity.getLocation()) > MAX_SPARKLER_DISTANCE)
		{
			return;
		}

		setTargetLocation(location);
	}

	private void setTargetLocation(Location location)
	{
		getHandle().setTargetBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	private void setTargetEntity(Entity entity)
	{
		getHandle().setTargetEntity(((CraftEntity) entity).getHandle());
	}

	private Location getTarget()
	{
		EntityEnderDragon dragon = getHandle();

		if (dragon.target != null)
		{
			return new Location(_entity.getWorld(), dragon.target.locX, dragon.target.locY, dragon.target.locZ);
		}
		else
		{
			// a = targetX, b = targetY, c = targetZ
			return new Location(_entity.getWorld(), dragon.a, dragon.b, dragon.c);
		}
	}

	private EntityEnderDragon getHandle()
	{
		return ((CraftEnderDragon) _entity).getHandle();
	}

	public EnderDragon getEntity()
	{
		return _entity;
	}
}
