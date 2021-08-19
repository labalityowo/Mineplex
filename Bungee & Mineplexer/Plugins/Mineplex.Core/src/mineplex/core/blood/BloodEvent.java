package mineplex.core.blood;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BloodEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean _cancelled = false;
	
	private Player _player;
	private Location _loc;
	private int _particles;
	private double _velMult;
	private Sound _sound;
	private float _soundVol;
	private float _soundPitch;
	private Material _type;
	private byte _data;
	private int _ticks;
	private boolean _bloodStep;
	
	public BloodEvent(Player player, Location loc, int particles, double velMult, Sound sound,
			float soundVol, float soundPitch, Material type, byte data,
			int ticks, boolean bloodStep)
	{
		_player = player;
		_loc = loc;
		_particles = particles;
		_velMult = velMult;
		_sound = sound;
		_soundVol = soundVol;
		_soundPitch = soundPitch;
		_type = type;
		_data = data;
		_ticks = ticks;
		_bloodStep = bloodStep;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean var)
	{
		_cancelled = var;
	}

	public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }
	
	public Player getPlayer()
	{
		return _player;
	}

	public Location getLocation()
	{
		return _loc;
	}
	
	public int getParticles()
	{
		return _particles;
	}
	
	public double getVelocityMult()
	{
		return _velMult;
	}
	
	public Sound getSound()
	{
		return _sound;
	}
	
	public float getSoundVolume()
	{
		return _soundVol;
	}
	
	public float getSoundPitch()
	{
		return _soundPitch;
	}
	
	public Material getMaterial()
	{
		return _type;
	}
	
	public byte getMaterialData()
	{
		return _data;
	}
	
	public int getTicks()
	{
		return _ticks;
	}
	
	public boolean getBloodStep()
	{
		return _bloodStep;
	}
	
	public void setItem(Material mat, byte data)
	{
		_type = mat;
		_data = data;
	}
}

