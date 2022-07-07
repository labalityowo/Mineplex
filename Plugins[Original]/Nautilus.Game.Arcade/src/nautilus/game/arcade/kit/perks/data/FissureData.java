package nautilus.game.arcade.kit.perks.data;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.golem.PerkFissure;

public class FissureData 
{
	private PerkFissure Host;

	private Player _player;

	private Vector _vec;
	private Location _loc;
	private Location _startLoc;

	private int _height = 0;
	private int _handled = 0;

	private HashSet<Player> _hit = new HashSet<Player>();

	private ArrayList<Block> _path = new ArrayList<Block>();

	public FissureData(PerkFissure host, Player player, Vector vec, Location loc)
	{
		Host = host;

		vec.setY(0);
		vec.normalize();
		vec.multiply(0.1);

		this._player = player;
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

			//Move up 1, cant go 2 up
			if (isSolid(block.getRelative(BlockFace.UP)))
			{
				_loc.add(0, 1, 0);
				block = _loc.getBlock();

				if (isSolid(block.getRelative(BlockFace.UP)))
				{
					return;
				}
					
			}

			//Move down 1, cant go 2 down
			else if (!isSolid(block))
			{
				_loc.add(0, -1, 0);
				block = _loc.getBlock();

				if (!isSolid(block))
				{
					return;
				}
			}

			if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), _loc) > 0.5)
				continue;

			_path.add(block);

			//Effect
			_loc.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

			//Slow
			for (Player cur : block.getWorld().getPlayers())
				if (!cur.equals(_player))
					if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), cur.getLocation()) < 1.5)
					{
						//Condition
						Host.Manager.GetCondition().Factory().Slow("Fissure", cur, _player, 4, 1, false, true, true, true);
					}
		}
	}

	public boolean Update() 
	{
		if (_handled >= _path.size())
			return true;

		Block block = _path.get(_handled);

		//Cannot raise
		if (block.getTypeId() == 46)
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
		if (block.getTypeId() == 1)		Host.Manager.GetBlockRestore().add(block, 4, block.getData(), 14000);
		if (block.getTypeId() == 2)		Host.Manager.GetBlockRestore().add(block, 3, block.getData(), 14000);
		if (block.getTypeId() == 98)	Host.Manager.GetBlockRestore().add(block, 98, (byte) 2, 14000);

		if (block.getType() == Material.SNOW)
		{
			Host.Manager.GetBlockRestore().add(block, Material.SNOW_BLOCK.getId(), (byte) 0, 10000 - (1000 * _height));
			Host.Manager.GetBlockRestore().add(up, Material.SNOW_BLOCK.getId(), (byte) 0, 10000 - (1000 * _height));
		}
		else
		{
			Host.Manager.GetBlockRestore().add(up, block.getTypeId(), block.getData(), 10000 - (1000 * _height));
		}
		_height++;

		//Effect
		up.getWorld().playEffect(up.getLocation(), Effect.STEP_SOUND, block.getTypeId());

		//Damage
		for (Player cur : up.getWorld().getPlayers())
			if (!cur.equals(_player))
			{
				//Teleport
				if (cur.getLocation().getBlock().equals(block))
				{
					cur.teleport(cur.getLocation().add(0, 1, 0));
					
					
				}
				
				if(Host.isTeamDamage(_player, cur))
				{
					continue;
				}

				//Damage
				if (!_hit.contains(cur))	
					if (UtilMath.offset(up.getLocation().add(0.5, 0.5, 0.5), cur.getLocation()) < 1.5)
					{
						_hit.add(cur);

						int damage = 4 + _handled;

						//Damage Event
						Host.Manager.GetDamage().NewDamageEvent(cur, _player, null, 
								DamageCause.CUSTOM, damage, false, false, false,
								_player.getName(), "Fissure");

						//Inform
						UtilPlayer.message(cur, F.main("Game", F.name(_player.getName()) +" hit you with " + F.skill(Host.GetName()) + "."));
						
						final Player fPlayer = cur;
						final Location fLoc = up.getLocation().add(0.5, 0.5, 0.5);
						
						Host.Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Host.Manager.getPlugin(), new Runnable()
						{
							public void run()
							{
								//Velocity
								UtilAction.velocity(fPlayer, UtilAlg.getTrajectory2d(fLoc, fPlayer.getLocation()), 
										1 + 0.1*_handled, true, 0.6 + 0.05*_handled, 0, 10, true);
							}
						}, 4);			
					}
			}

		//Next Column
		if (_height >= Math.min(2, _handled/3 + 1))
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

	private boolean isSolid(Block block)
	{
		return UtilBlock.solid(block) || block.getType() == Material.SNOW;
	}
}
