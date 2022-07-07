package mineplex.game.clans.fields.monsters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.fields.FieldMonster;
import mineplex.game.clans.fields.UtilField;
import mineplex.game.clans.fields.repository.FieldMonsterToken;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

public class FieldMonsterBase implements Listener
{
	public FieldMonster Manager;
	
	private String _name;
	private String _serverName;
	private EntityType _type;
	
	private int _mobMax;
	private Map<Entity, Integer> _mobs = new HashMap<Entity, Integer>();
	
	private List<Location> _locs = new ArrayList<Location>();
	
	private double _mobRate = 0.5;
	private long _mobLast = 0;
	
	private Location _centre;
	private int _radius;
	private int _height;
	
	public FieldMonsterBase(FieldMonster manager, String name, String serverName, EntityType type, int mobMax, double mobRate, Location centre, int radius, int height)
	{
		Manager = manager;
		
		_name = name;
		_serverName = serverName;
		_type = type;
		
		_mobMax = mobMax;
		_mobRate = mobRate;
		
		_centre = centre;
		_radius = radius;
		_height = height;
		
		
		PopulateLocations();	
	}
	
	@EventHandler
	public void Spawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (_locs.isEmpty())
			return;
		
		if (_mobs.size() >= _mobMax)
			return;
		
		if (!UtilTime.elapsed(_mobLast, UtilField.scale((long) (_mobRate * 60000))))
			return;
		
		_mobLast = System.currentTimeMillis();
		
		Entity ent = Manager.getCreature().SpawnEntity(SelectLocation(), _type);
		_mobs.put(ent, 0);
	}
	
	@EventHandler
	public void Despawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		HashSet<Entity> remove = new HashSet<Entity>();
		
		for (Entity mob : _mobs.keySet())
			if (mob == null || mob.isDead() || !mob.isValid())
				remove.add(mob);		

		for (Entity mob : remove)
			_mobs.remove(mob);
	}
	
	@EventHandler
	public void Return(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
	
		for (Entity mob : _mobs.keySet())
			if (UtilMath.offset2d(mob.getLocation(), _centre) > 2*_radius)
			{
				int time = _mobs.get(mob) + 1;	
				_mobs.put(mob, time);
				
				Location loc = SelectLocation();
				if (loc == null)
					continue;
				
				//Move
				if (time < 10)
				{
					EntityCreature ec = ((CraftCreature)mob).getHandle();
					NavigationAbstract nav = ec.getNavigation();
					nav.a(loc.getX(), loc.getY(), loc.getZ(), 0.2f);
				}
				
				//Extreme
				else 
				{
					mob.teleport(loc);
				}
			}	
			else
			{
				_mobs.put(mob, 0);
			}
	}
	
	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (_mobs.containsKey(event.getEntity()))
			event.setCancelled(true);
	}
	
	public void PopulateLocations() 
	{
		int attempts = 0;
		while (_locs.size() < (_radius*_radius) && attempts < 2000)
		{
			attempts++;
			
			Block block = _centre.getBlock().getRelative(UtilMath.r(_radius * 2) - _radius, UtilMath.r(_height * 2) - _height, UtilMath.r(_radius * 2) - _radius);
			
			if (!UtilBlock.solid(block))
				continue;
			
			if (!UtilBlock.airFoliage(block.getRelative(0,1,0)))
				continue;
			
			if (!UtilBlock.airFoliage(block.getRelative(0,2,0)))
				continue;
			
			if (_locs.contains(block.getLocation()))
				continue;
			
			_locs.add(block.getLocation().add(0.5, 1.5, 0.5));	
		}
	}

	public Location SelectLocation()
	{
		if (_locs.isEmpty())
			return null;
		
		return _locs.get(UtilMath.r(_locs.size()));
	}

	public double GetRadius() 
	{
		return _radius;
	}
	
	public String GetName()
	{
		return _name;
	}

	public void Display(Player caller) 
	{
		UtilPlayer.message(caller, F.value(_name, _type.toString() + " " + UtilWorld.locToStrClean(_centre)));
	}

	public void RemoveMonsters() 
	{
		for (Entity ent : _mobs.keySet())
			ent.remove();
		
		_mobs.clear();
	}

	public FieldMonsterToken GetToken() 
	{
		FieldMonsterToken token = new FieldMonsterToken();
		
		token.Name = _name;
		token.Server = _serverName;
		token.Type = _type.toString();
		token.MobMax = _mobMax;
		token.MobRate = _mobRate;
		token.Centre = UtilWorld.locToStr(_centre);
		token.Radius = _radius;
		token.Height = _height;
		
		return token;
	}
}
