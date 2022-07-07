package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityOcelot;

import org.bukkit.entity.*;

public class DisguiseCat extends DisguiseTameableAnimal
{
	public DisguiseCat(org.bukkit.entity.Entity entity)
	{
		super(EntityType.OCELOT, entity);

		DataWatcher.a(18, Byte.valueOf((byte) 0), EntityOcelot.META_TYPE, 0);
	}

	public int getCatType()
	{
		return DataWatcher.getByte(18);
	}

	public void setCatType(int i)
	{
		DataWatcher.watch(18, Byte.valueOf((byte) i), EntityOcelot.META_TYPE, i);
	}

	protected String getHurtSound()
	{
		return "mob.cat.hitt";
	}
}
