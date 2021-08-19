package mineplex.core.gadget.gadgets.balloons;

import net.minecraft.server.v1_8_R3.EntitySlime;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;

public class BalloonEntity extends EntitySlime
{

	public static BalloonEntity spawn(ArmorStand host, Player holder, boolean small)
	{
		BalloonEntity entity = new BalloonEntity(host, holder, small);
		UtilEnt.spawnEntity(entity, host.getLocation());
		return entity;
	}

	private final ArmorStand _host;
	private final Player _holder;
	private final boolean _small;
	private final double _height;

	private BalloonEntity(ArmorStand host, Player holder, boolean small)
	{
		super(((CraftWorld) host.getWorld()).getHandle());

		_host = host;
		_holder = holder;
		_small = small;
		_height = small ? 0.2 : 1.5;
	}

	@Override
	public void t_()
	{
		Location host = _host.getLocation(), holder = _holder.getEyeLocation(), teleport = host.clone();
		double offsetXZ = UtilMath.offset2dSquared(holder, host), offsetY = host.getY() - holder.getY();

		if (offsetY < 0.5)
		{
			teleport.add(0, 0.1, 0);
		}
		else if (offsetY > (_small ? 2 : 1))
		{
			teleport.subtract(0, 0.1, 0);
		}

		if (offsetXZ > 2)
		{
			teleport.add(UtilAlg.getTrajectory2d(host, holder).multiply(0.1 * offsetXZ));
		}

		teleport.add(UtilMath.random(-0.1, 0.1), UtilMath.random(-0.1, 0.1), UtilMath.random(-0.1, 0.1));
		teleport.setYaw((teleport.getYaw() + UtilMath.rRange(-2, 2)) % 360);

		((CraftEntity) _host).getHandle().setPositionRotation(teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getYaw(), 0);
		setPosition(teleport.getX(), teleport.getY() + _height, teleport.getZ());

		LivingEntity entity = (LivingEntity) getBukkitEntity();

		entity.setLeashHolder(_holder);
		entity.setShouldBreakLeash(false);
	}

	public void remove()
	{
		_host.remove();
		getBukkitEntity().remove();
	}

	public boolean isPartOfEntity(Entity entity)
	{
		return entity.equals(this.getBukkitEntity()) || entity.equals(_host);
	}
}
