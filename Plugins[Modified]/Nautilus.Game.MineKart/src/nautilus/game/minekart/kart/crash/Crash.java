package nautilus.game.minekart.kart.crash;

import mineplex.core.common.util.UtilTime;
import nautilus.game.minekart.kart.Kart;

import org.bukkit.EntityEffect;
import org.bukkit.util.Vector;

public class Crash 
{
	private Vector _velocity;
	private long _crashTime;
	private long _crashReq;
	private boolean _canEnd;
	private boolean _restoreStability;
	
	public Crash(Kart kart, Vector vel, long timeReq, boolean canEnd, boolean restoreStability)
	{
		kart.SetCrash(this);
		kart.ClearDrift();
		
		_velocity = vel;
		_crashTime = System.currentTimeMillis();
		_crashReq = timeReq;
		_canEnd = canEnd;
		_restoreStability = restoreStability;
		
		kart.GetDriver().playEffect(EntityEffect.HURT);
	}
	
	public void Update(Kart kart)
	{
		Move(kart);
	}
	
	public void Move(Kart kart)
	{
		kart.GetDriver().setVelocity(_velocity);
		
		//Display Velocity as Exp
		kart.GetDriver().setExp(Math.min(0.999f, ((float)_velocity.length()/(float)1.8)));
	}
	
	public boolean CrashEnd()
	{
		return _canEnd && UtilTime.elapsed(_crashTime , _crashReq);
	}

	public Vector GetVelocity() 
	{
		return _velocity;
	}
	
	public void SetVelocity(Vector vel)
	{
		_velocity = vel;
	}
	
	public long GetCrashTime()
	{
		return _crashTime;
	}
	
	public long GetCrashReq()
	{
		return _crashReq;
	}
	
	public void SetCanEnd(boolean end)
	{
		_canEnd = end;
	}
	
	public void SetCrashReq(long time)
	{
		_crashReq = time;
	}
	
	public boolean StabilityRestore()
	{
		return _restoreStability;
	}
}
