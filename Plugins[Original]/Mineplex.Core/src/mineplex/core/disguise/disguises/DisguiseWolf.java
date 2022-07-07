package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityWolf;

import org.bukkit.entity.*;

public class DisguiseWolf extends DisguiseTameableAnimal
{
	public DisguiseWolf(org.bukkit.entity.Entity entity)
	{
		super(EntityType.WOLF, entity);

		DataWatcher.a(18, new Float(20F), EntityWolf.META_WOLF_HEALTH, 20F);
		DataWatcher.a(19, new Byte((byte) 0), EntityWolf.META_BEGGING, false);
		DataWatcher.a(20, new Byte((byte) 14), EntityWolf.META_COLLAR, 14);
	}

	public boolean isAngry()
	{
		return (DataWatcher.getByte(16) & 0x2) != 0;
	}

	public void setAngry(boolean angry)
	{
		byte b0 = DataWatcher.getByte(16);

		if (angry)
			DataWatcher.watch(16, Byte.valueOf((byte) (b0 | 0x2)), EntityWolf.META_SITTING_TAMED, (byte) (b0 | 0x2));
		else
			DataWatcher
					.watch(16, Byte.valueOf((byte) (b0 & 0xFFFFFFFD)), EntityWolf.META_SITTING_TAMED, (byte) (b0 & 0xFFFFFFFD));
	}

	public int getCollarColor()
	{
		return DataWatcher.getByte(20) & 0xF;
	}

	public void setCollarColor(int i)
	{
		DataWatcher.watch(20, Byte.valueOf((byte) (i & 0xF)), EntityWolf.META_COLLAR, (i & 0xF));
	}

	public void m(boolean flag)
	{
		if (flag)
			DataWatcher.watch(19, Byte.valueOf((byte) 1), EntityWolf.META_BEGGING, flag);
		else
			DataWatcher.watch(19, Byte.valueOf((byte) 0), EntityWolf.META_BEGGING, flag);
	}

	public boolean ce()
	{
		return DataWatcher.getByte(19) == 1;
	}

	protected String getHurtSound()
	{
		return "mob.wolf.hurt";
	}
}
