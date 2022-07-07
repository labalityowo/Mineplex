package mineplex.core.disguise.disguises;

import java.util.Arrays;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EntityEnderman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.minecraft.server.v1_8_R3.PotionBrewer;

import org.bukkit.craftbukkit.libs.com.google.common.base.Optional;
import org.bukkit.entity.EntityType;

public class DisguiseEnderman extends DisguiseMonster
{
	public DisguiseEnderman(org.bukkit.entity.Entity entity)
	{
		super(EntityType.ENDERMAN, entity);

		DataWatcher.a(16, new Short((short) 0), EntityEnderman.META_BLOCK, Optional.<IBlockData> absent());
		DataWatcher.a(17, new Byte((byte) 0), EntityEnderman.META_BLOCK, Optional.<IBlockData> absent());
		DataWatcher.a(18, new Byte((byte) 0), EntityEnderman.META_ANGRY, false);

		int i = PotionBrewer.a(Arrays.asList(new MobEffect(MobEffectList.FIRE_RESISTANCE.id, 777)));
		DataWatcher.watch(8, Byte.valueOf((byte) (PotionBrewer.b(Arrays.asList(new MobEffect(MobEffectList.FIRE_RESISTANCE.id,
				777))) ? 1 : 0)), EntityLiving.META_AMBIENT_POTION, PotionBrewer.b(Arrays.asList(new MobEffect(
				MobEffectList.FIRE_RESISTANCE.id, 777))));
		DataWatcher.watch(7, Integer.valueOf(i), EntityLiving.META_POTION_COLOR, i);
	}

	public void UpdateDataWatcher()
	{
		super.UpdateDataWatcher();

		DataWatcher.watch(0, Byte.valueOf((byte) (DataWatcher.getByte(0) & ~(1 << 0))), getEntity().META_ENTITYDATA,
				(byte) (DataWatcher.getByte(0) & ~(1 << 0)));
		DataWatcher.watch(16, DataWatcher.getShort(16), EntityEnderman.META_BLOCK, getBlock(DataWatcher.getShort(16)));
	}

	private Optional<IBlockData> getBlock(int i)
	{
		Block b = Block.getById(i);
		if (b != null && b != Blocks.AIR)
		{
			return Optional.fromNullable(b.getBlockData());
		}

		return Optional.fromNullable(null);
	}

	public void SetCarriedId(int i)
	{
		DataWatcher.watch(16, new Short((short) (i & 0xFF)), EntityEnderman.META_BLOCK, getBlock(i));
	}

	public int GetCarriedId()
	{
		return DataWatcher.getByte(16);
	}

	public void SetCarriedData(int i)
	{
		DataWatcher.watch(17, Byte.valueOf((byte) (i & 0xFF)), EntityEnderman.META_BLOCK, getBlock(0));
	}

	public int GetCarriedData()
	{
		return DataWatcher.getByte(17);
	}

	public boolean bX()
	{
		return DataWatcher.getByte(18) > 0;
	}

	public void a(boolean flag)
	{
		DataWatcher.watch(18, Byte.valueOf((byte) (flag ? 1 : 0)), EntityEnderman.META_ANGRY, flag);
	}

	protected String getHurtSound()
	{
		return "mob.endermen.hit";
	}
}
