package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityZombie;

import org.bukkit.entity.*;

public class DisguiseZombie extends DisguiseMonster
{
	public DisguiseZombie(Entity entity)
	{
		this(EntityType.ZOMBIE, entity);
	}

	public DisguiseZombie(EntityType disguiseType, Entity entity)
	{
		super(disguiseType, entity);

		DataWatcher.a(12, Byte.valueOf((byte) 0), EntityZombie.META_CHILD, false);
		DataWatcher.a(13, Byte.valueOf((byte) 0), EntityZombie.META_VILLAGER, false);
		DataWatcher.a(14, Byte.valueOf((byte) 0), EntityZombie.META_CONVERTING, false);
	}

	public boolean isBaby()
	{
		return DataWatcher.getByte(12) == 1;
	}

	public void setBaby(boolean baby)
	{
		DataWatcher.watch(12, Byte.valueOf((byte) (baby ? 1 : 0)), EntityZombie.META_CHILD, baby);
	}

	public boolean isVillager()
	{
		return DataWatcher.getByte(13) == 1;
	}

	public void setVillager(boolean villager)
	{
		DataWatcher.watch(13, Byte.valueOf((byte) (villager ? 1 : 0)), EntityZombie.META_VILLAGER, villager);
	}

	protected String getHurtSound()
	{
		return "mob.zombie.hurt";
	}
}
