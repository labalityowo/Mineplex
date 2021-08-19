package nautilus.game.arcade.game.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

/*
 * This module will implement CutClean-esque features within the game.
 *
 * In particular, the CutCleanModule#associateBlockDrop and CutCleanModule#associateMobDrop methods will
 * allow you to drop different items for each block and mob killed
 */
public class CutCleanModule extends Module
{
	private final EnumMap<Material, List<ItemStack>> _blockDrops = new EnumMap<>(Material.class);
	private final EnumMap<Material, List<ItemStack>> _mobDrops = new EnumMap<>(Material.class);

	/*
	 * Associates a material with a list of drops.
	 *
	 * Every time a block of that material is broken, the drops given will be dropped instead
	 *
	 * fixme does not support data ids
	 */
	public CutCleanModule associateBlockDrop(Material block, ItemStack... drops)
	{
		if (_blockDrops.containsKey(block))
		{
			throw new IllegalStateException(block + " is already registered to " + _blockDrops.get(block));
		}
		_blockDrops.put(block, new ArrayList<>(Arrays.asList(drops)));
		return this;
	}

	/*
	 * Associates a mob drop with a list of different drops
	 *
	 * Every time an item of the given type is dropped by a mob death, the alternative drops will be dropped instead
	 */
	public CutCleanModule associateMobDrop(Material drop, ItemStack... drops)
	{
		if (_mobDrops.containsKey(drop))
		{
			throw new IllegalStateException(drop + " is already registered to " + _mobDrops.get(drop));
		}
		_mobDrops.put(drop, new ArrayList<>(Arrays.asList(drops)));
		return this;
	}

	@EventHandler
	public void on(BlockBreakEvent event)
	{
		List<ItemStack> drops = _blockDrops.get(event.getBlock().getType());
		if (drops == null)
		{
			return;
		}

		event.getBlock().setType(Material.AIR);

		getGame().getArcadeManager().getScheduler().runTaskLater(getGame().getArcadeManager().getPlugin(), () ->
		{
			Location dropLocation = event.getBlock().getLocation().add(0.5, 0.2, 0.5);
			for (ItemStack drop : drops)
			{
				event.getBlock().getWorld().dropItem(dropLocation, drop.clone());
			}
		}, 1L);
	}

	@EventHandler
	public void on(EntityDeathEvent event)
	{
		List<ItemStack> drops = event.getDrops();
		List<ItemStack> newDrops = new ArrayList<>();

		Iterator<ItemStack> itemIterator = drops.iterator();
		while (itemIterator.hasNext())
		{
			ItemStack item = itemIterator.next();

			List<ItemStack> replacements = _mobDrops.get(item.getType());
			if (replacements == null)
			{
				continue;
			}

			itemIterator.remove();

			for (ItemStack replace : replacements)
			{
				newDrops.add(replace.clone());
			}
		}

		drops.addAll(newDrops);
	}
}
