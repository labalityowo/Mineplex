package mineplex.core.treasure.animation.animations;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class GingerbreadChestAnimation extends TreasureOpenAnimation
{

	private static final ItemStack HELMET = SkinData.PRESENT.getSkull();
	private static final int RADIUS = 3;
	private static final double DELTA_THETA = Math.PI / 40;
	private static final double TOTAL_THETA = 2 * Math.PI;
	private static final double DELTA_Y = -1;
	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.STAR)
			.withColor(Color.YELLOW)
			.withFade(Color.RED, Color.GREEN)
			.withFlicker()
			.build();

	private final Set<Location> _chests;
	private double _totalTheta;

	public GingerbreadChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);

		_chests = new HashSet<>();
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.STAINED_CLAY, 5, Material.STAINED_CLAY, 14);

		Location spawn = getTreasureLocation().getChestLocations().get(0);

		ArmorStand stand = spawnArmourStand(spawn);
		stand.setHelmet(HELMET);
	}

	@Override
	public void onTick()
	{
		_totalTheta += DELTA_THETA;

		for (ArmorStand stand : _stands)
		{
			Location location = getTreasureLocation().getChest().clone();
			double x = RADIUS * Math.cos(_totalTheta);
			double z = RADIUS * Math.sin(_totalTheta);

			location.add(x, DELTA_Y, z);

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getTreasureLocation().getChest())));

			stand.teleport(location);
			UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location.clone().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0.01F, 1, ViewDist.NORMAL);

			for (Location chest : getTreasureLocation().getChestLocations())
			{
				if (UtilMath.offset2dSquared(chest, location) < 1 && _chests.add(chest))
				{
					chest = chest.clone();
					chest.setYaw(location.getYaw());
					createChestAt(chest, Material.ENDER_CHEST);
					UtilFirework.playFirework(chest.add(0, 1, 0), FIREWORK_EFFECT);
				}
			}

			if (getTicks() % 5 == 0)
			{
				stand.getWorld().playSound(location, Sound.ORB_PICKUP, 1, 1);
			}
		}

		UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, getTreasureLocation().getChest().clone().add(0, 4, 0), 3, 0.3F, 3, 0, 3, ViewDist.NORMAL);

		if (_totalTheta >= TOTAL_THETA)
		{
			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{
		_stands.forEach(Entity::remove);
	}
}
