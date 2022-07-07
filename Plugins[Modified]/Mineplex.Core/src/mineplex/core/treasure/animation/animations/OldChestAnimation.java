package mineplex.core.treasure.animation.animations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureOpenAnimation;
import mineplex.core.treasure.types.Treasure;

public class OldChestAnimation extends TreasureOpenAnimation
{

	private static final ItemStack HELMET = new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
			.setPlayerHead("MHF_Chest")
			.build();
	private static final double SHAKE_MAGNITUDE = Math.PI / 30D;

	private int _index;

	public OldChestAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);
	}

	@Override
	protected void onStart()
	{
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
				stand.getWorld().playSound(location, Sound.HORSE_ARMOR, 1, 0.7F);
			}

			moved = true;
			location.add(0, 0.1, 0);
			stand.teleport(location);
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.STONE, 0), stand.getLocation().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0, 3, ViewDist.NORMAL);

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
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.5F, 0.5F, 0.5F, 0, 20, ViewDist.NORMAL);
				createChestAt(location, Material.CHEST);
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
