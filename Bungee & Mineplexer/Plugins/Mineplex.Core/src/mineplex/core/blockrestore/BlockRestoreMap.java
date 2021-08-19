package mineplex.core.blockrestore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.core.common.block.BlockData;

public class BlockRestoreMap
{
	private BlockRestore _blockRestore;
	// The rate at which we restore blocks
	private int _blocksPerTick;
	// Easy access to all the blocks we have modified
	private HashSet<Block> _changedBlocks;
	// A hashmap for each level, so we can quickly restore top down
	private HashMap<Block, BlockData>[] _blocks;

	protected BlockRestoreMap(BlockRestore blockRestore)
	{
		this(blockRestore, 50);
	}

	protected BlockRestoreMap(BlockRestore blockRestore, int blocksPerTick)
	{
		_blockRestore = blockRestore;
		_blocksPerTick = blocksPerTick;
		_changedBlocks = new HashSet<Block>();
		_blocks = new HashMap[256];

		// Populate Array
		for (int i = 0; i < 256; i++)
		{
			_blocks[i] = new HashMap<Block, BlockData>();
		}
	}

	public void addBlockData(BlockData blockData)
	{
		Block block = blockData.Block;
		
		if (block.getY() > 0 && block.getY() < _blocks.length)
		{
			if (!_blocks[block.getY()].containsKey(block))
			{
				_blocks[block.getY()].put(block, blockData);
			}
		}

		_changedBlocks.add(blockData.Block);
	}

	public void set(Block block, Material material)
	{
		set(block, material, (byte) 0);
	}

	public void set(Block block, Material material, byte toData)
	{
		set(block, material.getId(), toData);
	}

	public void set(Block block, int toId, byte toData)
	{
		addBlockData(new BlockData(block));

		block.setTypeIdAndData(toId, toData, false);
	}

	public boolean contains(Block block)
	{
		return _changedBlocks.contains(block);
	}

	public HashSet<Block> getChangedBlocks()
	{
		return _changedBlocks;
	}

	/**
	 * Restore all the blocks changed in this BlockRestoreMap
	 * NOTE: You should not use the same BlockRestoreMap instance after you run restore.
	 * You must initialize a new BlockRestoreMap from BlockRestore
	 */
	public void restore()
	{
		// The idea behind this is that the runnable will restore blocks over time
		// If the server happens to shutdown while the runnable is running, we will still
		// restore all our blocks with restoreInstant (as called by BlockRestore)
		BlockDataRunnable runnable = new BlockDataRunnable(_blockRestore.getPlugin(), new RestoreIterator(), _blocksPerTick, new Runnable()
		{
			@Override
			public void run()
			{
				clearMaps();
				_blockRestore.removeMap(BlockRestoreMap.this);
			}
		});
		runnable.start();
	}

	private void clearMaps()
	{
		for (int i = 0; i < 256; i++)
		{
			_blocks[i].clear();
		}

		_changedBlocks.clear();
	}

	public void restoreInstant()
	{
		for (int i = 0; i < 256; i++)
		{
			HashMap<Block, BlockData> map = _blocks[i];
			for (BlockData data : map.values())
			{
				data.restore();
			}
		}

		clearMaps();
	}

	public int getBlocksPerTick()
	{
		return _blocksPerTick;
	}

	public void setBlocksPerTick(int blocksPerTick)
	{
		_blocksPerTick = blocksPerTick;
	}

	private class RestoreIterator implements Iterator<BlockData>
	{
		private Iterator<BlockData> _currentIterator;
		private int _currentIndex;

		public RestoreIterator()
		{
			_currentIndex = 255;
			updateIterator();
		}

		private void updateIterator()
		{
			_currentIterator = _blocks[_currentIndex].values().iterator();
		}

		@Override
		public boolean hasNext()
		{
			while (!_currentIterator.hasNext() && _currentIndex > 0)
			{
				_currentIndex--;
				updateIterator();
			}

			return _currentIterator.hasNext();
		}

		@Override
		public BlockData next()
		{
			while (!_currentIterator.hasNext() && _currentIndex > 0)
			{
				_currentIndex--;
				updateIterator();
			}

			return _currentIterator.next();
		}

		@Override
		public void remove()
		{
			_currentIterator.remove();
		}
	}

}
