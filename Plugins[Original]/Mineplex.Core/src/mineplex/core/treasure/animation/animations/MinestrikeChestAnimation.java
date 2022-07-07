package mineplex.core.treasure.animation.animations;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class MinestrikeChestAnimation extends TreasureOpenAnimation
{

	public MinestrikeChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.SAND, 0, Material.SANDSTONE, 0);
	}

	@Override
	public void onTick()
	{
		if (getTicks() % 3 == 0)
		{
			for (Location location : getTreasureLocation().getChestLocations())
			{
				UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.OBSIDIAN, 0), location.clone().add(0, 0.4, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);
			}
		}

		if (getTicks() > 50)
		{
			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{
		for (Location location : getTreasureLocation().getChestLocations())
		{
			location = location.clone();
			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getTreasureLocation().getChest())));
			location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.CHEST);
			createChestAt(location, Material.CHEST);

			location.add(0, 1, 0);

			UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, location, 0, 0, 0, 0.1F, 1, ViewDist.NORMAL);
		}
	}
}
