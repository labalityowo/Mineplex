package mineplex.game.clans.fields;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemStackFactory;

public class FieldBlockData 
{
	//Block
	private String _server;
	private Location _loc;

	private List<FieldBlockLootData> _loot;

	private int _stockCur;
	private int _stockMax;

	private int _blockId;
	private byte _blockData;

	private int _emptyId;
	private byte _emptyData;

	private long _regenTime;
	private long _breakTime;
	
	private FieldBlock Field;

	//Constructor
	public FieldBlockData(FieldBlock field, String server, Location loc,
			int blockId, byte blockData, int emptyId, byte emptyData, 
			int stockMax, double regenTime, String lootString)
	{
		Field = field;
		
		//Block
		_server = server;
		_loc = loc;

		//Loot
		_loot = new ArrayList<FieldBlockLootData>();
		for (String cur : lootString.split(","))
		{
			String[] loot = cur.split(":");

			if (loot.length != 5)
			{
				System.out.println("Loot Failure: " + cur);
				continue;
			}

			//Parse and Add
			try
			{
				_loot.add(new FieldBlockLootData(Integer.parseInt(loot[0]), 
						Byte.parseByte(loot[1]), 
						Integer.parseInt(loot[2]), 
						Integer.parseInt(loot[3]), 
						Integer.parseInt(loot[4])));	
			}
			catch (Exception e)
			{
				System.out.println("Loot Failure: " + cur);
			}
		}

		_stockCur = 0;
		_stockMax = stockMax;

		_blockId = blockId;
		_blockData = blockData;

		_emptyId = emptyId;
		_emptyData = emptyData;

		_regenTime = (long)(regenTime * 60000);
		_breakTime = 0;

		//Start Ore
		regen();
	}

	public void check()
	{
		//Correct Block
		if (_stockCur >= 1)
			if (_loc.getBlock().getTypeId() != _blockId)
			{
				//89 is used to display Field Blocks
				if (_loc.getBlock().getTypeId() != 89)
					_stockCur--;
				
				_loc.getBlock().setTypeIdAndData(_blockId, _blockData, false);
			}

		if (_stockCur == 0)
			if (_loc.getBlock().getTypeId() != _emptyId)
				_loc.getBlock().setTypeIdAndData(_emptyId, _emptyData, false);
	}

	public void regen()
	{
		//Maxed
		if (_stockCur >= _stockMax)
			return;

		//Cooldown
		if (!UtilTime.elapsed(_breakTime, UtilField.scale(_regenTime)))
			return;

		//Increase Ore 
		if (_stockCur == 0)
			_loc.getBlock().setTypeIdAndData(_blockId, _blockData, false);

		_stockCur++;
		_breakTime = System.currentTimeMillis();
	
		//Effect
		_loc.getWorld().playEffect(_loc, Effect.STEP_SOUND, _blockId);
	}

	public void handleMined(Player player)
	{
		if (!Field.getEnergy().Use(player, "Mine Field Block", 60, true, true))
			return;
		
		//Set Break Time - Avoid Instant Regen
		if (_stockCur == _stockMax)
			_breakTime = System.currentTimeMillis();

		//Loot + Change Block
		if (_stockCur <= 0)
			return;

		if (tryMine())
		{
			//Effect
			_loc.getWorld().playEffect(_loc, Effect.STEP_SOUND, _loc.getBlock().getTypeId());
			
			_stockCur--;
			if (_stockCur == 0)
				_loc.getBlock().setTypeIdAndData(_emptyId, _emptyData, false);
		}
		
		//Slow
		Field.getCondition().Slow("Field Slow", player, player, 6, 1, false, true, false, false);
	}

	public boolean tryMine()
	{
		if (_loot.isEmpty())
			return true;

		boolean dropped = false;

		for (FieldBlockLootData cur : _loot)
		{
			if (cur.drop())
			{
				int amount = cur.base + UtilMath.r(cur.bonus+1);
				_loc.getWorld().dropItemNaturally(_loc, ItemStackFactory.Instance.CreateStack(cur.id, cur.data, amount));
				dropped = true;
			}
		}

		return dropped;
	}

	public void showInfo(Player player)
	{
		UtilPlayer.message(player, F.main("Field", "Block Information;"));
		UtilPlayer.message(player, F.desc("Block Capacity", _stockCur + "/" + _stockMax));

		if (_stockCur >= _stockMax)
			UtilPlayer.message(player, F.desc("Block Regeneration", "Full Capacity"));
		else
		{
			long time = UtilField.scale(_regenTime) - (System.currentTimeMillis() - _breakTime);
			UtilPlayer.message(player, F.desc("Block Regeneration", 
					UtilTime.convertString(time, 1, TimeUnit.FIT)));
		}
		
		for (FieldBlockLootData cur : _loot)
			cur.showInfo(player);
	}

	public String getServer()
	{
		return _server;
	}

	public void clean() 
	{
		_loc.getBlock().setTypeIdAndData(_emptyId, _emptyData, false);	
	}
	
	public Block getBlock()
	{
		return _loc.getBlock();
	}
	
	public Location getLocation()
	{
		return _loc;
	}

	public void setEmpty() 
	{
		getBlock().setTypeIdAndData(_emptyId, _emptyData, true);
	}

	public boolean isEmpty() 
	{
		return _stockCur == 0;
	}
}
