package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityBlaze;

import org.bukkit.entity.*;

public class DisguiseBlaze extends DisguiseMonster
{
	public DisguiseBlaze(org.bukkit.entity.Entity entity)
	{
		super(EntityType.BLAZE, entity);

		DataWatcher.a(16, new Byte((byte) 0), EntityBlaze.META_FIRE, (byte) 0);
	}

	public boolean bT()
	{
		return (DataWatcher.getByte(16) & 0x01) != 0;
	}

	public void a(boolean flag)
	{
		byte b0 = DataWatcher.getByte(16);

		if (flag)
			b0 = (byte) (b0 | 0x1);
		else
			b0 = (byte) (b0 | 0xFFFFFFFE);

		DataWatcher.watch(16, Byte.valueOf(b0), EntityBlaze.META_FIRE, b0);
	}

	public String getHurtSound()
	{
		return "mob.blaze.hit";
	}
}
