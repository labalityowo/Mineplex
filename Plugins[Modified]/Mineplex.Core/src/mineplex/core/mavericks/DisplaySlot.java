package mineplex.core.mavericks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
/**
 * Represents a display slot to display Mavericks Master Builders builds.
 */
public class DisplaySlot
{

	private MavericksApprovedWrapper _data;
	private Location _loc;
	private ArrayList<Entity> _pastedEntities = new ArrayList<>();
	private Map<Vector, ParticleType> _particles = null;
	private List<Hologram> _holograms = new ArrayList<>();
	
	/**
	 * @param loc The minimum corner of where the build will be pasted in.
	 */
	public DisplaySlot(Location loc, HologramManager hologramManager)
	{
		_loc = loc.clone();
		for(int i = 0; i < 4; i++)
		{
			_holograms.add(new Hologram(hologramManager, _loc.clone(), "Built by ???", "Theme: ???"));
		}
	}
	
	public MavericksApprovedWrapper getData()
	{
		return _data;
	}
	
	public void setData(MavericksApprovedWrapper data)
	{
		clearEntities();
		
		for(Hologram h : _holograms)
			h.stop();
		
		Schematic schematic = data.getBuild().getSchematic();
		
		Location a = _loc;
		Location b = _loc.clone().add(schematic.getWidth(), schematic.getHeight(), schematic.getLength());
		
		UtilBlock.startQuickRecording();
		for(int x = a.getBlockX(); x < b.getX(); x++)
		{
			// Ignore the floor to keep the outer ring
			for(int y = a.getBlockY() + 1; y < b.getY(); y++)
			{
				for(int z = a.getBlockZ(); z < b.getZ(); z++)
				{
					UtilBlock.setQuick(a.getWorld(), x, y, z, 0, (byte) 0);
				}
			}
		}
		SchematicData pasteData = schematic.paste(_loc, true, false);
		for(Entity e : pasteData.getEntities())
		{
			if(e instanceof Item)
			{
				//Don't despawn
				e.setTicksLived(32768);
				//Prevent Pickup
				((Item)e).setPickupDelay(32767);
			}
			else
			{
				UtilEnt.vegetate(e, true);
				UtilEnt.ghost(e, true, false);
			}
			_pastedEntities.add(e);
		}
		
		_particles = data.getBuild().getParticles();
		
		boolean wasNull = _data == null;
		
		_data = data;
		
		//Only need to set locations first time after we get the data
		if(wasNull)
		{
			setHologramLocations();
		}
		for(Hologram h : _holograms)
		{
			h.setText(
				C.cGray + "Built by " + C.cYellow + C.Bold + _data.getBuild().getName(), 
				C.cGray + "Theme: " + C.cYellow + C.Bold + data.getBuild().getTheme());
			h.start();
		}
	}
	
	/**
	 * Send all the entities to nearby players. Should be called every 10 ticks.
	 */
	public void updateParticles()
	{
		if(_particles == null) return;
		
		for(Entry<Vector, ParticleType> e : _particles.entrySet())
		{
			Location loc = _loc.clone().add(e.getKey());
			
			ParticleType type = e.getValue();
			
			int amount = 8;
			
			if (type == ParticleType.HUGE_EXPLOSION ||
					type == ParticleType.LARGE_EXPLODE ||
					type == ParticleType.NOTE)
					amount = 1;
			
			UtilParticle.PlayParticleToAll(type, loc, 0.4f, 0.4f, 0.4f, 0, amount, ViewDist.LONG); 
		}
	}
	
	private void clearEntities()
	{
		for(Entity e : _pastedEntities)
		{
			e.remove();
		}
		_pastedEntities.clear();
	}
	
	/**
	 * @param e The entity you want to check.
	 * @return Returns true if this entity is spawned in by this display slot.
	 */
	public boolean isDisplaySlotEntity(Entity e)
	{
		return _pastedEntities.contains(e);
	}
	
	public boolean isInside(Location loc)
	{
		if(!_loc.getWorld().equals(loc.getWorld())) return false;
		if(_data == null) return false;
		
		Schematic s = _data.getBuild().getSchematic();
		
		Location min = _loc.clone();
		Location max = _loc.clone().add(s.getWidth(), s.getHeight(), s.getLength());
		
		return UtilAlg.inBoundingBox(loc, min, max);
	}
	
	public void setHologramLocations()
	{
		if(_data == null) return;
		
		Schematic s = _data.getBuild().getSchematic();
		
		Location min = _loc.clone();
		
		double height = 4;
		
		_holograms.get(0).setLocation(min.clone().add(s.getWidth()/2.0, height, -1.5));
		_holograms.get(1).setLocation(min.clone().add(s.getWidth()/2.0, height, s.getLength() + 1.5));
		
		_holograms.get(2).setLocation(min.clone().add(-1.5, height, s.getLength()/2.0));
		_holograms.get(3).setLocation(min.clone().add(s.getWidth() + 1.5, height, s.getLength()/2.0));
		
	}

}
