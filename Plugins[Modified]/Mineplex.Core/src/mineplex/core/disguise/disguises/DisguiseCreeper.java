package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityCreeper;

import org.bukkit.entity.*;

public class DisguiseCreeper extends DisguiseMonster
{
	public DisguiseCreeper(org.bukkit.entity.Entity entity)
	{
		super(EntityType.CREEPER, entity);

		DataWatcher.a(16, Byte.valueOf((byte) -1), EntityCreeper.META_FUSE_STATE, -1);
		DataWatcher.a(17, Byte.valueOf((byte) 0), EntityCreeper.META_POWERED, false);
	}

	public boolean IsPowered()
	{
		return DataWatcher.getByte(17) == 1;
	}

	public void SetPowered(boolean powered)
	{
		DataWatcher.watch(17, Byte.valueOf((byte) (powered ? 1 : 0)), EntityCreeper.META_POWERED, powered);
	}

	public int bV()
	{
		return DataWatcher.getByte(16);
	}

	public void a(int i)
	{
		DataWatcher.watch(16, Byte.valueOf((byte) i), EntityCreeper.META_FUSE_STATE, i);
	}

	protected String getHurtSound()
	{
		return "mob.creeper.say";
	}
}
