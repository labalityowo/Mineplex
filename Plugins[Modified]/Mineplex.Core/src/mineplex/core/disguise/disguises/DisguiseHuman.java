package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityHuman;
import org.bukkit.entity.EntityType;

public abstract class DisguiseHuman extends DisguiseLiving
{
	public DisguiseHuman(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);

		DataWatcher.a(10, (byte) 127, EntityHuman.META_SKIN, (byte) 127);
		DataWatcher.a(16, (byte) 1, EntityHuman.META_CAPE, (byte) 1);
		DataWatcher.a(17, 0F, EntityHuman.META_SCALED_HEALTH, 0F);
		DataWatcher.a(18, 0, EntityHuman.META_SCORE, 0);
	}
}
