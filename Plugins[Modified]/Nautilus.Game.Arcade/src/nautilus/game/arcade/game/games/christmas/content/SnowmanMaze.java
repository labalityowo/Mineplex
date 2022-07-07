package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.content.SnowmanWaypoint.CardinalDirection;

public class SnowmanMaze 
{
	private Christmas Host;
	private ArrayList<Location> _spawns;
	private HashSet<Block> _waypoints;
	private ArrayList<Location> _borders;
	private Location _present;

	private HashMap<Snowman, SnowmanWaypoint> _ents = new HashMap<Snowman, SnowmanWaypoint>();

	public SnowmanMaze(Christmas host, ArrayList<Location> spawns, ArrayList<Location> borders, Location[] presents)
	{
		Host = host;

		_spawns = spawns;
		_borders = borders;
		
		_waypoints = new HashSet<Block>();
		
		for (Location loc : _spawns)
		{
			_waypoints.add(loc.getBlock());
			
			loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.QUARTZ_BLOCK);
		}
			
		
		//Set Present
		if (UtilMath.offset(presents[0], _spawns.get(0)) < UtilMath.offset(presents[1], _spawns.get(0)))
		{
			_present = presents[0].getBlock().getLocation();
		}
		else
		{
			_present = presents[1].getBlock().getLocation();
		}
	}

	public void Update()
	{
		bump();
		
		if (!Host.GetSleigh().HasPresent(_present))
		{
			spawn();
			move();
		}
		else
		{
			for (Entity ent : _ents.keySet())
			{
				ent.getWorld().playEffect(ent.getLocation(), Effect.STEP_SOUND, 80);
				ent.remove();
			}
			
			_ents.clear();
		}
	}

	private void bump()
	{
		//Hit Players
		for (Player player : Host.GetPlayers(true))
		{
			if (!Recharge.Instance.usable(player, "Snowman Hit"))
				continue;

			//Hit Snowman
			for (Snowman snowman : _ents.keySet())
			{
				if (UtilMath.offset2d(player, snowman) < 1)
				{
					Recharge.Instance.useForce(player, "Snowman Hit", 1000);
					
					//Velocity
					UtilAction.velocity(player, new Vector(1,0,0), 4, false, 0, 1.2, 1.2, true);

					//Damage Event
					Host.Manager.GetDamage().NewDamageEvent(player, snowman, null, 
							DamageCause.ENTITY_ATTACK, 10, false, false, false,
							null, null);
				}
			}
			
			//Out of Bounds
			if (player.getLocation().getY() != _spawns.get(0).getBlockY())
				if (UtilAlg.inBoundingBox(player.getLocation(), _borders.get(0), _borders.get(1)))
				{
					Recharge.Instance.useForce(player, "Snowman Hit", 1000);
					
					//Velocity
					UtilAction.velocity(player, new Vector(1,0,0), 4, false, 0, 1.2, 1.2, true);

					//Damage Event
					Host.Manager.GetDamage().NewDamageEvent(player, null, null, 
							DamageCause.ENTITY_ATTACK, 10, false, false, false,
							null, null);
					
					UtilPlayer.message(player, F.main("Game", "You cannot jump on this challenge!"));
				}
		}
	}

	private void move()
	{
		Iterator<Entry<Snowman, SnowmanWaypoint>> entIterator = _ents.entrySet().iterator();

		//Move & Die
		while (entIterator.hasNext())
		{
			Entry<Snowman, SnowmanWaypoint> data = entIterator.next();
			
			//New or Fallen
			if (data.getValue().Target == null || data.getKey().getLocation().getY() < data.getValue().Target.getBlockY())
			{
				Location loc = UtilAlg.findClosest(data.getKey().getLocation(), _spawns);
				
				data.getKey().teleport(loc);
				data.getValue().Target = loc;
			}

			//New Waypoint
			if (UtilMath.offset2d(data.getKey().getLocation(), data.getValue().Target) < 0.4)
			{
				ArrayList<Block> nextBlock = new ArrayList<Block>();
				
				Block north = getTarget(data.getKey().getLocation().getBlock(), null, BlockFace.NORTH);
				Block south = getTarget(data.getKey().getLocation().getBlock(), null, BlockFace.SOUTH);
				Block east = getTarget(data.getKey().getLocation().getBlock(), null, BlockFace.EAST);
				Block west = getTarget(data.getKey().getLocation().getBlock(), null, BlockFace.WEST);
				
				if (north != null)		nextBlock.add(north);
				if (south != null)		nextBlock.add(south);
				if (east != null)		nextBlock.add(east);
				if (west != null)		nextBlock.add(west);
				
				if(nextBlock.isEmpty())
				{
					entIterator.remove();
					data.getKey().remove();
					continue;
				}
				
				if(nextBlock.size() > 1 && data.getValue().Direction != CardinalDirection.NULL) // they can do a uturn if they're stuck
				{
					if(data.getValue().Direction == CardinalDirection.NORTH)
					{
						nextBlock.remove(south);
					}
					else if(data.getValue().Direction == CardinalDirection.SOUTH)
					{
						nextBlock.remove(north);
					}
					else if(data.getValue().Direction == CardinalDirection.WEST)
					{
						nextBlock.remove(east);
					}
					else if(data.getValue().Direction == CardinalDirection.EAST)
					{
						nextBlock.remove(west);
					}
				}
				
				if (nextBlock.isEmpty())
				{
					entIterator.remove();
					data.getKey().remove();
					continue;
				}
				
				//Random Direction
				Location nextLoc = UtilAlg.Random(nextBlock).getLocation();
				data.getValue().Target = nextLoc.clone().add(0.5, 0, 0.5);
				if(north != null && nextLoc.equals(north.getLocation()))
				{
					data.getValue().Direction = CardinalDirection.NORTH;
				}
				else if(south != null && nextLoc.equals(south.getLocation()))
				{
					data.getValue().Direction = CardinalDirection.SOUTH;
				}
				else if(east != null && nextLoc.equals(east.getLocation()))
				{
					data.getValue().Direction = CardinalDirection.EAST;
				}
				else if(west != null && nextLoc.equals(west.getLocation()))
				{
					data.getValue().Direction = CardinalDirection.WEST;
				}
			}

			UtilEnt.CreatureMoveFast(data.getKey(), data.getValue().Target, 1.4f);
		}
	}

	private Block getTarget(Block start, Block cur, BlockFace face)
	{	
		if (cur == null)
			cur = start;

		while (_waypoints.contains(cur.getRelative(face)))
		{
			cur = cur.getRelative(face);

			//Stop at intersection
			int count = 0;

			if (face != BlockFace.NORTH && _waypoints.contains(cur.getRelative(BlockFace.NORTH))) count++;
			if (face != BlockFace.SOUTH && _waypoints.contains(cur.getRelative(BlockFace.SOUTH))) count++;
			if (face != BlockFace.EAST && _waypoints.contains(cur.getRelative(BlockFace.EAST))) count++;
			if (face != BlockFace.WEST && _waypoints.contains(cur.getRelative(BlockFace.WEST))) count++;

			if (count > 1)
				break;
		}

		if (cur.equals(start))
			return null;

		return cur;
	}

	private void spawn()
	{
		//Spawn
		if (!Host.GetSleigh().HasPresent(_present))
		{
			while (_ents.size() < 44)
			{
				Location loc = UtilAlg.Random(_spawns);
				
				Host.CreatureAllowOverride = true;
				Snowman ent = loc.getWorld().spawn(loc, Snowman.class);
				Host.CreatureAllowOverride = false;
				
				UtilEnt.vegetate(ent);
				UtilEnt.ghost(ent, true, false);
				_ents.put(ent, new SnowmanWaypoint(ent.getLocation()));
			}
		}
	}
}
