package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.games.christmas.parts.Part5;
import net.minecraft.server.v1_8_R3.EntityCreature;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class BossSnowmanPattern 
{
	private boolean _active = false;
	private int _difficulty = 0;

	private Part5 Host;
	private ArrayList<Location> _spawnA;
	private ArrayList<Location> _spawnB;

	private int _aDir = 1;
	private int _bDir = -1;

	private long _lastSpawn = 0;

	private ArrayList<BossSnowman> _ents = new ArrayList<BossSnowman>();

	public BossSnowmanPattern(Part5 host, ArrayList<Location> spawnA, ArrayList<Location> spawnB, Location waypoint)
	{
		Host = host;

		_spawnA = new ArrayList<Location>();
		_spawnB = new ArrayList<Location>();

		//Order A Spawns
		while (!spawnA.isEmpty())
		{
			Location bestLoc = null;
			double bestDist = 0;

			for (Location loc : spawnA)
			{
				double dist = UtilMath.offset(waypoint, loc);

				if (bestLoc == null || bestDist > dist)
				{
					bestLoc = loc;
					bestDist = dist;
				}
			}

			_spawnA.add(bestLoc);
			spawnA.remove(bestLoc);
		}

		//Order B Spawns
		while (!spawnB.isEmpty())
		{
			Location bestLoc = null;
			double bestDist = 0;

			for (Location loc : spawnB)
			{
				double dist = UtilMath.offset(waypoint, loc);

				if (bestLoc == null || bestDist > dist)
				{
					bestLoc = loc;
					bestDist = dist;
				}
			}

			_spawnB.add(bestLoc);
			spawnB.remove(bestLoc);
		}
	}

	public void SetActive(boolean active, int difficulty)
	{
		_active = active;
		_difficulty = difficulty;
	}

	public void Update()
	{
		MoveDieHit();

		if (!_active)
			return;

		//Timer
		if (!UtilTime.elapsed(_lastSpawn, 4000 - (400 * _difficulty)))
			return;
		
		_lastSpawn = System.currentTimeMillis();

		Host.Host.CreatureAllowOverride = true;
		
		//Spawn A
		for (int i=0 ; i<_spawnA.size() ; i++)
		{
			if (i%6 < 3)
				continue;

			Location loc = _spawnA.get(i);
			Snowman ent = loc.getWorld().spawn(loc, Snowman.class);
			UtilEnt.vegetate(ent);
			UtilEnt.ghost(ent, true, false);
			_ents.add(new BossSnowman(ent, loc, _aDir));
		}

		//Spawn B
		for (int i=0 ; i<_spawnB.size() ; i++)
		{
			if (i%6 >= 3)
				continue;

			Location loc = _spawnB.get(i);
			Snowman ent = loc.getWorld().spawn(loc, Snowman.class);
			UtilEnt.vegetate(ent);
			UtilEnt.ghost(ent, true, false);
			_ents.add(new BossSnowman(ent, loc, _bDir));
		}

		Host.Host.CreatureAllowOverride = false;
	}

	private void MoveDieHit() 
	{
		Iterator<BossSnowman> entIterator = _ents.iterator();

		//Move & Die
		while (entIterator.hasNext())
		{
			BossSnowman ent = entIterator.next();
			
			//Fire
			if (ent.Entity.getFireTicks() > 0)
			{
				ent.Entity.remove();
				entIterator.remove();
				continue;
			}	

			//Move
			EntityCreature ec = ((CraftCreature)ent.Entity).getHandle();
			ec.getControllerMove().a(ent.Entity.getLocation().getX()+ent.Direction, ent.Entity.getLocation().getY(), ent.Entity.getLocation().getZ(), 1.25 + 0.25*_difficulty);

			//Die
			if (!ent.Entity.isValid() || UtilMath.offset(ent.Entity.getLocation(), ent.Spawn) > 43)
			{
				ent.Entity.remove();
				entIterator.remove();
			}
		}

		//Hit Players
		for (Player player : Host.Host.GetPlayers(true))
		{
			if (!Recharge.Instance.usable(player, "Snowman Hit"))
				return;

			for (BossSnowman snowman : _ents)
			{
				if (UtilMath.offset2d(player, snowman.Entity) < 1)
				{
					if (Math.abs(player.getLocation().getY() - snowman.Entity.getLocation().getY()) < 2)
					{
						UtilAction.velocity(player, new Vector(snowman.Direction,0,0), 2, false, 0, 0.8, 0.8, true);
						Recharge.Instance.useForce(player, "Snowman Hit", 1000);
						
						//Damage Event
						Host.Host.Manager.GetDamage().NewDamageEvent(player, snowman.Entity, null, 
								DamageCause.ENTITY_ATTACK, 3, false, false, false,
								null, null);
					}
				}
			}
		}
	}
	
	public void Clean()
	{
		for (BossSnowman ent : _ents)
			ent.Entity.remove();
		
		_ents.clear();
	}
}
