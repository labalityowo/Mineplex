package mineplex.minecraft.game.core.damage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;

public class CustomDamageEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	private DamageCause _eventCause;
	private double _initialDamage;

	private ArrayList<DamageChange> _damageMult = new ArrayList<DamageChange>();
	private ArrayList<DamageChange> _damageMod = new ArrayList<DamageChange>();

	private ArrayList<String> _cancellers = new ArrayList<String>();

	private HashMap<String, Double> _knockbackMod = new HashMap<String, Double>();

	private Map<String, Object> _metadata = new HashMap<>();
	//Ents
	private LivingEntity _damageeEntity;
	private Player _damageePlayer;
	private LivingEntity _damagerEntity;
	private Player _damagerPlayer;
	private Projectile _projectile;
	private Location _knockbackOrigin = null;

	//Flags
	private boolean _ignoreArmor = false; 
	private boolean _ignoreRate = false;		
	private boolean _knockback = true;
	private boolean _damageeBrute = false;
	private boolean _damageToLevel = true;
	private boolean _arrowShow = true;
	private boolean _projectileDamageSelf = false;

	public CustomDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile projectile, Location knockbackOrigin,
			DamageCause cause, double damage, boolean knockback, boolean ignoreRate, boolean ignoreArmor, String initialSource,
			String initialReason, boolean cancelled)
	{
		_eventCause = cause;

		//if (initialSource == null || initialReason == null)
			_initialDamage = damage;

		_damageeEntity = damagee;
		if (_damageeEntity != null && _damageeEntity instanceof Player)			_damageePlayer = (Player)_damageeEntity;

		_damagerEntity = damager;
		if (_damagerEntity != null && _damagerEntity instanceof Player)			_damagerPlayer = (Player)_damagerEntity;

		_projectile = projectile;

		_knockback = knockback;
		_ignoreRate = ignoreRate;
		_ignoreArmor = ignoreArmor;

		if (initialSource != null && initialReason != null)
			AddMod(initialSource, initialReason, 0, true);
		
		if (_eventCause == DamageCause.FALL)
			_ignoreArmor = true;
		
		if (cancelled)
			SetCancelled("Pre-Cancelled");

		_knockbackOrigin = knockbackOrigin;
	}

	@Override
	public HandlerList getHandlers() 
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers; 
	}

	public void AddMult(String source, String reason, double mod, boolean useAttackName)
	{
		_damageMult.add(new DamageChange(source, reason, mod, useAttackName));
	}

	public void AddMod(String source, double mod)
	{
		AddMod(source, "", mod, false);
	}

	public void AddMod(String source, String reason, double mod, boolean useAttackName)
	{
		_damageMod.add(new DamageChange(source, reason, mod, useAttackName));
	}

	public void AddKnockback(String reason, double d)
	{
		_knockbackMod.put(reason, d);
	}

	public boolean IsCancelled()
	{
		return !_cancellers.isEmpty();
	}

	public void SetCancelled(String reason)
	{
		_cancellers.add(reason);
	}

	public double GetDamage()
	{
		double damage = GetDamageInitial();

		for (DamageChange mult : _damageMod)
			damage += mult.GetDamage();
		
		for (DamageChange mult : _damageMult)
			damage *= mult.GetDamage();

		return damage;
	}

	public LivingEntity GetDamageeEntity()
	{
		return _damageeEntity;
	}

	public Player GetDamageePlayer()
	{
		return _damageePlayer;
	}

	public LivingEntity GetDamagerEntity(boolean ranged)
	{
		if (ranged)
			return _damagerEntity;

		else if (_projectile == null)	
			return _damagerEntity;

		return null;
	}

	public Player GetDamagerPlayer(boolean passthroughRanged)
	{
		if (passthroughRanged)
			return _damagerPlayer;

		else if (_projectile == null)	
			return _damagerPlayer;

		return null;
	}

	public Projectile GetProjectile()
	{
		return _projectile;
	}
	
	public boolean getProjectileDamageSelf()
	{
		return _projectileDamageSelf;
	}
	
	public void setProjectileDamageSelf(boolean projectileDamageSelf)
	{
		_projectileDamageSelf = projectileDamageSelf;
		if(!projectileDamageSelf)
		{
			_cancellers.remove("Self Projectile Damage");
		}
		else
		{
			if(!_cancellers.contains("Self Projectile Damage"))
			{
				_cancellers.add("Self Projectile Damage");
			}
		}
	}
	
	public void setShowArrows(boolean show)
	{
		_arrowShow = show;
	}
	
	public boolean getShowArrows()
	{
		return _arrowShow;
	}

	public DamageCause GetCause()
	{
		return _eventCause;
	}

	public double GetDamageInitial()
	{
		return _initialDamage;
	}

	public void SetIgnoreArmor(boolean ignore)
	{
		if (ignore)
		{
			_cancellers.removeIf(reason -> reason.equals("World/Monster Damage Rate"));
		}

		_ignoreArmor = ignore;
	}

	/**
	 * A warning to those using this method, the {@link DamageManager} cancels for rate on {@link org.bukkit.event.EventPriority#LOW}, which is a problem.
	 * So only call this on {@link org.bukkit.event.EventPriority#LOWEST} otherwise it will not work. It's stupid but there you go.
	 */
	public void SetIgnoreRate(boolean ignore) 
	{
		_ignoreRate = ignore;
	}

	public void SetKnockback(boolean knockback) 
	{
		_knockback = knockback;
	}

	public void SetBrute()
	{
		_damageeBrute = true;
	}

	public boolean IsBrute()
	{
		return _damageeBrute;
	}

	public String GetReason() 
	{
		StringBuilder reason = new StringBuilder();

		//Get Reason
		for (DamageChange change : _damageMod)
		{
			if (change.UseReason())
			{
				reason
						.append(C.mSkill)
						.append(change.GetReason())
						.append(C.mBody)
						.append(", ");
			}
		}

		//Trim Reason
		if (reason.length() > 0)
		{
			return reason.substring(0, reason.length() - 2);
		}

		return null;
	}

	public boolean IsKnockback() 
	{
		return _knockback;
	}

	public boolean IgnoreRate() 
	{
		return _ignoreRate;
	}

	public boolean IgnoreArmor() 
	{
		return _ignoreArmor;
	}

	public void SetDamager(LivingEntity ent) 
	{
		if (ent == null)
			return;

		_damagerEntity = ent;

		_damagerPlayer = null;
		if (ent instanceof Player)
			_damagerPlayer = (Player)ent;
	}
	
	public void setDamagee(LivingEntity ent) 
	{
		_damageeEntity = ent;
		_damageePlayer = ent instanceof Player ? (Player) ent : null;
	}

	public void setDamager(LivingEntity ent)
	{
		_damagerEntity = ent;
		_damagerPlayer = ent instanceof Player ? (Player) ent : null;
	}
	
	public void setKnockbackOrigin(Location loc)
	{
		_knockbackOrigin = loc;
	}
	
	public Location getKnockbackOrigin()
	{
		return _knockbackOrigin;
	}

	public ArrayList<DamageChange> GetDamageMod()
	{
		return _damageMod;
	}

	public ArrayList<DamageChange> GetDamageMult()
	{
		return _damageMult;
	}

	public HashMap<String, Double> GetKnockback() 
	{
		return _knockbackMod;
	}
	
	public double getKnockbackValue()
	{
		double value = 0.0d;
		
		for (double knockback : _knockbackMod.values())
		{
			value += knockback;
		}
		
		return value;
	}
	
	/**
	 * Invert knockback modifier values to their negative counterparts.
	 */
	public void invertKnockback()
	{
		for (String key : _knockbackMod.keySet())
		{
			double value = _knockbackMod.get(key);
			_knockbackMod.put(key, -value);
		}
	}

	public ArrayList<String> GetCancellers() 
	{
		return _cancellers;
	}
	
	public void SetDamageToLevel(boolean val)
	{
		_damageToLevel = val;
	}

	public boolean DisplayDamageToLevel() 
	{
		return _damageToLevel;
	}

    @Override
    public boolean isCancelled()
    {
        return IsCancelled();
    }

	/**
	 * Associates the provided metadata key with the provided value.
	 * Metadata can later be retrieved from individual {@link
	 * mineplex.minecraft.game.core.combat.CombatDamage} instances acquired
	 * from the {@link mineplex.minecraft.game.core.combat.CombatLog}.
	 *
	 * @param key non-null key to associate the value with
	 * @param value nullable value
	 * @throws IllegalArgumentException if key is null
	 */
	public void setMetadata(String key, Object value)
	{
		Validate.notNull(key);
		_metadata.put(key, value);
	}

	/**
	 * Gets all Metadata associated with this event. There is
	 * no standardized metadata that should be expected.
	 *
	 * @see #setMetadata(String, Object)
	 * @return non-null map of metadata
	 */
	public Map<String, Object> getMetadata()
	{
		return _metadata;
	}

    @Override
    @Deprecated
    /**
     * Don't call this method. Use SetCancelled(String) instead.
     * 
     * You will be made the butt of jokes if you use this method.
     */
    public void setCancelled(boolean isCancelled)
    {
        SetCancelled("No reason given because SOMEONE IS AN IDIOT");
    }

	
}
