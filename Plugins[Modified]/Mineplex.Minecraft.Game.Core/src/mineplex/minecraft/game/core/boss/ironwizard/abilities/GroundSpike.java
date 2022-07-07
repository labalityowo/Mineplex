package mineplex.minecraft.game.core.boss.ironwizard.abilities;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

public class GroundSpike
{
	private Block _initial;
	private Material _type;
	private int _max, _height;
	private long _lastTick, _remain;
	private LinkedList<Block> _blocks;
	private boolean _shrinking, _finished;
	
	public GroundSpike(Block first, Material type, int maxHeight, long remainDuration)
	{
		_initial = first;
		_type = type;
		_height = 0;
		_max = maxHeight;
		_remain = remainDuration;
		_lastTick = System.currentTimeMillis();
		_blocks = new LinkedList<>();
		_shrinking = false;
		_finished = false;
	}
	
	@SuppressWarnings("deprecation")
	private void raise()
	{
		if ((_height + 1) < _max)
		{
			_lastTick = System.currentTimeMillis();
			Block b = _initial.getRelative(0, _height, 0);
			for (Player player : UtilServer.getPlayers())
			{
				player.sendBlockChange(b.getLocation(), _type, (byte)0);
			}
			_blocks.add(b);
			_height++;
		}
		else
		{
			if (UtilTime.elapsed(_lastTick, _remain))
			{
				_shrinking = true;
				lower();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void lower()
	{
		_height = Math.min(_blocks.size() - 1, _height);
		if ((_height - 1) >= 0)
		{
			_lastTick = System.currentTimeMillis();
			for (Player player : UtilServer.getPlayers())
			{
				player.sendBlockChange(_blocks.get(_height).getLocation(), Material.AIR, (byte)0);
			}
			_blocks.remove(_height);
			_height--;
		}
		else
		{
			finish();
		}
	}
	
	public boolean isFinished()
	{
		if (!_blocks.isEmpty())
			return false;
		
		return _finished;
	}
	
	@SuppressWarnings("deprecation")
	public void finish()
	{
		_finished = true;
		for (Block block : _blocks)
		{
			for (Player player : UtilServer.getPlayers())
			{
				player.sendBlockChange(block.getLocation(), Material.AIR, (byte)0);
			}
		}
		_blocks.clear();
	}
	
	public void tick()
	{
		if (isFinished())
			return;
		if (!UtilTime.elapsed(_lastTick, 500))
			return;
		
		if (_shrinking)
		{
			lower();
		}
		else
		{
			raise();
		}
	}
}