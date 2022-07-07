package mineplex.core.disguise.disguises;

import org.bukkit.entity.*;

public abstract class DisguiseMonster extends DisguiseCreature
{
	public DisguiseMonster(EntityType disguiseType, org.bukkit.entity.Entity entity)
	{
		super(disguiseType, entity);
	}
}
