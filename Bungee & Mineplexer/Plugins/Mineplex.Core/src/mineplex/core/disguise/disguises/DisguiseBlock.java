package mineplex.core.disguise.disguises;

import java.util.Random;

import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class DisguiseBlock extends DisguiseBase
{
	private static Random _random = new Random();

	private int _blockId;
	private int _blockData;

	public DisguiseBlock(Entity entity, int blockId, int blockData)
	{
		super(EntityType.FALLING_BLOCK, entity);

		_blockId = blockId;
		_blockData = blockData;
	}

	public DisguiseBlock(Entity entity, Material material, int data)
	{
		this(entity, material.getId(), data);
	}

	public int GetBlockId()
	{
		return _blockId;
	}

	public byte GetBlockData()
	{
		return (byte) _blockData;
	}

	@Override
	public Packet getSpawnPacket()
	{
		PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity();
		packet.a = getEntity().getId();
		packet.b = MathHelper.floor(getEntity().locX * 32.0D);
		packet.c = MathHelper.floor(getEntity().locY * 32.0D);
		packet.d = MathHelper.floor(getEntity().locZ * 32.0D);
		packet.h = MathHelper.d(getEntity().pitch * 256.0F / 360.0F);
		packet.i = MathHelper.d(getEntity().yaw * 256.0F / 360.0F);
		packet.j = 70;
		packet.k = _blockId | _blockData << 12;
		packet.uuid = getEntity().getUniqueID();

		double d1 = getEntity().motX;
		double d2 = getEntity().motY;
		double d3 = getEntity().motZ;
		double d4 = 3.9D;

		if (d1 < -d4)
			d1 = -d4;
		if (d2 < -d4)
			d2 = -d4;
		if (d3 < -d4)
			d3 = -d4;
		if (d1 > d4)
			d1 = d4;
		if (d2 > d4)
			d2 = d4;
		if (d3 > d4)
			d3 = d4;

		packet.e = ((int) (d1 * 8000.0D));
		packet.f = ((int) (d2 * 8000.0D));
		packet.g = ((int) (d3 * 8000.0D));

		return packet;
	}

	protected String getHurtSound()
	{
		return "damage.hit";
	}

	protected float getVolume()
	{
		return 1.0F;
	}

	protected float getPitch()
	{
		return (_random.nextFloat() - _random.nextFloat()) * 0.2F + 1.0F;
	}
}
