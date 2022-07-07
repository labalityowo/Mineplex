package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityAgeable;

import org.bukkit.entity.*;

public abstract class DisguiseAgeable extends DisguiseCreature
{
	public DisguiseAgeable(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);
		
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
}
