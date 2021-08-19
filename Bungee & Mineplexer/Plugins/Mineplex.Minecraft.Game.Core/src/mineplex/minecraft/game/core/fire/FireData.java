package mineplex.minecraft.game.core.fire;

import org.bukkit.entity.LivingEntity;

public class FireData 
{
	private LivingEntity _owner;
	private long _expireTime; 
	private long _delayTime;
	private double _burnTime;
	private double _damage;
	private String _skillName;
	private boolean _hitOwner;
	
	public FireData(LivingEntity owner, double expireTime, double delayTime, double burnTime, double damage, String skillName, boolean hitSelf)
	{
		_owner = owner;
		_expireTime = System.currentTimeMillis() + (long)(1000 * expireTime);
		_delayTime = System.currentTimeMillis() + (long)(1000 * delayTime);
		_burnTime = burnTime;
		_damage = damage;
		_skillName = skillName;
		_hitOwner = hitSelf;
	}
	
	public LivingEntity GetOwner()
	{
		return _owner;
	}
	
	public double GetBurnTime()
	{
		return _burnTime;
	}
	
	public double GetDamage()
	{
		return _damage;
	}
	
	public String GetName()
	{
		return _skillName;
	}
	
	public boolean IsPrimed()
	{
		return System.currentTimeMillis() > _delayTime;
	}
	
	public boolean Expired()
	{
		return System.currentTimeMillis() > _expireTime;
	}
	
	public boolean canHitOwner()
	{
		return _hitOwner;
	}
}
