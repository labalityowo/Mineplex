package nautilus.game.arcade.game.games.speedbuilders.data;

import org.bukkit.entity.EntityType;

public class MobData
{

	public EntityType EntityType;

	public int DX;
	public int DY;
	public int DZ;

	public MobData(EntityType entityType, int dx, int dy, int dz)
	{
		EntityType = entityType;
		
		DX = dx;
		DY = dy;
		DZ = dz;
	}

}
