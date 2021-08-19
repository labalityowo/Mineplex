package mineplex.core.treasure.animation.animations;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class IlluminatedChestAnimation extends TreasureOpenAnimation
{

	private static final ItemStack HELMET = new ItemStack(Material.SEA_LANTERN);
	private static final int RADIUS = 3;
	private static final double OFFSET_THETA = Math.PI / 10;
	private static final double DELTA_RADIUS = 0.1;
	private static final float DELTA_YAW = 6F;

	private float _yaw;
	private double _radius;
	private double _deltaTheta;

	public IlluminatedChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.PRISMARINE, 0, Material.PRISMARINE, 1);

		List<Location> chests = getTreasureLocation().getChestLocations();
		_deltaTheta = Math.PI * 2 / (double) chests.size();

		for (Location location : chests)
		{
			ArmorStand stand = spawnArmourStand(location.clone());
			stand.setHelmet(HELMET);
		}
	}

	@Override
	public void onTick()
	{
		int index = 0;
		for (ArmorStand stand : _stands)
		{
			Location location = getTreasureLocation().getChest().clone().subtract(0, 1, 0);
			double x = _radius * Math.cos(OFFSET_THETA + index * _deltaTheta);
			double z = _radius * Math.sin(OFFSET_THETA + index * _deltaTheta);

			location.add(x, 0, z);
			location.setYaw(_yaw );

			stand.teleport(location);
			location.add(0, 1, 0);
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, location, 0.25F, 0.25F, 0.25F, 0.1F, 1, ViewDist.NORMAL);
			UtilParticle.PlayParticleToAll(ParticleType.PORTAL, location, 0.25F, 0.25F, 0.25F, 0.1F, 3, ViewDist.NORMAL);

			index++;
		}

		if (getTicks() % 2 == 0)
		{
			getTreasureLocation().getChest().getWorld().playSound(getTreasureLocation().getChest(), Sound.NOTE_PLING, 1, (float) (_radius / 3 + 0.3F));
		}

		_yaw += DELTA_YAW;
		_radius += DELTA_RADIUS;
		if (_radius >= RADIUS)
		{
			for (ArmorStand stand : _stands)
			{
				Location location = stand.getLocation().add(0, 1, 0);
				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getTreasureLocation().getChest())));
				location.getWorld().playSound(location, Sound.EXPLODE, 1, 1);
				UtilParticle.PlayParticleToAll(ParticleType.PORTAL, location, 0.5F, 0.5F, 0.5F, 0.5F, 10, ViewDist.NORMAL);
				stand.remove();

				createChestAt(location, Material.ENDER_CHEST);
			}

			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
