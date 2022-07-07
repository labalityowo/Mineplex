package mineplex.core.blockrestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

@ReflectivelyCreateMiniPlugin
public class BlockRestore extends MiniPlugin
{
	private Map<Block, BlockRestoreData> _blocks = new HashMap<>();
	private LinkedList<BlockRestoreMap> _restoreMaps;

	private BlockRestore() 
	{
		super("Block Restore");

		_restoreMaps = new LinkedList<BlockRestoreMap>();
	}

	@EventHandler(priority=EventPriority.LOW)
	public void blockBreak(BlockBreakEvent event)
	{
		if (contains(event.getBlock()))
		{
			BlockRestoreData data = _blocks.get(event.getBlock());
			if (data != null && data.isRestoreOnBreak())
			{
				_blocks.remove(event.getBlock());
				data.restore();
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (contains(event.getBlockPlaced()))
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void piston(BlockPistonExtendEvent event)
	{
		if (event.isCancelled())
			return;

		Block push = event.getBlock();
		for (int i=0 ; i<13 ; i++)
		{
			push = push.getRelative(event.getDirection());

			if (push.getType() == Material.AIR)
				return;

			if (contains(push))
			{
				push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getTypeId());
				event.setCancelled(true);
				return;	
			}		
		}			
	}

	@EventHandler
	public void expireBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		ArrayList<Block> toRemove = new ArrayList<Block>();

		for (BlockRestoreData cur : _blocks.values())
			if (cur.expire()) 
				toRemove.add(cur._block);

		//Remove Handled
		for (Block cur : toRemove)
			_blocks.remove(cur);
	}
	
	@EventHandler
	public void expireUnload(ChunkUnloadEvent event)
	{
		Iterator<Entry<Block, BlockRestoreData>> iterator = _blocks.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<Block, BlockRestoreData> entry = iterator.next();
			if (entry.getKey().getChunk().equals(event.getChunk()))
			{
				entry.getValue().restore();
				iterator.remove();
			}
		}
	}

	public boolean restore(Block block)
	{
		if (!contains(block))
			return false;

		_blocks.remove(block).restore();
		return true;
	}
	
	public void restoreAll()
	{
		for (BlockRestoreData data : _blocks.values())
			data.restore();
		
		_blocks.clear();
	}
	
	public HashSet<Location> restoreBlockAround(Material type, Location location, int radius)
	{
		HashSet<Location> restored = new HashSet<Location>();
		
		Iterator<Block> blockIterator = _blocks.keySet().iterator();
		
		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();
			
			if (block.getType() != type)
				continue;
			
			if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), location) > radius)
				continue;
			
			restored.add(block.getLocation().add(0.5, 0.5, 0.5));
			
			_blocks.get(block).restore();
			
			blockIterator.remove();
		}
		
		return restored;
	}

	public void add(Block block, int toID, byte toData, long expireTime)
	{
		add(block, toID, toData, expireTime, false);
	}

	public void add(Block block, int toID, byte toData, long expireTime, boolean restoreOnBreak)
	{
		add(block, toID, toData, block.getTypeId(), block.getData(), expireTime, restoreOnBreak);
	}

	public void add(Block block, int toID, byte toData, int fromID, byte fromData, long expireTime)
	{
		add(block, toID, toData, fromID, fromData, expireTime, false);
	}
	
	public void add(Block block, int toID, byte toData, int fromID, byte fromData, long expireTime, boolean restoreOnBreak)
	{ 
		if (!contains(block))		getBlocks().put(block, new BlockRestoreData(block, toID, toData, fromID, fromData, expireTime, 0, restoreOnBreak));
		else
		{
			if (getData(block) != null)
			{
				getData(block).update(toID, toData, expireTime);
			}
		}
	}

	public void snow(Block block, byte heightAdd, byte heightMax, long expireTime, long meltDelay, int heightJumps)
	{
		//Fill Above
		if (((block.getTypeId() == 78 && block.getData() >= (byte)7) || block.getTypeId() == 80) && getData(block) != null)
		{
			if (getData(block) != null)
				getData(block).update(78, heightAdd, expireTime, meltDelay);

			if (heightJumps > 0)	snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, heightJumps - 1);
			if (heightJumps == -1)	snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, -1);

			return;
		}

		//Not Grounded
		if (!UtilBlock.solid(block.getRelative(BlockFace.DOWN)) && block.getRelative(BlockFace.DOWN).getTypeId() != 78)
			return;	

		//Not on Solid Snow
		if (block.getRelative(BlockFace.DOWN).getTypeId() == 78 && block.getRelative(BlockFace.DOWN).getData() < (byte)7)
			return;

		//No Snow on Ice
		if (block.getRelative(BlockFace.DOWN).getTypeId() == 79 || block.getRelative(BlockFace.DOWN).getTypeId() == 174)
			return;

		//No Snow on Slabs
		if (block.getRelative(BlockFace.DOWN).getTypeId() == 44 || block.getRelative(BlockFace.DOWN).getTypeId() == 126)
			return;

		//No Snow on Stairs
		if (block.getRelative(BlockFace.DOWN).getType().toString().contains("STAIRS"))
			return;

		//No Snow on Fence or Walls
		if (block.getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("fence") ||
			block.getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("wall"))
			return;

		//Not Buildable
		if (!UtilBlock.airFoliage(block) && block.getTypeId() != 78 && block.getType() != Material.CARPET)
			return;

		//Limit Build Height
		if (block.getTypeId() == 78)
			if (block.getData() >= (byte)(heightMax-1))
				heightAdd = 0;

		//Snow
		if (!contains(block))
			getBlocks().put(block, new BlockRestoreData(block, 78, (byte) Math.max(0, heightAdd - 1), block.getTypeId(), block.getData(), expireTime, meltDelay, false));
		else	
		{
			if (getData(block) != null)
				getData(block).update(78, heightAdd, expireTime, meltDelay);
		}
	}

	public boolean contains(Block block)
	{
		if (getBlocks().containsKey(block))
			return true;

		for (BlockRestoreMap restoreMap : _restoreMaps)
		{
			if (restoreMap.contains(block))
				return true;
		}

		return false;
	}

	public BlockRestoreData getData(Block block)
	{
		if (_blocks.containsKey(block))
			return _blocks.get(block);
		return null;
	}

	public Map<Block, BlockRestoreData> getBlocks()
	{
		return _blocks;
	}

	public BlockRestoreMap createMap()
	{
		BlockRestoreMap map = new BlockRestoreMap(this);
		_restoreMaps.add(map);
		return map;
	}

	protected void removeMap(BlockRestoreMap blockRestore)
	{
		_restoreMaps.remove(blockRestore);
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (_blocks.containsKey(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void disable()
	{
		// Clear all restore maps
		for (BlockRestoreMap restoreMap : _restoreMaps)
		{
			restoreMap.restoreInstant();
		}

		restoreAll();
	}
}