package mineplex.core.treasure.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.util.TreasureUtil;

public abstract class TreasureOpenAnimation extends TreasureAnimation
{

	private final List<Block> _blocksToRestore;

	public TreasureOpenAnimation(Treasure treasure, TreasureLocation treasureLocation)
	{
		super(treasure, treasureLocation);

		_blocksToRestore = new ArrayList<>();
	}

	protected void createChestAt(Location location, Material material)
	{
		MapUtil.QuickChangeBlockAt(location, material, TreasureUtil.getChestFacing(location.getYaw()));
	}

	protected int changeFloor(Material materialA, int dataA)
	{
		return changeFloor(materialA, dataA, materialA, dataA);
	}

	protected int changeFloor(Material materialA, int dataA, Material materialB, int dataB)
	{
		BlockRestore blockRestore = getTreasureLocation().getManager().getBlockRestore();
		AtomicInteger size = new AtomicInteger();
		AtomicBoolean tick = new AtomicBoolean();
		Location center = getTreasureLocation().getChest().clone().subtract(0, 1, 0);

		UtilServer.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				int aSize = size.get();

				if (aSize > 3)
				{
					cancel();
					return;
				}

				int id = tick.get() ? materialA.getId() : materialB.getId();
				byte data = (byte) (tick.get() ? dataA : dataB);

				for (Block block : UtilBlock.getInBoundingBox(center.clone().add(aSize, 0, aSize), center.clone().subtract(aSize, 0, aSize)))
				{
					if (blockRestore.contains(block))
					{
						continue;
					}

					_blocksToRestore.add(block);
					blockRestore.add(block, id, data, Long.MAX_VALUE);
				}

				size.getAndIncrement();
				tick.set(!tick.get());

			}
		}, 0, 10);

		return getTicks() + 40;
	}

	@Override
	public void cleanup()
	{
		super.cleanup();

		BlockRestore blockRestore = getTreasureLocation().getManager().getBlockRestore();
		_blocksToRestore.forEach(blockRestore::restore);
	}
}
