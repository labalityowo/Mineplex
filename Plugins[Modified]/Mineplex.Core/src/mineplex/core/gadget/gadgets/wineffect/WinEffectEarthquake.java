package mineplex.core.gadget.gadgets.wineffect;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.gadget.util.CostConstants;

public class WinEffectEarthquake extends WinEffectGadget
{

	private static final int DROP_HEIGHT = 25;
	private static final int RADIUS = 8;
	private static final int PERIOD = 5;
	private static final double DELTA_SCALE = 0.1;

	public WinEffectEarthquake(GadgetManager manager)
	{
		super(manager, "Earthquake", UtilText.splitLineToArray(C.cGray + "Come crashing down on the losers and send them flying away!", LineFormat.LORE), CostConstants.FOUND_IN_TREASURE_CHESTS, Material.STONE, (byte) 3);

		_schematicName = "TornadoPodium";
		_length = TimeUnit.SECONDS.toMillis(7);
	}

	@Override
	public void play()
	{
		Entity entity = getNPC(_player, getBaseLocation().add(0, DROP_HEIGHT, 0), true).getEntity().getBukkitEntity();
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), _other.size(), 4);

		int i = 0;
		for (Player player : _other)
		{
			Location location = circle.get(i++);
			location.add(UtilAlg.getTrajectory(getBaseLocation(), location).multiply(0.4 + Math.random() / 2));

			getNPC(player, location, true).getEntity().getBukkitEntity();
		}

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (entity.isValid() && !UtilEnt.isGrounded(entity))
				{
					return;
				}

				entity.getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 3, 1);
				Manager.runSyncTimer(new BukkitRunnable()
				{
					Map<Block, Double> blocks = UtilBlock.getInRadius(getBaseLocation(), RADIUS);
					Map<LivingEntity, Double> entities = UtilEnt.getInRadius(getBaseLocation(), RADIUS);
					double scale = 0.9;

					@Override
					public void run()
					{
						blocks.entrySet().removeIf(entry ->
						{
							Block block = entry.getKey();

							if (block.getType() == Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR || entry.getValue() > 0.9)
							{
								return true;
							}

							if (scale < entry.getValue())
							{
								if (Math.random() < 0.7)
								{
									Location location = block.getLocation().add(0.5, 1, 0.5);
									double random = 0.5 + Math.random() / 2;
									FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(location, block.getType(), block.getData());
									fallingBlock.setDropItem(false);
									fallingBlock.setHurtEntities(false);
									fallingBlock.setVelocity(UtilAlg.getTrajectory(entity, fallingBlock).multiply(0.3).setY(random));
									location.getWorld().playSound(location, Sound.DIG_GRASS, 2, (float) random);
								}
								
								return true;
							}

							return false;
						});

						entities.entrySet().removeIf(entry ->
						{
							LivingEntity nearby = entry.getKey();

							if (nearby.equals(entity))
							{
								return true;
							}

							if (scale < entry.getValue())
							{
								nearby.playEffect(EntityEffect.HURT);
								UtilAction.velocity(nearby, UtilAlg.getTrajectory(getBaseLocation(), nearby.getLocation()), 2, true, 1, 0, 1, true);
								return true;
							}

							return false;
						});

						scale -= DELTA_SCALE;

						if (blocks.isEmpty())
						{
							cancel();
						}
					}
				}, 0, PERIOD);

				cancel();
			}
		}, 10, 1);
	}

	@Override
	public void finish()
	{

	}
}
