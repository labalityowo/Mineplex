package mineplex.core.npc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.LivingEntity;
import net.minecraft.server.v1_8_R3.EntityCreature;

import mineplex.core.common.util.C;
import mineplex.database.tables.records.NpcsRecord;

public class Npc
{
	private final NpcManager _npcManager;
	private final NpcsRecord _databaseRecord;
	private Location _location;
	private LivingEntity _entity;
	private int _failedAttempts = 0;
	private boolean _returning = false;
	private final String[] _info;
	private final Double _infoRadiusSquared;

	public Npc(NpcManager npcManager, NpcsRecord databaseRecord)
	{
		_npcManager = npcManager;
		_databaseRecord = databaseRecord;

		Double yaw = getDatabaseRecord().getYaw();
		Double pitch = getDatabaseRecord().getPitch();

		if(yaw == null)
		{
			yaw = 0d;
		}

		if(pitch == null)
		{
			pitch = 0d;
		}

		_location = new Location(Bukkit.getWorld(getDatabaseRecord().getWorld()),
		  getDatabaseRecord().getX(),
		  getDatabaseRecord().getY(),
		  getDatabaseRecord().getZ(),
		  yaw.floatValue(),
		  pitch.floatValue());

		if (getDatabaseRecord().getInfo() == null)
			_info = null;
		else
		{
			String[] info = getDatabaseRecord().getInfo().split("\\r?\\n");

			for (int i = 0; i < info.length; i++)
			{
				for (ChatColor color : ChatColor.values())
					info[i] = info[i].replace("(" + color.name().toLowerCase() + ")", color.toString());
				info[i] = ChatColor.translateAlternateColorCodes('&', info[i]);
			}
			
			_info = new String[info.length + 2];
			
			for (int i=0 ; i<_info.length ; i++)
			{
				if (i == 0 || i == _info.length-1)
				{
					_info[i] = C.cGold + C.Strike + "=============================================";
				}
				else
				{
					_info[i] = info[i-1];
				}
			}
		}

		if (getDatabaseRecord().getInfoRadius() == null)
			_infoRadiusSquared = null;
		else
			_infoRadiusSquared = getDatabaseRecord().getInfoRadius() * getDatabaseRecord().getInfoRadius();
	}

	public void setEntity(LivingEntity entity)
	{
		if (_entity != null)
			getNpcManager()._npcMap.remove(_entity.getUniqueId());

		_entity = entity;

		if (_entity != null)
			getNpcManager()._npcMap.put(_entity.getUniqueId(), this);
	}

	public LivingEntity getEntity()
	{
		return _entity;
	}

	public NpcsRecord getDatabaseRecord()
	{
		return _databaseRecord;
	}

	public int getFailedAttempts()
	{
		return _failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts)
	{
		_failedAttempts = failedAttempts;
	}

	public int incrementFailedAttempts()
	{
		return ++_failedAttempts;
	}

	public Location getLocation()
	{
		return _location;
	}

	public double getRadius()
	{
		return getDatabaseRecord().getRadius();
	}

	public boolean isInRadius(Location location)
	{
		if (location.getWorld() != getLocation().getWorld())
			return false;

		return location.distanceSquared(getLocation()) <= getRadius() * getRadius();
	}

	public void returnToPost()
	{
		if (_entity instanceof CraftCreature)
		{
			EntityCreature ec = ((CraftCreature) _entity).getHandle();

			ec.getNavigation().a(getLocation().getX(), getLocation().getY(), getLocation().getZ(), .8f);

			_returning = true;
		}
	}

	public boolean isReturning()
	{
		return _returning;
	}

	public void clearGoals()
	{
		if (_entity instanceof CraftCreature)
		{
			_returning = false;

			Location entityLocation = _entity.getLocation();
			EntityCreature ec = ((CraftCreature) _entity).getHandle();
			ec.getNavigation().a(entityLocation.getX(), entityLocation.getY(), entityLocation.getZ(), .8f);
		}
	}

	public NpcManager getNpcManager()
	{
		return _npcManager;
	}

	public Chunk getChunk()
	{
		return getLocation().getChunk();
	}

	public String[] getInfo()
	{
		return _info;
	}

	public Double getInfoRadiusSquared()
	{
		return _infoRadiusSquared;
	}

	public void setLocation(Location location)
	{
		_location = location;
	}

	@Override
	public String toString()
	{
		return "NPC[entity=" + _entity + "]";
	}
}
