package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityWither;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import mineplex.core.common.util.UtilMath;

public class DisguiseWither extends DisguiseMonster
{
	public DisguiseWither(org.bukkit.entity.Entity entity)
	{
		super(EntityType.WITHER, entity);

		DataWatcher.a(17, new Integer(0), EntityWither.META_INVUL_TIME, 0);
		DataWatcher.a(18, new Integer(0), EntityWither.META_TARGET_1, 0);
		DataWatcher.a(19, new Integer(0), EntityWither.META_TARGET_2, 0);
		DataWatcher.a(20, new Integer(0), EntityWither.META_TARGET_3, 0);
	}

	public int getInvulTime()
	{
		return DataWatcher.getInt(20);
	}

	public void setInvulTime(int i)
	{
		DataWatcher.watch(17, Integer.valueOf(i), EntityWither.META_INVUL_TIME, i);
		DataWatcher.watch(20, Integer.valueOf(i), EntityWither.META_INVUL_TIME, i);
	}

	public int t(int i)
	{
		return DataWatcher.getInt(17 + i);
	}
}
