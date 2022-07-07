package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntitySkeleton;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton.SkeletonType;

public class DisguiseSkeleton extends DisguiseMutable
{
	private static final int SKELETON_ID = 51;
	private static final int WITHER_SKELETON_ID = 5;

	private SkeletonType type = SkeletonType.NORMAL;

	public DisguiseSkeleton(org.bukkit.entity.Entity entity)
	{
		super(EntityType.SKELETON, entity);

		DataWatcher.a(13, Byte.valueOf((byte) 0), EntitySkeleton.META_TYPE, 0);
	}

	public void SetSkeletonType(SkeletonType skeletonType)
	{
		DataWatcher.watch(13, Byte.valueOf((byte) skeletonType.getId()), EntitySkeleton.META_TYPE, skeletonType.getId());
		this.type = skeletonType;
		mutate();
	}

	public SkeletonType getSkeletonType()
	{
		return type;
	}

	protected String getHurtSound()
	{
		return "mob.skeleton.hurt";
	}

	// 1.11 and up require separate entity ids
	@Override
	protected int getTypeId(boolean separate)
	{
		return separate && type == SkeletonType.WITHER ? WITHER_SKELETON_ID : SKELETON_ID;
	}
}
