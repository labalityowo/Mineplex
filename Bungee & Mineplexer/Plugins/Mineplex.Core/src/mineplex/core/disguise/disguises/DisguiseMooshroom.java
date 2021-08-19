package mineplex.core.disguise.disguises;

import org.bukkit.entity.*;

public class DisguiseMooshroom extends DisguiseAnimal
{
	public DisguiseMooshroom(org.bukkit.entity.Entity entity)
	{
		super(EntityType.MUSHROOM_COW, entity);
	}
	
	public String getHurtSound()
	{
		return "mob.cow.hurt";
	}
}
