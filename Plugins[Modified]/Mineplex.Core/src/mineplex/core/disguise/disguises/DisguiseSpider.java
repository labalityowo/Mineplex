package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntitySpider;

import org.bukkit.entity.*;

public class DisguiseSpider extends DisguiseMonster
{
	public DisguiseSpider(org.bukkit.entity.Entity entity)
	{
		super(EntityType.SPIDER, entity);

		DataWatcher.a(16, new Byte((byte) 0), EntitySpider.META_CLIMBING, (byte) 0);
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
			b0 = (byte) (b0 & 0xFFFFFFFE);

		DataWatcher.watch(16, Byte.valueOf(b0), EntitySpider.META_CLIMBING, b0);
	}

	protected String getHurtSound()
	{
		return "mob.spider.say";
	}
}
