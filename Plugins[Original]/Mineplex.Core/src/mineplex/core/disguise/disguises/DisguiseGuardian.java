package mineplex.core.disguise.disguises;

import net.minecraft.server.v1_8_R3.EntityGuardian;

import org.bukkit.entity.EntityType;

public class DisguiseGuardian extends DisguiseMutable
{
	private static final int GUARDIAN_ID = 68;
	private static final int ELDER_GUARDIAN_ID = 4;

	private int target = 0;
	private boolean elder = false;

	public DisguiseGuardian(org.bukkit.entity.Entity entity)
	{
		super(EntityType.GUARDIAN, entity);
		DataWatcher.a(16, 0, EntityGuardian.META_ELDER, (byte) 0);
		DataWatcher.a(17, 0, EntityGuardian.META_TARGET, 0);
	}

	public void setTarget(int target)
	{
		this.target = target;

		DataWatcher.watch(17, target, EntityGuardian.META_TARGET, target);
	}

	public void setElder(boolean elder)
	{
		this.elder = elder;

		int oldValue = DataWatcher.getInt(16);
		int newValue = elder ? oldValue | 4 : oldValue & ~4;

		DataWatcher.watch(16, Integer.valueOf(newValue), EntityGuardian.META_ELDER, (byte) newValue);

		mutate();
	}

	public boolean isElder()
	{
		return elder;
	}

	public int getTarget()
	{
		return target;
	}

	protected String getHurtSound()
	{
		if (isElder())
		{
			return "mob.guardian.elder.hit";
		}

		return "mob.guardian.hit";
	}

	@Override
	protected int getTypeId(boolean separate)
	{
		return separate && isElder() ? ELDER_GUARDIAN_ID : GUARDIAN_ID;
	}
}
