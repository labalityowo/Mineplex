package mineplex.game.clans.clans.siege.outpost.build;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

import mineplex.core.common.util.UtilWorld;

public class OutpostBlock 
{
	private Location _location;
	private int _id;
	private byte _data;
	
	private int _originalId;
	private byte _originalData;
	
	public OutpostBlock(Map<String, OutpostBlock> blocks, Location loc, int id, byte data)
	{
		_location = loc;
		_id = id;
		_data = data;
		
		String locStr = UtilWorld.locToStr(loc);
		
		if (blocks.containsKey(locStr))
		{
			_originalId = blocks.get(locStr)._originalId;
			_originalData = blocks.get(locStr)._originalData;
		}
		else
		{
			_originalId = _location.getBlock().getTypeId();
			_originalData = _location.getBlock().getData();
		}
	}
	
	public void set() 
	{
		_location.getBlock().setTypeIdAndData(_id, _data, false);
		if (_id != 0)
		{
			_location.getWorld().playEffect(_location, Effect.STEP_SOUND, Material.getMaterial(_id), 10);
		}
	}
	
	public void restore()
	{
		BlockState state = _location.getBlock().getState();
		state.setTypeId(_originalId);
		state.setRawData(_originalData);
		state.update(true, false);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public byte getData()
	{
		return _data;
	}
	
	public int getOriginalId()
	{
		return _originalId;
	}
	
	public int getOriginalData()
	{
		return _originalData;
	}
	
	public Location getLocation()
	{
		return _location;
	}
}
