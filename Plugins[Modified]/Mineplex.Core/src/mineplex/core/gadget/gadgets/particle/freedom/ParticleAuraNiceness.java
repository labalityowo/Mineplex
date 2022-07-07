package mineplex.core.gadget.gadgets.particle.freedom;

import mineplex.core.arcadeevents.CoreGameStartEvent;
import mineplex.core.arcadeevents.CoreGameStopEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.treasure.event.TreasureFinishEvent;
import mineplex.core.treasure.event.TreasureStartEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Places flowers around the player with poofs of red and white
 */
public class ParticleAuraNiceness extends ParticleGadget
{
	/** Radius within which flowers not allowed near treasure chests */
	private static final int TREASURE_RADIUS = 4;

	/** Horizontal offset for particle spawns */
	private static final double H_FIELD = 0.5;

	/** Vertical offset for particle spawns */
	private static final double V_FIELD = 0.35;

	/** How likely a flower is to be spawned (1/n) */
	private static final int ROSE_PROBABILITY = 40;

	/** Radius in which flowers are spawned */
	private static final double ROSE_RADIUS = 2.5;

	/** How many particles accompany each flower spawn */
	private static final int PARTICLE_COUNT = 20;

	/** List of blocks that have flowers in them */
	private final Set<Block> _blocks = new HashSet<>();

	/** Milliseconds for which flowers persist */
	private final long DURATION = 5000;

	/** Locations at which treasure is currently being opened */
	private final Map<UUID, Location> _openingTreasure = new HashMap<>();

	/** Whether flowers can be spawned in addition to particles */
	private boolean _enabled = true;

	public ParticleAuraNiceness(GadgetManager manager)
	{
		super(manager, "Aura of Niceness",
				UtilText.splitLineToArray(C.cGray + "Canadians are always nice online.", LineFormat.LORE), -8, Material.WOOL,
				(byte) 0);

		setDisplayItem(CountryFlag.CANADA.getBanner());
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Location location : _openingTreasure.values())
		{
			if (location.toVector().isInSphere(player.getLocation().toVector(), TREASURE_RADIUS))
			{
				return;
			}
		}

		UtilShapes.getCircle(player.getLocation().subtract(0, 0.5, 0), false, ROSE_RADIUS).stream().map(Location::getBlock)
				.collect(Collectors.toSet()).forEach(block ->
		{
			if (ThreadLocalRandom.current().nextInt(ROSE_PROBABILITY) == 0)
			{
				Block b = block.getRelative(BlockFace.UP);

				if (b.isEmpty() && UtilBlock.fullSolid(block) && !UtilBlock.bottomSlab(block))
				{
					Location loc = b.getLocation().add(H_FIELD, V_FIELD, H_FIELD);

					if (_enabled)
					{
						byte data = ThreadLocalRandom.current().nextInt(2) == 0 ? (byte) 4 : 6;

						_blocks.add(b);
						Manager.getBlockRestore().add(b, Material.RED_ROSE.getId(), data, DURATION);
					}

					for (int i = 0; i < PARTICLE_COUNT; ++i)
					{
						UtilParticle.playColoredParticleToAll(Color.RED, UtilParticle.ParticleType.RED_DUST, UtilMath.gauss(loc, 4, 4, 4), 0, UtilParticle.ViewDist.NORMAL);
						UtilParticle.playColoredParticleToAll(Color.WHITE, UtilParticle.ParticleType.RED_DUST, UtilMath.gauss(loc, 4, 4, 4), 0, UtilParticle.ViewDist.NORMAL);
					}
				}
			}
		});

		for(Iterator<Block> it = _blocks.iterator(); it.hasNext();)
		{
			Block b = it.next();

			if (b.getType() != Material.RED_ROSE)
			{
				it.remove();
				Location loc = b.getLocation().add(H_FIELD, V_FIELD, H_FIELD);
				for (int i = 0; i < PARTICLE_COUNT / 2; ++i)
				{
					UtilParticle.playColoredParticleToAll(Color.RED, UtilParticle.ParticleType.RED_DUST, UtilMath.gauss(loc, 6, 6, 6), 0, UtilParticle.ViewDist.NORMAL);
					UtilParticle.playColoredParticleToAll(Color.WHITE, UtilParticle.ParticleType.RED_DUST, UtilMath.gauss(loc, 6, 6, 6), 0, UtilParticle.ViewDist.NORMAL);
				}
			}
		}
	}

	/**
	 * Stop flowers from popping off of blocks
	 */
	@EventHandler
	public void onBlockFade(BlockPhysicsEvent event)
	{
		if (_blocks.contains(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Disable flowers in the area around treasure being opened.
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void disableOnTreasureStart(TreasureStartEvent event)
	{
		_openingTreasure.put(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
		Manager.getBlockRestore().restoreBlockAround(Material.CARPET, event.getPlayer().getLocation(), TREASURE_RADIUS);
	}

	/**
	 * Enable flowers in the area around treasure no longer being opened.
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void enableOnTreasureFinish(TreasureFinishEvent event)
	{
		if (_openingTreasure.containsKey(event.getPlayer().getUniqueId()))
		{
			_openingTreasure.remove(event.getPlayer().getUniqueId());
		}
	}

	/**
	 * Disable flowers on game start
	 */
	@EventHandler
	public void onGameStart(CoreGameStartEvent event)
	{
		_enabled = false;
	}

	/**
	 * Enable flowers on game end
	 */
	@EventHandler
	public void onGameEnd(CoreGameStopEvent event)
	{
		_enabled = true;
	}
}
