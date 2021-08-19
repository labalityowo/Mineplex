package mineplex.core.mavericks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.util.Vector;

import com.java.sk89q.jnbt.CompoundTag;
import com.java.sk89q.jnbt.NBTUtils;
import com.java.sk89q.jnbt.Tag;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.UtilParticle.ParticleType;

/**
 * A simple wrapper class for Mavericks-MasterBuilders SQL data
 */
public class MavericksBuildWrapper
{
	private final long _buildId;
	private final UUID _uuid;
	private final String _theme;
	private final double _points;
	private final int _place;
	private final long _dateStamp;
	private final byte[] _schematic;
	private Schematic _schematicCache;
	private final byte[] _particles;
	private Map<Vector, ParticleType> _particlesCache;
	
	private boolean _reviewed;
	
	private final String _name;
	
	
	public MavericksBuildWrapper(long buildId, UUID uuid, String name, String theme, double points, int place, long dateStamp, 
			byte[] schematic, byte[] particles, boolean reviewed)
	{
		this._buildId = buildId;
		this._uuid = uuid;
		this._name = name;
		this._theme = theme;
		this._points = points;
		this._place = place;
		this._dateStamp = dateStamp;
		this._schematic = schematic;
		this._particles = particles;
		this._reviewed = reviewed;
	}
	
	public MavericksBuildWrapper(long buildId, UUID uuid, String theme, double points, int place, long dateStamp, 
			byte[] schematic, byte[] particles, boolean reviewed)
	{
		this(buildId, uuid, null, theme, points, place, dateStamp, schematic, particles, reviewed);
	}
	
	public long getBuildId()
	{
		return _buildId;
	}
	
	public UUID getUUID()
	{
		return _uuid;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean hasNameSet()
	{
		return _name != null;
	}
	
	public String getTheme()
	{
		return _theme;
	}
	
	public double getPoints()
	{
		return _points;
	}
	
	public int getPlace()
	{
		return _place;
	}
	
	public long getDateStamp()
	{
		return _dateStamp;
	}
	
	public byte[] getSchematicBytes()
	{
		return _schematic;
	}
	
	public boolean isReviewed()
	{
		return _reviewed;
	}
	
	public void setReviewed(boolean reviewed)
	{
		_reviewed = reviewed;
	}
	
	public Schematic getSchematic()
	{
		if(_schematicCache != null) return _schematicCache;
		try 
		{
			return _schematicCache = UtilSchematic.loadSchematic(_schematic);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean hasParticles()
	{
		return _particles != null && _particles.length > 0;
	}
	
	public byte[] getParticlesRaw()
	{
		return _particles;
	}
	
	public Map<Vector, ParticleType> getParticles()
	{
		if(_particlesCache != null) return _particlesCache;
		
		Map<Vector, ParticleType> map = new HashMap<>();
		if(!hasParticles()) return map;
		
		try 
		{
			CompoundTag tag = (CompoundTag) NBTUtils.getFromBytesCompressed(_particles).getTag();
			for(Entry<String, Tag> e : tag.getValue().entrySet())
			{
				CompoundTag parent = (CompoundTag) e.getValue();
				
				Vector v = NBTUtils.getVector(parent);
				ParticleType particle = ParticleType.valueOf(parent.getString("particle"));
				
				while(map.containsKey(v)) v.add(new Vector(0.00000001, 0, 0));
				map.put(v, particle);
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return _particlesCache = map;
	}
	
	@Override
	public String toString()
	{
		return "MavericksBuildWrapper[uuid='" + _uuid + "',theme='" + _theme + "',points=" + _points + ",place=" + _place
				+ ",date=" + _dateStamp + ",Schematic=ByteArray[" + _schematic.length + "]]";
	}
	
}
