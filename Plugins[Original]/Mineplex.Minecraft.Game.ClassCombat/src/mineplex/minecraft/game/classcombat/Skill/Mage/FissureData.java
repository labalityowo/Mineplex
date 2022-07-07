package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.ArrayList;
import java.util.HashSet;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;

import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.classcombat.Skill.Mage.events.FissureModifyBlockEvent;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class FissureData 
{
	private Fissure Host;

	private Player _player;
	private int _level;

	private Vector _vec;
	private Location _loc;
	private Location _startLoc;

	private int _height = 0;
	private int _handled = 0;

	private HashSet<Player> _hit = new HashSet<Player>();

	private ArrayList<Block> _path = new ArrayList<Block>();

	public FissureData(Fissure host, Player player, int level, Vector vec, Location loc)
	{
		Host = host;

		vec.setY(0);
		vec.normalize();
		vec.multiply(0.1);

		this._player = player;
		this._level = level;
		this._vec = vec;
		this._loc = loc;
		this._startLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());

		MakePath();
	}

	private void MakePath() 
	{	
		while (UtilMath.offset2d(_loc, _startLoc) < 14) 
		{
			_loc.add(_vec);

			Block block = _loc.getBlock();

			if (block.equals(_startLoc.getBlock()))
				continue;

			if (_path.contains(block))
				continue;

			//Up
			if (UtilBlock.solid(block.getRelative(BlockFace.UP)))
			{
				_loc.add(0, 1, 0);
				block = _loc.getBlock();

				if (UtilBlock.solid(block.getRelative(BlockFace.UP)))
					return;
			}

			//Down
			else if (!UtilBlock.solid(block))
			{
				_loc.add(0, -1, 0);
				block = _loc.getBlock();

				if (!UtilBlock.solid(block))
					return;
			}

			if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), _loc) > 0.5)
				continue;

			_path.add(block);

			//Effect
			_loc.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

			//Slow
			for (Player cur : block.getWorld().getPlayers())
			{
				if (UtilPlayer.isSpectator(cur)) continue;
				
				if (!cur.equals(_player))
				{
					if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), cur.getLocation()) < 1.5)
					{
						//Condition
						Host.Factory.Condition().Factory().Slow("Fissure", cur, _player, 1 + _level, 1, false, true, true, true);
					}
				}
			}
		}
	}

	public boolean Update() 
	{
		if (_handled >= _path.size())
			return true;

		Block block = _path.get(_handled);

		//Cannot raise TNT/WATER
		if (block.getType() == Material.TNT || block.isLiquid())
		{
			return false;
		}
		FissureModifyBlockEvent event = UtilServer.CallEvent(new FissureModifyBlockEvent(block));
		if (event.isCancelled())
		{
			return false;
		}
		if (block.getType().toString().contains("BANNER"))
			return false;
		if (block.getRelative(BlockFace.UP).getType().toString().contains("BANNER"))
			return false;
		if (block.getType() == Material.ANVIL)
			return false;
		if (block.getRelative(BlockFace.UP).getType() == Material.ANVIL)
			return false;

		Block up = block.getRelative(0, _height + 1, 0);

		//Done Column
		if (!UtilBlock.airFoliage(up))
		{
			_loc.getWorld().playEffect(up.getLocation(), Effect.STEP_SOUND, up.getTypeId());
			_height = 0;
			_handled++;
			return false;
		}

		//Boost Column
		if (block.getTypeId() == 1)		Host.Factory.BlockRestore().add(block, 4, block.getData(), 14000);
		if (block.getTypeId() == 2)		Host.Factory.BlockRestore().add(block, 3, block.getData(), 14000);
		if (block.getTypeId() == 98)	Host.Factory.BlockRestore().add(block, 98, (byte) 0, 14000);
		Host.Factory.BlockRestore().add(up, block.getTypeId(), block.getData(), 10000 - (1000 * _height));
		_height++;

		//Effect
		up.getWorld().playEffect(up.getLocation(), Effect.STEP_SOUND, block.getTypeId());

		//Damage
		for (Player cur : up.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;
			
			if (!cur.equals(_player))
			{
				//Teleport
				if (cur.getLocation().getBlock().equals(block))
				{
					cur.teleport(cur.getLocation().add(0, 1, 0));
				}

				//Damage
				if (!_hit.contains(cur))
				{
					if (UtilMath.offset(up.getLocation().add(0.5, 0.5, 0.5), cur.getLocation()) < 1.8)
					{
						_hit.add(cur);

						double damage = 2 + (0.4 * _level) + (_handled * (0.7 + 0.1 * _level));

						//Damage Event
						Host.Factory.Damage().NewDamageEvent(cur, _player, null, 
								DamageCause.CUSTOM, damage, true, false, false,
								_player.getName(), "Fissure");

						//Inform
						UtilPlayer.message(cur, F.main(Host.GetClassType().name(), F.name(_player.getName()) +" hit you with " + F.skill(Host.GetName(_level)) + "."));
					}
				}
			}
		}
		//Next Column
		if (_height >= Math.min(3, _handled/2 + 1))
		{
			_height = 0;
			_handled++;
		}

		return (_handled >= _path.size());
	}

	public void Clear() 
	{
		_hit.clear();
		_path.clear();
		Host = null;
		_player = null;
		_loc = null;
		_startLoc = null;
	}
}