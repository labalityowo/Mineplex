package nautilus.game.arcade.game.games.halloween.creatures;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

public abstract class CreatureBase<T extends LivingEntity>
{ 
	public Halloween Host;  

	private String _name;
	private T _ent; 

	private Location _target;
	private long _targetTime;
	
	private List<Entity> _parts = new ArrayList<>();

	public CreatureBase(Halloween game, String name, Class<T> mobClass, Location loc)
	{
		Host = game;
		_name = name; 

		game.CreatureAllowOverride = true;
		_ent = loc.getWorld().spawn(loc, mobClass);


		if (_name != null)
		{
			_ent.setCustomName(name);
			_ent.setCustomNameVisible(true);
		}
		
		addEntityPart(_ent);

		SpawnCustom(_ent);

		game.CreatureAllowOverride = false;
	}
	
	protected void addEntityPart(Entity ent)
	{
		if(_parts.contains(ent)) return;
		_parts.add(ent);
	}
	
	public List<Entity> getEntityParts()
	{
		return new ArrayList<>(_parts);
	}
	
	public List<Player> getInsideBoundingBox()
	{
		List<Player> players = Host.GetPlayers(true);
		List<Player> list = UtilEnt.getPlayersInsideEntity(_ent, players);
		for(Entity ent : _parts)
		{
			players.removeAll(list);
			if(players.isEmpty()) break;
			
			list.addAll(UtilEnt.getPlayersInsideEntity(ent, players));
		}
		
		return list;
	}

	public abstract void SpawnCustom(T ent);

	public String GetName()
	{
		return _name;
	}

	public T GetEntity()
	{
		return _ent;
	}

	public Location GetTarget()
	{
		return _target;
	}

	public void SetTarget(Location loc)
	{
		_target = loc;
		_targetTime = System.currentTimeMillis();
	}

	public long GetTargetTime()
	{
		return _targetTime;
	}

	public Location GetPlayerTarget()
	{
		if (Host.GetPlayers(true).size() == 0)
		{
			return Host.GetTeamList().get(0).GetSpawn();
		}
		else
		{
			Player target = Host.GetPlayers(true).get(UtilMath.r(Host.GetPlayers(true).size()));
			return target.getLocation();
		}
	}

	public Location GetRoamTarget()
	{
		if (Math.random() > 0.75)
			return GetPlayerTarget();

		Location loc = null;
		
		while (loc == null || UtilMath.offset(loc, GetEntity().getLocation()) < 16)
		{
			loc = new Location(Host.GetSpectatorLocation().getWorld(), UtilMath.r(80) - 40, 5, UtilMath.r(80) - 40);	
		}
		
		return loc;
	}

	public boolean Updater(UpdateEvent event)
	{
		if (_ent == null || !_ent.isValid())
			return true;

		Update(event);

		return false;
	}

	public abstract void Update(UpdateEvent event);

	public abstract void Damage(CustomDamageEvent event);

	public abstract void Target(EntityTargetEvent event);
	
	public void CreatureMove(Creature creature)
	{
		CreatureMove(creature, 1);
	}

	public void CreatureMove(Creature creature, float speed)
	{
		//New Target
		SetTarget(GetRoamTarget());

		//Untarget if dead or far away
		if (creature.getTarget() != null)
		{
			if (UtilMath.offset2d(creature, creature.getTarget()) > 12 || !Host.IsAlive(creature.getTarget()))
			{
				creature.setTarget(null);
			}
		}
		//Move
		else
		{
			UtilEnt.CreatureMove(creature, GetTarget(), speed);
			Host.moves++;
		}
	}
	
	public void remove()
	{
		for(Entity ent : _parts)
		{
			if(ent instanceof Damageable)
			{
				((Damageable) ent).damage(((Damageable) ent).getHealth());
			}
			else
			{
				ent.remove();
			}
		}
	}
}
