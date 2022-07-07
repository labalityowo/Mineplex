package mineplex.core.common.block.schematic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.block.BlockData;
import mineplex.core.common.util.Callback;

public class SchematicRunnable implements Runnable
{
	private JavaPlugin _plugin;
	private boolean _started;
	private BukkitTask _task;
	private List<BlockData> _changedBlocks;
	private Callback<List<BlockData>> _callback;

	private Schematic _schematic;
	private Block _startBlock;
	private int _currentX = 0;
	private int _currentY = 0;
	private int _currentZ = 0;
	private int _blocksPerTick = 1000;

	public SchematicRunnable(JavaPlugin plugin, Schematic schematic, Block startBlock, Callback<List<BlockData>> callback)
	{
		_plugin = plugin;
		_changedBlocks = new ArrayList<BlockData>(schematic.getSize());
		_started = false;
		_callback = callback;

		_schematic = schematic;
		_startBlock = startBlock;
	}

	public void start()
	{
		if (!_started)
		{
			_task = Bukkit.getScheduler().runTaskTimer(_plugin, this, 1, 1);
			_started = true;
		}
	}

	public void pause()
	{
		if (_started)
		{
			_task.cancel();
			_started = false;
		}
	}

	public void setBlocksPerTick(int blocksPerTick)
	{
		_blocksPerTick = blocksPerTick;
	}

	@Override
	public void run()
	{
		for (int i = 0; i < _blocksPerTick; i++)
		{
			setBlock(_startBlock.getRelative(_currentX, _currentY, _currentZ), _currentX, _currentY, _currentZ);

			_currentX++;

			if (_currentX >= _schematic.getWidth())
			{
				_currentX = 0;
				_currentZ++;

				if (_currentZ >= _schematic.getLength())
				{
					_currentZ = 0;
					_currentY++;

					if (_currentY >= _schematic.getHeight())
					{
						// We are done
						System.out.println("Finished importing schematic with setblockrunnable!");

						if (_callback != null)
							_callback.run(_changedBlocks);

						_task.cancel();
						return;
					}
				}
			}
		}
	}

	private void setBlock(Block block, int x, int y, int z)
	{
		Short materialId = _schematic.getBlock(x, y, z);
		Byte data = _schematic.getData(x, y, z);
		
		if (materialId == null || data == null)
		{
			return;
		}
		Material material = Material.getMaterial(materialId);
		if (material == null)
		{
			System.out.println("Failed to find material with id: " + materialId);
			return;
		}

		if (block.getTypeId() == materialId && block.getData() == data)
			return;

		BlockData blockData = new BlockData(block);
		_changedBlocks.add(blockData);
		block.setTypeIdAndData(materialId, data, false);
	}
}
