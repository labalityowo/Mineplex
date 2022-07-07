package mineplex.core.blockrestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import mineplex.core.common.block.BlockData;

public class BlockDataRunnable implements Runnable
{
	private JavaPlugin _plugin;
	private boolean _started;
	private BukkitTask _task;
	private List<BlockData> _changedBlocks;
	private Runnable _onComplete;
	private int _blocksPerTick;
	private Iterator<BlockData> _blockIterator;

	public BlockDataRunnable(JavaPlugin plugin, Iterator<BlockData> blockIterator, int blocksPerTick, Runnable onComplete)
	{
		_plugin = plugin;
		_changedBlocks = new ArrayList<BlockData>();
		_started = false;
		_blocksPerTick = blocksPerTick;
		_onComplete = onComplete;
		_blockIterator = blockIterator;
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
			if (_blockIterator.hasNext())
			{
				BlockData data = _blockIterator.next();
				data.restore();
			}
			else
			{
				// We are done
				_task.cancel();
				_onComplete.run();
				return;
			}
		}
	}
}
