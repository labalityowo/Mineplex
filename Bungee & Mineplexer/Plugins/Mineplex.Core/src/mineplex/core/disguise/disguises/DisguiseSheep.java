package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntitySheep;

import org.bukkit.DyeColor;
import org.bukkit.entity.*;

public class DisguiseSheep extends DisguiseAnimal
{
	public DisguiseSheep(org.bukkit.entity.Entity entity)
	{
		super(EntityType.SHEEP, entity);

		DataWatcher.a(16, new Byte((byte) 0), EntitySheep.META_WOOL_STATE, (byte) 0);
	}

	public boolean isSheared()
	{
		return (DataWatcher.getByte(16) & 16) != 0;
	}

	public void setSheared(boolean sheared)
	{
		byte b0 = DataWatcher.getByte(16);

		if (sheared)
			DataWatcher.watch(16, Byte.valueOf((byte) (b0 | 16)), EntitySheep.META_WOOL_STATE, (byte) (b0 | 16));
		else
			DataWatcher.watch(16, Byte.valueOf((byte) (b0 & -17)), EntitySheep.META_WOOL_STATE, (byte) (b0 & -17));
	}

	public int getColor()
	{
		return DataWatcher.getByte(16) & 15;
	}

	@SuppressWarnings("deprecation")
	public void setColor(DyeColor color)
	{
		byte b0 = DataWatcher.getByte(16);

		DataWatcher.watch(16, Byte.valueOf((byte) (b0 & 240 | color.getWoolData() & 15)), EntitySheep.META_WOOL_STATE,
				(byte) (b0 & 240 | color.getWoolData() & 15));
	}
}
