package mineplex.core.disguise.disguises;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Magma cubes are essentially identical to slimes for disguise purposes
 */
public class DisguiseMagmaCube extends DisguiseSlime
{
	public DisguiseMagmaCube(Entity entity)
	{
		super(EntityType.MAGMA_CUBE, entity);
	}
}