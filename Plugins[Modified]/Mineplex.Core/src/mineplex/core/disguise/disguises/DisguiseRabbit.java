package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityRabbit;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit.Type;

public class DisguiseRabbit extends DisguiseAnimal
{

	private Type _type;

	public DisguiseRabbit(org.bukkit.entity.Entity entity)
	{
		super(EntityType.RABBIT, entity);

		_type = Type.BROWN;
		DataWatcher.a(18, Byte.valueOf((byte) 0), EntityRabbit.META_TYPE, 0);
	}

	public void setType(Type type)
	{
		_type = type;

		int id = type.ordinal();
		DataWatcher.watch(18, (byte) id, EntityRabbit.META_TYPE, id);
	}

	public Type getType()
	{
		return _type;
	}

	@Override
	public Packet getSpawnPacket()
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		packet.a = getEntity().getId();
		packet.b = (byte) 101;
		packet.c = (int) MathHelper.floor(getEntity().locX * 32D);
		packet.d = (int) MathHelper.floor(getEntity().locY * 32.0D);
		packet.e = (int) MathHelper.floor(getEntity().locZ * 32D);
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
}
