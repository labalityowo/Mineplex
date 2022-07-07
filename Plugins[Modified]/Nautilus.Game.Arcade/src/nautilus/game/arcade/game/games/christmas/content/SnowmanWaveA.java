package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
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

public class SnowmanWaveA 
{
	private Christmas Host;
	private ArrayList<Location> _spawns;
	private Location _present;

	private int xDir = 1;

	private long lastSpawn = 0;
	private int lastGap = 0;

	private ArrayList<Snowman> _ents = new ArrayList<Snowman>();

	public SnowmanWaveA(Christmas host, ArrayList<Location> spawns, Location waypoint, Location[] presents)
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
			if (UtilTime.elapsed(lastSpawn, 2000))
			{
				lastSpawn = System.currentTimeMillis();

				int gap = 1 + UtilMath.r(_spawns.size() - 1);

				//Ensure Gap is different to last
				while (Math.abs(lastGap - gap) < 5 || Math.abs(lastGap - gap) > 13)
					gap = 1 + UtilMath.r(_spawns.size() - 1);

				lastGap = gap;

				for (int i=0 ; i<_spawns.size() ; i++)
				{
					if (Math.abs(gap - i) <= 2)
						continue;

					Location loc = _spawns.get(i);
					Host.CreatureAllowOverride = true;
					Snowman ent = loc.getWorld().spawn(loc, Snowman.class);
					Host.CreatureAllowOverride = false;
					UtilEnt.vegetate(ent);
					UtilEnt.ghost(ent, true, false);
					_ents.add(ent);
				}
			}
		}

		Iterator<Snowman> entIterator = _ents.iterator();

		//Move & Die
		while (entIterator.hasNext())
		{
			Snowman ent = entIterator.next();

			EntityCreature ec = ((CraftCreature)ent).getHandle();
			ec.getControllerMove().a(ent.getLocation().getX()+xDir, ent.getLocation().getY(), ent.getLocation().getZ(), 1.8f);

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
