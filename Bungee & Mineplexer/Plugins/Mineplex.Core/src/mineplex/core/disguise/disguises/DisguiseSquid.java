package mineplex.core.disguise.disguises;

import org.bukkit.entity.*;

public class DisguiseSquid extends DisguiseMonster
{
	public DisguiseSquid(org.bukkit.entity.Entity entity)
	{
		super(EntityType.SQUID, entity);
	}

	protected String getHurtSound()
	{
		return "damage.hit";
	}

	protected float getVolume()
	{
		return 0.4F;
	}
}
