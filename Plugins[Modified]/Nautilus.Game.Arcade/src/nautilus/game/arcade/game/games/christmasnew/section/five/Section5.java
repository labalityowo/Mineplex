package nautilus.game.arcade.game.games.christmasnew.section.five;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.MapUtil;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;

public class Section5 extends Section
{

	private static final byte[] COLOURS = {14, 1, 4, 5, 3, 9, 11, 10, 6};
	static final int TICKS_TO_DELAY = 900;

	private final List<Location> _bridgeBlocks;
	private final Set<FallingBlock> _fallingBlocks;
	private final int _solidifyAt;

	private int _colourIndex;

	public Section5(ChristmasNew host, Location sleighTarget, Location... presents)
	{
		super(host, sleighTarget);

		_bridgeBlocks = _worldData.GetDataLocs("PURPLE");
		_fallingBlocks = new HashSet<>();
		_solidifyAt = _bridgeBlocks.get(0).getBlockY();

		registerChallenges(
				new SwitchParkour(host, presents[0], this),
				new SnowmenKong(host, presents[1], this)
		);

		setTimeSet(12000);
	}

	@Override
	public void onRegister()
	{
		AtomicInteger lowestZ = new AtomicInteger(Integer.MAX_VALUE);

		for (Location location : _bridgeBlocks)
		{
			if (lowestZ.get() > location.getBlockZ())
			{
				lowestZ.set(location.getBlockZ());
			}
		}

		_host.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{

			@Override
			public void run()
			{
				if (++_colourIndex == COLOURS.length)
				{
					_colourIndex = 0;
				}

				byte colour = COLOURS[_colourIndex];
				int z = lowestZ.getAndIncrement();

				_bridgeBlocks.removeIf(location ->
				{
					if (location.getBlockZ() == z)
					{
						FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location.add(0, 8, 0), Material.WOOL, colour);
						fallingBlock.setDropItem(false);
						fallingBlock.setHurtEntities(false);

						_fallingBlocks.add(fallingBlock);
						return true;
					}

					return false;
				});

				if (_bridgeBlocks.isEmpty())
				{
					cancel();
				}
			}
		}, 0, 8);
	}

	@Override
	public void onUnregister()
	{

	}

	@Override
	public void onSantaTarget()
	{
		_host.sendSantaMessage("Look, two more presents.", ChristmasNewAudio.SANTA_TWO_MORE_PRESENTS);
	}

	@EventHandler
	public void updateFallingBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_fallingBlocks.removeIf(block ->
		{
			Location location = block.getLocation();

			if (location.getBlockY() <= _solidifyAt)
			{
				block.remove();
				location.setY(_solidifyAt);
				MapUtil.QuickChangeBlockAt(location, block.getMaterial(), block.getBlockData());

				if (Math.random() > 0.8)
				{
					location.getWorld().playEffect(location, Effect.STEP_SOUND, block.getMaterial(), block.getBlockData());
				}

				return true;
			}

			return false;
		});
	}
}
