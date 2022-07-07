package mineplex.core.treasure.animation.animations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.util.TreasureUtil;

public class TrickOrTreatChestAnimation extends TreasureOpenAnimation
{

	private boolean _tick;

	public TrickOrTreatChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.WOOL, 15, Material.WOOL, 1);
	}

	@Override
	public void onTick()
	{
		if (getTicks() % 10 == 0)
		{
			for (Location location : getTreasureLocation().getChestLocations())
			{
				if (Math.random() > 0.7)
				{
					location.getWorld().playSound(location, Sound.ORB_PICKUP, 1, 0.5F);
				}

				MapUtil.QuickChangeBlockAt(
						location,
						_tick ? Material.PUMPKIN : Material.JACK_O_LANTERN,
						TreasureUtil.getPumpkinFacing(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getTreasureLocation().getChest())))
				);
			}

			_tick = !_tick;
		}

		if (getTicks() % 3 == 0)
		{
			for (Location location : getTreasureLocation().getChestLocations())
			{
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, location.clone().add(0, 1, 0), 0.2F, 0.2F, 0.2F, 0.05F, 2, ViewDist.NORMAL);
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
			createChestAt(location, Material.ENDER_CHEST);

			location.add(0, 1, 0);

			for (int i = 0; i < 3; i++)
			{
				Bat bat = spawnEntity(location, Bat.class, true);
				bat.setAwake(true);
			}

			UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, location, 0, 0, 0, 0.1F, 1, ViewDist.NORMAL);
		}
	}
}
