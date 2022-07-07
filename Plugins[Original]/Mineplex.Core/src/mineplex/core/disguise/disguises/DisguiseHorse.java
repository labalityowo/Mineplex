package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityAgeable;
import net.minecraft.server.v1_8_R3.EntityHorse;

import org.bukkit.craftbukkit.libs.com.google.common.base.Optional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class DisguiseHorse extends DisguiseMutable
{
	private static final int HORSE_ID = 100;
	private static final int DONKEY_ID = 31;
	private static final int MULE_ID = 32;
	private static final int ZOMBIE_HORSE_ID = 29;
	private static final int SKELETON_HORSE_ID = 28;

	private Horse.Variant variant = Horse.Variant.HORSE;

	public DisguiseHorse(org.bukkit.entity.Entity entity)
	{
		super(EntityType.HORSE, entity);

		DataWatcher.a(16, Integer.valueOf(0), EntityHorse.META_HORSE_STATE, (byte) 0);
		DataWatcher.a(19, Byte.valueOf((byte) 0), EntityHorse.META_TYPE, 0);
		DataWatcher.a(20, Integer.valueOf(0), EntityHorse.META_VARIANT, 0);
		DataWatcher.a(21, String.valueOf(""), EntityHorse.META_OWNER, Optional.absent());
		DataWatcher.a(22, Integer.valueOf(0), EntityHorse.META_ARMOR, 0);

		DataWatcher.a(12, new Byte((byte)0), EntityAgeable.META_BABY, false);
	}

	public boolean isBaby()
	{
		return DataWatcher.getByte(12) < 0;
	}

	public void setBaby()
	{
		DataWatcher.watch(12, new Byte((byte) ( -1 )), EntityAgeable.META_BABY, true);
	}

	public void setType(Horse.Variant horseType)
	{
		DataWatcher.watch(19, Byte.valueOf((byte) horseType.ordinal()), EntityHorse.META_TYPE, horseType.ordinal());
		this.variant = horseType;
		mutate();
	}

	public Horse.Variant getType()
	{
		return Horse.Variant.values()[DataWatcher.getByte(19)];
	}

	public void setVariant(Horse.Color color)
	{
		DataWatcher.watch(20, Integer.valueOf(color.ordinal()), EntityHorse.META_VARIANT, color.ordinal());
	}

	public Horse.Color getVariant()
	{
		return Horse.Color.values()[DataWatcher.getInt(20)];
	}

	public void kick()
	{
		b(32, false);
		b(64, true);
	}

	public void stopKick()
	{
		b(64, false);
	}

	private void b(int i, boolean flag)
	{
		int j = DataWatcher.getInt(16);

		if (flag)
			DataWatcher.watch(16, Integer.valueOf(j | i), EntityHorse.META_HORSE_STATE, (byte) (j | i));
		else
			DataWatcher.watch(16, Integer.valueOf(j & (i ^ 0xFFFFFFFF)), EntityHorse.META_HORSE_STATE,
					(byte) (j & (i ^ 0xFFFFFFFF)));
	}

	public int getArmor()
	{
		return DataWatcher.getInt(22);
	}

	public void setArmor(int i)
	{
		DataWatcher.watch(22, Integer.valueOf(i), EntityHorse.META_ARMOR, i);
	}

	// 1.11 and up require separate entity ids
	@Override
	protected int getTypeId(boolean separate)
	{
		if (separate && variant != Horse.Variant.HORSE)
		{
			switch (variant)
			{
				case DONKEY: return DONKEY_ID;
				case MULE: return MULE_ID;
				case UNDEAD_HORSE: return ZOMBIE_HORSE_ID;
				case SKELETON_HORSE: return SKELETON_HORSE_ID;
				default: return HORSE_ID;
			}
		}

		return HORSE_ID;
	}
}
