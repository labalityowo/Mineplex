package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Slimes have an odd type hierarchy, but they're essentially creatures as far as disguises are concerned.
 */
public class DisguiseSlime extends DisguiseCreature
{
	public DisguiseSlime(Entity entity)
	{
		this(EntityType.SLIME, entity);
	}

	/**
	 * For magma cubes
	 */
	protected DisguiseSlime(EntityType type, Entity entity)
	{
		super(type, entity);
		DataWatcher.a(16, new Byte((byte) 1), EntitySlime.META_SIZE, 1);
	}

	public void SetSize(int i)
	{
		DataWatcher.watch(16, new Byte((byte) i), EntitySlime.META_SIZE, i);
	}

	public int GetSize()
	{
		return DataWatcher.getByte(16);
	}

	protected String getHurtSound()
	{
		return "mob.slime." + (GetSize() > 1 ? "big" : "small");
	}

	protected float getVolume()
	{
		return 0.4F * (float) GetSize();
	}
}
