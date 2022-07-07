package mineplex.core.treasure.animation.animations;

import java.awt.Color;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class StPatricksChestAnimation extends TreasureOpenAnimation
{

	private static final ItemStack HELMET = new ItemStack(Material.WOOL, 1, (short) 5);
	private static final int RADIUS = 3;
	private static final double OFFSET_THETA = Math.PI / 10;
	private static final double DELTA_THETA = Math.PI / 40;
	private static final double TOTAL_THETA = 2 * Math.PI;
	private static final ShapeWings CLOVER_BLACK = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '#', ShapeWings.DEFAULT_ROTATION, ShapeWings.FOUR_LEAF_CLOVER);
	private static final ShapeWings CLOVER_DARK_GREEN = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '%', ShapeWings.DEFAULT_ROTATION, ShapeWings.FOUR_LEAF_CLOVER);
	private static final ShapeWings CLOVER_GREEN = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(1, 1, 1), 1, 0, '*', ShapeWings.DEFAULT_ROTATION, ShapeWings.FOUR_LEAF_CLOVER);

	private double _deltaTheta;
	private double _totalTheta;

	public StPatricksChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.WOOL, 13, Material.WOOL, 5);

		List<Location> chests = getTreasureLocation().getChestLocations();
		_deltaTheta = Math.PI * 2 / (double) chests.size();

		for (Location location : chests)
		{
			ArmorStand stand = spawnArmourStand(location);
			stand.setHelmet(HELMET);
		}
	}

	@Override
	public void onTick()
	{
		_totalTheta += DELTA_THETA;
		
		if (getTicks() % 5 == 0)
		{
			Location center = getTreasureLocation().getChest().clone().add(0, 5, 0);
			CLOVER_BLACK.displayColored(center, Color.BLACK);
			CLOVER_DARK_GREEN.displayColored(center, new Color(0, 100, 0));
			CLOVER_GREEN.displayColored(center, Color.GREEN);
		}

		int index = 0;
		for (ArmorStand stand : _stands)
		{
			Location location = getTreasureLocation().getChest().clone();
			double x = RADIUS * Math.cos(_totalTheta + OFFSET_THETA + index * _deltaTheta);
			double z = RADIUS * Math.sin(_totalTheta + OFFSET_THETA + index * _deltaTheta);

			location.add(x, 0, z);

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getTreasureLocation().getChest())));

			stand.teleport(location);
			UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, location.add(0, 1, 0), 0.25F, 0.25F, 0.25F, 0.1F, 1, ViewDist.NORMAL);

			index++;
		}

		if (_totalTheta >= TOTAL_THETA)
		{
			for (ArmorStand stand : _stands)
			{
				Location location = stand.getLocation();
				stand.getWorld().playSound(location, Sound.ANVIL_LAND, 1, 0.8F);
				UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, location, 0, 0, 0, 0.1F, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, location, 0.8F, 0.8F, 0.8F, 0.1F, 20, ViewDist.NORMAL);
				stand.remove();

				createChestAt(location, Material.CHEST);
			}

			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
