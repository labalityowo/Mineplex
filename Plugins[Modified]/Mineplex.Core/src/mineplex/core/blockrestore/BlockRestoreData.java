package mineplex.core.blockrestore;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class BlockRestoreData 
{
	protected Block _block;

	protected int _fromID;
	protected byte _fromData;

	protected int _toID;
	protected byte _toData;

	protected long _expireDelay;
	protected long _epoch;

	protected long _meltDelay = 0;
	protected long _meltLast = 0;
	
	protected BlockState _fromState;
	
	protected HashMap<Location, Byte> _pad = new HashMap<Location, Byte>();

	protected boolean _restoreOnBreak;

	public BlockRestoreData(Block block, int toID, byte toData, int fromID, byte fromData, long expireDelay, long meltDelay, boolean restoreOnBreak)
	{
		_block = block;
		_fromState = block.getState();

		_fromID = fromID;
		_fromData = fromData;

		_toID = toID;
		_toData = toData;

		_expireDelay = expireDelay;
		_epoch = System.currentTimeMillis();

		_meltDelay = meltDelay;
		_meltLast = System.currentTimeMillis();

		_restoreOnBreak = restoreOnBreak;

		//Set
		set();
	}

	public boolean expire()
	{
		if (System.currentTimeMillis() - _epoch < _expireDelay)
			return false;

		//Try to Melt
		if (melt())
			return false;

		//Restore
		restore();
		return true;
	}

	public boolean melt() 
	{
		if (_block.getTypeId() != 78 && _block.getTypeId() != 80)
			return false;

		if (_block.getRelative(BlockFace.UP).getTypeId() == 78 || _block.getRelative(BlockFace.UP).getTypeId() == 80)
		{
			_meltLast = System.currentTimeMillis();
			return true;
		}

		if (System.currentTimeMillis() - _meltLast < _meltDelay)
			return true;

		//Return to Cover
		if (_block.getTypeId() == 80)
			_block.setTypeIdAndData(78, (byte)7, false);

		byte data = _block.getData();
		if (data <= 0)	return false;

		_block.setData((byte)(_block.getData() - 1));
		_meltLast = System.currentTimeMillis();
		return true;
	}

	public void update(int toIDIn, byte toDataIn)
	{
		_toID = toIDIn;
		_toData = toDataIn;

		//Set
		set();
	}

	public void update(int toID, byte addData, long expireTime)
	{
		//Snow Data
		if (toID == 78)
		{
			if (_toID == 78)	_toData = (byte)Math.min(7, _toData + addData);
			else				_toData = addData;
		}
		else				
			_toData = addData;

		_toID = toID;

		//Set
		set();

		//Reset Time
		_expireDelay = expireTime;
		_epoch = System.currentTimeMillis();
	}

	public void update(int toID, byte addData, long expireTime, long meltDelay)
	{
		//Snow Data
		if (toID == 78)
		{
			if (_toID == 78)	_toData = (byte)Math.min(7, _toData + addData);
			else				_toData = addData;
		}

		_toID = toID;

		//Set
		set();

		//Reset Time
		_expireDelay = expireTime;
		_epoch = System.currentTimeMillis();

		//Melt Delay
		if (_meltDelay < meltDelay)
			_meltDelay = (_meltDelay + meltDelay)/2;
	}

	public void set()
	{
		if (_toID == 78 && _toData == (byte)7)
			_block.setTypeIdAndData(80, (byte)0, true);
		else if (_toID == 8 || _toID == 9 || _toID == 79)
		{
			handleLilypad(false);
			_block.setTypeIdAndData(_toID, _toData, true);
		}
		else
			_block.setTypeIdAndData(_toID, _toData, true);
	}

	public boolean isRestoreOnBreak()
	{
		return _restoreOnBreak;
	}

	public void restore()
	{
		_block.setTypeIdAndData(_fromID, _fromData, true);
		_fromState.update();
		
		handleLilypad(true);
	}

	public void setFromId(int i)
	{
		_fromID = i;
	}
	
	public void setFromData(byte i)
	{
		_fromData = i;
	}
	
	private void handleLilypad(boolean restore)
	{
		if (restore)
		{
			for (Location l : _pad.keySet())
			{
				l.getBlock().setType(Material.WATER_LILY);
				l.getBlock().setData(_pad.get(l));
			}
		}
		else
		{
			if (_block.getRelative(BlockFace.UP, 1).getType() == Material.WATER_LILY)
			{
				_pad.put(_block.getRelative(BlockFace.UP, 1).getLocation(), _block.getRelative(BlockFace.UP, 1).getData());
				_block.getRelative(BlockFace.UP, 1).setType(Material.AIR);
			}
		}
	}
}
