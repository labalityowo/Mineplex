package mineplex.core.treasure.animation.animations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class OmegaChestAnimation extends TreasureOpenAnimation
{

	private static final ItemStack HELMET = SkinData.OMEGA_CHEST.getSkull();
	private static final double SHAKE_MAGNITUDE = Math.PI / 35D;

	private int _index;

	public OmegaChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
		changeFloor(Material.WOOL, 15, Material.WOOL, 7);

		for (Location location : getTreasureLocation().getChestLocations())
		{
			ArmorStand stand = spawnArmourStand(location.clone().subtract(0, 2.5, 0));

			stand.setHelmet(HELMET);
		}
	}

	@Override
	public void onTick()
	{
		if (getTicks() % 10 == 0 && _index != _stands.size())
		{
			_index++;
		}

		int chestY = getTreasureLocation().getChest().getBlockY();
		boolean moved = false;

		for (int i = 0; i < _index; i++)
		{
			ArmorStand stand = _stands.get(i);
			Location location = stand.getLocation();

			if (location.getY() > chestY - 1)
			{
				continue;
			}
			else if (Math.random() < 0.1)
			{
				stand.getWorld().playSound(location, Sound.ZOMBIE_UNFECT, 1, 0.7F);
			}

			moved = true;
			location.add(0, 0.05, 0);
			stand.teleport(location);
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.OBSIDIAN, 0), stand.getLocation().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);

			if (location.getY() > chestY - 1)
			{
				resetArmourStand(stand);
			}
			else
			{
				shakeArmourStand(stand, SHAKE_MAGNITUDE);
			}
		}

		if (!moved)
		{
			for (ArmorStand stand : _stands)
			{
				Location location = stand.getLocation().add(0, 1, 0);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, location, 0.5F, 0.5F, 0.5F, 0.5F, 20, ViewDist.NORMAL);
				createChestAt(location, Material.ENDER_CHEST);
				stand.remove();
			}

			setRunning(false);
		}
	}

	@Override
	protected void onFinish()
	{

	}
}
