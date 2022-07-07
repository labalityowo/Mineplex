package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityTameableAnimal;

import org.bukkit.craftbukkit.libs.com.google.common.base.Optional;
import org.bukkit.entity.EntityType;

public abstract class DisguiseTameableAnimal extends DisguiseAnimal
{
	public DisguiseTameableAnimal(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);

		DataWatcher.a(16, Byte.valueOf((byte) 0), EntityTameableAnimal.META_SITTING_TAMED, (byte) 0);
		DataWatcher.a(17, "", EntityTameableAnimal.META_OWNER, Optional.absent());
	}

	public boolean isTamed()
	{
		return (DataWatcher.getByte(16) & 0x4) != 0;
	}

	public void setTamed(boolean tamed)
	{
		int i = DataWatcher.getByte(16);

		if (tamed)
			DataWatcher.watch(16, Byte.valueOf((byte) (i | 0x4)), EntityTameableAnimal.META_SITTING_TAMED, (byte) (i | 0x4));
		else
			DataWatcher.watch(16, Byte.valueOf((byte) (i | 0xFFFFFFFB)), EntityTameableAnimal.META_SITTING_TAMED,
					(byte) (i | 0xFFFFFFFB));
	}

	public boolean isSitting()
	{
		return (DataWatcher.getByte(16) & 0x1) != 0;
	}

	public void setSitting(boolean sitting)
	{
		int i = DataWatcher.getByte(16);

		if (sitting)
			DataWatcher.watch(16, Byte.valueOf((byte) (i | 0x1)), EntityTameableAnimal.META_SITTING_TAMED, (byte) (i | 0x1));
		else
			DataWatcher.watch(16, Byte.valueOf((byte) (i | 0xFFFFFFFE)), EntityTameableAnimal.META_SITTING_TAMED,
					(byte) (i | 0xFFFFFFFE));
	}

}
