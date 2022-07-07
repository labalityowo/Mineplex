package mineplex.core.treasure.animation.animations;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBlaze;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class AncientChestAnimation extends TreasureOpenAnimation
{

	public AncientChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.NETHER_BRICK, 0);

		for (Location location : getTreasureLocation().getChestLocations())
		{
			location = location.clone().add(0, UtilMath.rRange(4, 6) + Math.random(), 0);
			ArmorStand stand = spawnArmourStand(location);
			disguise(new DisguiseBlaze(stand));
		}
	}

	@Override
	public void onTick()
	{
		double y = getTreasureLocation().getChest().getY() + 0.5;

		_stands.removeIf(stand ->
		{
			Location location = stand.getLocation();

			if (Math.random() < 0.05)
			{
				stand.getWorld().playSound(location, Sound.ZOMBIE_REMEDY, 1, 0.6F);
			}

			stand.teleport(location.subtract(0, 0.1, 0));

			if (stand.isValid() && location.getY() < y)
			{
				stand.getWorld().playEffect(location, Effect.ENDER_SIGNAL, 0);
				stand.getWorld().playSound(location, Sound.EXPLODE, 1, 0.4F);
				UtilParticle.PlayParticleToAll(ParticleType.LAVA, location, 0.5F, 0.5F, 0.5F, 0.1F, 10, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, location, 0.2F, 0.2F, 0.2F, 0.1F, 10, ViewDist.NORMAL);
				stand.remove();
				createChestAt(location, Material.TRAPPED_CHEST);
				return true;
			}

			return false;
		});

		if (_stands.isEmpty())
		{
			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
