package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityBat;

import org.bukkit.entity.*;

public class DisguiseBat extends DisguiseCreature
{
	public DisguiseBat(org.bukkit.entity.Entity entity)
	{
		super(EntityType.BAT, entity);

		DataWatcher.a(16, new Byte((byte) 0), EntityBat.META_UPSIDEDOWN, (byte) 0);
	}

	public boolean isSitting()
	{
		return (DataWatcher.getByte(16) & 0x1) != 0;
	}

	public void setSitting(boolean paramBoolean)
	{
		int i = DataWatcher.getByte(16);
		if (paramBoolean)
			DataWatcher.watch(16, Byte.valueOf((byte) (i | 0x1)), EntityBat.META_UPSIDEDOWN, (byte) (i | 0x1));
		else
			DataWatcher.watch(16, Byte.valueOf((byte) (i & 0xFFFFFFFE)), EntityBat.META_UPSIDEDOWN, (byte) (i & 0xFFFFFFFE));
	}

	public String getHurtSound()
	{
		return "mob.bat.hurt";
	}
}