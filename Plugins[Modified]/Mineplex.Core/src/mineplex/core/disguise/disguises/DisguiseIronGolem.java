package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityIronGolem;

import org.bukkit.entity.*;

public class DisguiseIronGolem extends DisguiseGolem
{
	public DisguiseIronGolem(org.bukkit.entity.Entity entity)
	{
		super(EntityType.IRON_GOLEM, entity);

		DataWatcher.a(16, Byte.valueOf((byte) 0), EntityIronGolem.META_PLAYER_CREATED, (byte) 0);
	}

	public boolean bW()
	{
		return (DataWatcher.getByte(16) & 0x1) != 0;
	}

	public void setPlayerCreated(boolean flag)
	{
		byte b0 = DataWatcher.getByte(16);

		if (flag)
			DataWatcher.watch(16, Byte.valueOf((byte) (b0 | 0x1)), EntityIronGolem.META_PLAYER_CREATED, (byte) (b0 | 0x1));
		else
			DataWatcher.watch(16, Byte.valueOf((byte) (b0 & 0xFFFFFFFE)), EntityIronGolem.META_PLAYER_CREATED,
					(byte) (b0 & 0xFFFFFFFE));
	}

	protected String getHurtSound()
	{
		return "mob.irongolem.hit";
	}
}
