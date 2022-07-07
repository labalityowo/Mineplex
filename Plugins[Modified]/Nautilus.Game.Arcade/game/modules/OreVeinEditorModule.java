package nautilus.game.arcade.game.modules;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.common.timing.TimingManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/*
 * This module will allow you to edit veins of ore live as players break them
 */
public class OreVeinEditorModule extends Module
{
	// Initial range to look for veins
	// For example, if I break a block at 0,0,0 and RANGE is 3
	// Then this module will look for ores in the region of -3,-3,-3 to 3,3,3
	private static final int RANGE = 3;

	private boolean _debug = false;
	private boolean _removeNonAirVeins = false;

	private Predicate<Block> _predicateIsOre = block ->
			(block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE);

	private Consumer<List<Block>> _consumerOreEditor = list ->
	{

	};

	public OreVeinEditorModule debug()
	{
		this._debug = true;
		return this;
	}

	public OreVeinEditorModule removeNonAirVeins()
	{
		this._removeNonAirVeins = true;
		return this;
	}

	public OreVeinEditorModule useFilter(Predicate<Block> filter)
	{
		this._predicateIsOre = filter;
		return this;
	}

	public OreVeinEditorModule useEditor(Consumer<List<Block>> editor)
	{
		this._consumerOreEditor = editor;
		return this;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(BlockBreakEvent event)
	{
		if (_debug)
			TimingManager.start("Block Break");

		// Find Nearby Ores
		List<Block> ores = findOres(event.getBlock(), RANGE);

		// Anti-Xray
		removeNonAirVeins(generateVeins(ores));

		if (_debug)
			TimingManager.stop("Block Break");
	}

	// Searches in a range x range x range cube for ores
	private List<Block> findOres(Block source, int range)
	{
		List<Block> ores = new ArrayList<>();

		for (int x = -range; x <= range; x++)
			for (int z = -range; z <= range; z++)
				for (int y = -range; y <= range; y++)
					findOreFromBlock(ores, source.getRelative(x, y, z));

		if (_debug)
			for (Block debug : ores)
				System.out.println("Found " + debug.getType() + " at " + UtilWorld.locToStrClean(debug.getLocation()));

		return ores;
	}

	// Checks if the current block is ore
	// If so, then search all blocks around it to see if they are ores
	private void findOreFromBlock(List<Block> ores, Block block)
	{
		if (ores.contains(block))
			return;

		if (_predicateIsOre.test(block))
		{
			ores.add(block);

			for (Block neighbour : UtilBlock.getSurrounding(block, true))
			{
				findOreFromBlock(ores, neighbour);
			}
		}
	}

	private List<List<Block>> generateVeins(List<Block> ores)
	{
		List<List<Block>> veins = new ArrayList<>();

		while (!ores.isEmpty())
		{
			Block block = ores.remove(0);

			if (_debug)
				System.out.println("NEW VEIN - " + block.getType());

			// Start New Vein
			List<Block> vein = new ArrayList<>();
			veins.add(vein);

			vein.add(block);

			// Find Vein Ores
			boolean addedToVein = true;
			while (addedToVein)
			{
				addedToVein = false;

				Iterator<Block> oreIterator = ores.iterator();

				while (oreIterator.hasNext())
				{
					Block ore = oreIterator.next();

					boolean inVein = false;

					// Check if in Vein
					// fixme is this a good algorithm?
					for (Block veinOre : vein)
					{
						if (UtilMath.offset(ore.getLocation(), veinOre.getLocation()) <= 2)
						{
							inVein = true;
							break;
						}
					}

					// Add to Vein
					if (inVein)
					{
						vein.add(ore);
						oreIterator.remove();
						addedToVein = true;
					}
				}
			}

			if (_debug)
				for (Block veinOre : vein)
					System.out.println(UtilWorld.locToStrClean(veinOre.getLocation()));
		}

		return veins;
	}

	private void removeNonAirVeins(List<List<Block>> oreVeins)
	{
		// Remove Non-Aired Veins
		for (List<Block> vein : oreVeins)
		{
			boolean visible = false;

			// Check if Air is near Vein
			outer: for (Block ore : vein)
			{
				for (Block visibleCheckBlock : UtilBlock.getSurrounding(ore, true))
				{
					if (visibleCheckBlock.getType() == Material.AIR || UtilBlock.isVisible(visibleCheckBlock))
					{
						visible = true;
						break outer;
					}
				}
			}

			if (visible)
				_consumerOreEditor.accept(vein);

			// Remove Vein
			if (!visible && _removeNonAirVeins)
			{
				if (_debug)
					System.out.println("DELETING VEIN;");

				for (Block ore : vein)
				{
					if (_debug)
						System.out.println(ore.getType() + "  " + UtilWorld.locToStrClean(ore.getLocation()));

					ore.setType(Material.STONE);
				}
			}
			else
			{
				if (_debug)
					System.out.println("VALID VEIN!");
			}
		}
	}
}
