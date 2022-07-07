package mineplex.core.disguise.disguises;

import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.Vector3f;

public class DisguiseArmorStand extends DisguiseInsentient
{
	public DisguiseArmorStand(org.bukkit.entity.Entity entity)
	{
		super(EntityType.ARMOR_STAND, entity);

		DataWatcher.a(10, (byte) 0, EntityArmorStand.META_ARMOR_OPTION, (byte) 0);

		DataWatcher.a(11, new Vector3f(0, 0, 0), EntityArmorStand.META_HEAD_POSE, new Vector3f(0, 0, 0));
		DataWatcher.a(12, new Vector3f(0, 0, 0), EntityArmorStand.META_BODY_POSE, new Vector3f(0, 0, 0));
		DataWatcher.a(13, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_ARM_POSE, new Vector3f(0, 0, 0));
		DataWatcher.a(14, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_ARM_POSE, new Vector3f(0, 0, 0));
		DataWatcher.a(15, new Vector3f(0, 0, 0), EntityArmorStand.META_LEFT_LEG_POSE, new Vector3f(0, 0, 0));
		DataWatcher.a(16, new Vector3f(0, 0, 0), EntityArmorStand.META_RIGHT_LEG_POSE, new Vector3f(0, 0, 0));

		// Rotations are from -360 to 360
	}

	private Vector3f convert(Vector vector)
	{
		return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
	}

	private Vector convert(Vector3f vector)
	{
		return new Vector(vector.getX(), vector.getY(), vector.getZ());
	}

	public Vector getHeadPosition()
	{
		return convert(DataWatcher.h(11));
	}

	protected String getHurtSound()
	{
		return null;
	}

	@Override
	public Packet getSpawnPacket()
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		packet.a = getEntity().getId();
		packet.b = (byte) 30;
		packet.c = (int) MathHelper.floor(getEntity().locX * 32.0D);
		packet.d = (int) MathHelper.floor(getEntity().locY * 32.0D);
		packet.e = (int) MathHelper.floor(getEntity().locZ * 32.0D);
		packet.i = (byte) ((int) (getEntity().yaw * 256.0F / 360.0F));
		packet.j = (byte) ((int) (getEntity().pitch * 256.0F / 360.0F));
		packet.k = (byte) ((int) (getEntity().yaw * 256.0F / 360.0F));
		packet.uuid = getEntity().getUniqueID();

		double var2 = 3.9D;
		double var4 = 0;
		double var6 = 0;
		double var8 = 0;

		if (var4 < -var2)
		{
			var4 = -var2;
		}

		if (var6 < -var2)
		{
			var6 = -var2;
		}

		if (var8 < -var2)
		{
			var8 = -var2;
		}

		if (var4 > var2)
		{
			var4 = var2;
		}

		if (var6 > var2)
		{
			var6 = var2;
		}

		if (var8 > var2)
		{
			var8 = var2;
		}

		packet.f = (int) (var4 * 8000.0D);
		packet.g = (int) (var6 * 8000.0D);
		packet.h = (int) (var8 * 8000.0D);

		packet.l = DataWatcher;
		packet.m = DataWatcher.b();

		return packet;
	}

	public void setBodyPosition(Vector vector)
	{
		DataWatcher.watch(12, convert(vector), EntityArmorStand.META_BODY_POSE, convert(vector));
	}

	public void setHasArms()
	{
		DataWatcher.watch(10, (byte) (DataWatcher.getByte(10) | 4), EntityArmorStand.META_ARMOR_OPTION,
				(byte) (DataWatcher.getByte(10) | 4));
	}

	public void setHeadPosition(Vector vector)
	{
		DataWatcher.watch(11, convert(vector), EntityArmorStand.META_HEAD_POSE, convert(vector));
	}

	public void setLeftArmPosition(Vector vector)
	{
		DataWatcher.watch(13, convert(vector), EntityArmorStand.META_LEFT_ARM_POSE, convert(vector));
	}

	public void setLeftLegPosition(Vector vector)
	{
		DataWatcher.watch(15, convert(vector), EntityArmorStand.META_LEFT_LEG_POSE, convert(vector));
	}

	public void setRemoveBase()
	{
		DataWatcher.watch(10, (byte) (DataWatcher.getByte(10) | 8), EntityArmorStand.META_ARMOR_OPTION,
				(byte) (DataWatcher.getByte(10) | 8));
	}

	public void setRightArmPosition(Vector vector)
	{
		DataWatcher.watch(14, convert(vector), EntityArmorStand.META_RIGHT_ARM_POSE, convert(vector));
	}

	public void setRightLegPosition(Vector vector)
	{
		DataWatcher.watch(16, convert(vector), EntityArmorStand.META_RIGHT_LEG_POSE, convert(vector));
	}

	public void setSmall()
	{
		DataWatcher.watch(10, (byte) (DataWatcher.getByte(10) | 1), EntityArmorStand.META_ARMOR_OPTION,
				(byte) (DataWatcher.getByte(10) | 1));
	}

	public void setGravityEffected()
	{
		DataWatcher.watch(10, (byte) (DataWatcher.getByte(10) | 2), EntityArmorStand.META_ARMOR_OPTION,
				(byte) (DataWatcher.getByte(10) | 2));
	}
}
