package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.christmas.Christmas;
import net.minecraft.server.v1_8_R3.EntityCreature;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SnowmanWaveB 
{
	private Christmas Host;
	private ArrayList<Location> _spawns;
	private Location _present;

	private int xDir = -1;

	private long _lastSpawn = 0;
	private ArrayList<Integer> _lastPositions = new ArrayList<Integer>();

	private ArrayList<Snowman> _ents = new ArrayList<Snowman>();

	public SnowmanWaveB(Christmas host, ArrayList<Location> spawns, Location waypoint, Location[] presents)
	{
		Host = host;

		_spawns = new ArrayList<Location>();

		//Order Spawns
		while (!spawns.isEmpty())
		{
			Location bestLoc = null;
			double bestDist = 0;

			for (Location loc : spawns)
			{
				double dist = UtilMath.offset(waypoint, loc);

				if (bestLoc == null || bestDist > dist)
				{
					bestLoc = loc;
					bestDist = dist;
				}
			}

			_spawns.add(bestLoc);
			spawns.remove(bestLoc);
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
		//Spawn
		if (!Host.GetSleigh().HasPresent(_present))
		{
			if (Math.random() > 0.25)
			{
				_lastSpawn = System.currentTimeMillis();

				while (_lastPositions.size() > 6)
					_lastPositions.remove(0);

				int i = UtilMath.r(_spawns.size());

				while (_lastPositions.contains(i))
					i = UtilMath.r(_spawns.size());

				_lastPositions.add(i);

				Location loc = _spawns.get(i);
				Host.CreatureAllowOverride = true;
				Snowman ent = loc.getWorld().spawn(loc, Snowman.class);
				Host.CreatureAllowOverride = false;
				UtilEnt.vegetate(ent);
				UtilEnt.ghost(ent, true, false);
				_ents.add(ent);
			}
		}

		Iterator<Snowman> entIterator = _ents.iterator();

		//Move & Die
		while (entIterator.hasNext())
		{
			Snowman ent = entIterator.next();

			EntityCreature ec = ((CraftCreature)ent).getHandle();
			ec.getControllerMove().a(ent.getLocation().getX()+xDir, ent.getLocation().getY(), ent.getLocation().getZ(), 2f);

			double dist = Math.abs(_spawns.get(0).getX() - ent.getLocation().getX());
			
			if (ent.getTicksLived() > 500 || dist > 52)
			{
				ent.getWorld().playEffect(ent.getLocation(), Effect.STEP_SOUND, 80);
				ent.remove();
				entIterator.remove();
			}
		}

		//Hit Players
		for (Player player : Host.GetPlayers(true))
		{
			if (!Recharge.Instance.usable(player, "Snowman Hit"))
				return;

			for (Snowman snowman : _ents)
			{
				if (UtilMath.offset2d(player, snowman) < 1)
				{
					UtilAction.velocity(player, new Vector(xDir,0,0), 4, false, 0, 1.2, 1.2, true);
					Recharge.Instance.useForce(player, "Snowman Hit", 2000);
					
					//Damage Event
					Host.Manager.GetDamage().NewDamageEvent(player, snowman, null, 
							DamageCause.ENTITY_ATTACK, 4, false, false, false,
							null, null);
				}
			}
		}
	}
}
