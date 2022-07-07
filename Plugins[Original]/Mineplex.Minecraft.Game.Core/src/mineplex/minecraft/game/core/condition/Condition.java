package mineplex.minecraft.game.core.condition;

import net.minecraft.server.v1_8_R3.MobEffect;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.events.AddConditionEvent;

public class Condition 
{
	public enum ConditionType
	{
		CLOAK,
		UNTRUE_CLOAK,
		SHOCK,
		POISON_SHOCK,
		SILENCE,
		BURNING,
		FALLING,
		LIGHTNING,
		INVULNERABLE,
		EXPLOSION,
		FIRE_ITEM_IMMUNITY,
		ARCADE_HUNGER_DISABLE,

		CUSTOM,

		ABSORBTION,
		BLINDNESS,
		CONFUSION,
		DAMAGE_RESISTANCE,
		FAST_DIGGING,
		FIRE_RESISTANCE,
		HARM,
		HEAL,
		HEALTH_BOOST,
		HUNGER,
		INCREASE_DAMAGE,
		INVISIBILITY,
		JUMP,
		NIGHT_VISION,
		POISON,
		REGENERATION,
		SLOW,
		SLOW_DIGGING,
		SPEED,
		WATER_BREATHING,
		WEAKNESS,
		WITHER;

	}

	protected ConditionManager Manager;
	protected long _time;

	protected String _reason;

	protected String _informOn;
	protected String _informOff;

	protected LivingEntity _ent;
	protected LivingEntity _source;

	protected ConditionType _type;
	protected int _mult;	
	protected int _ticks;
	protected int _ticksTotal;
	protected boolean _ambient;

	protected Material _indicatorType;
	protected byte _indicatorData;

	protected boolean _add = false;
	protected boolean _live = false; 
	
	protected boolean _cancelPotion;
	
	protected boolean _showIndicator = true;

	public Condition(ConditionManager manager, String reason, LivingEntity ent, LivingEntity source, 
			ConditionType type, int mult, int ticks, boolean add, Material visualType, byte visualData, boolean showIndicator, boolean ambient)
	{
		Manager = manager;
		_time = System.currentTimeMillis();

		_reason = reason;

		_ent = ent;
		_source = source;

		_type = type;
		_mult = mult;
		_ticks = ticks;
		_ticksTotal = ticks;
		_ambient = ambient;

		_indicatorType = visualType;
		_indicatorData = visualData;
		_showIndicator = showIndicator;

		_add = add;

		//Live if NOT Additive
		_live = !add;
	}

	public Condition(ConditionManager manager, String reason, LivingEntity ent, LivingEntity source, 
			ConditionType type, int mult, int ticks, boolean add, Material visualType, byte visualData, boolean showIndicator, boolean ambient, boolean cancelPotion)
	{
		Manager = manager;
		_time = System.currentTimeMillis();

		_reason = reason;

		_ent = ent;
		_source = source;

		_type = type;
		_mult = mult;
		_ticks = ticks;
		_ticksTotal = ticks;
		_ambient = ambient;

		_indicatorType = visualType;
		_indicatorData = visualData;
		_showIndicator = showIndicator;

		_cancelPotion = cancelPotion;
		
		_add = add;

		//Live if NOT Additive
		_live = !add;
	}

	public Condition(ConditionManager manager, AddConditionEvent event)
	{

		this(manager, event.getReason(), event.getEnt(), event.getSource(), ConditionType.valueOf(event.getType().name()),
				event.getMult(), event.getTicks(), event.isAdd(), event.getIndicatorType(), event.getIndicatorData(),
				event.isShowIndicator(), event.isAmbient(), event.isCancelPotion());
	}

	public boolean Tick()
	{
		if (_live && _ticks > 0)
			_ticks--;

		return IsExpired();
	}
	
	public void OnConditionAdd()
	{
		
	}

	public void Apply()
	{
		_live = true;

		Add();
	}

	public void Add()
	{
		if (_cancelPotion)
		{
			return;
		}
		
		try
		{
			PotionEffectType type = PotionEffectType.getByName(_type.toString());

			boolean remove = false;
			for (PotionEffect effect : _ent.getActivePotionEffects())
			{
				if (effect.getType() == type && effect.getAmplifier() != _mult)
					remove = true;
			}

			// Remove
			if (remove)
				_ent.removePotionEffect(type);

			//Add
			PotionEffect effect = new PotionEffect(type, _ticks, _mult, _ambient, _showIndicator);
			if (_ticks == -1)
				_ent.addPotionEffect(new PotionEffect(type, 72000, _mult, _ambient, _showIndicator), true);
			else
				((CraftLivingEntity) _ent).getHandle().addEffect(new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), _showIndicator));
		}
		catch (Exception e)
		{

		}	
	}

	public void Remove()
	{
		PotionEffectType type = PotionEffectType.getByName(_type.toString());

		if (type != null)
		{
			_ent.removePotionEffect(type);
		}
	}

	public Material GetIndicatorMaterial()
	{
		return _indicatorType;
	}

	public byte GetIndicatorData()
	{
		return _indicatorData;
	}

	public LivingEntity GetEnt()
	{
		return _ent;
	}

	public LivingEntity GetSource()
	{
		return _source;
	}

	public boolean IsAdd()
	{
		return _add;
	}

	public ConditionType GetType()
	{
		return _type;
	}

	public int GetMult() 
	{
		return _mult;
	}

	public void SetLive(boolean live) 
	{
		_live = live;
	}

	public int GetTicks() 
	{
		return _ticks;
	}

	public int GetTicksTotal()
	{
		return _ticksTotal;
	}

	public String GetReason()
	{
		return _reason;
	}

	public long GetTime()
	{
		return _time;
	}

	public void Expire() 
	{
		_ticks = 0;

		Remove();
	}

	public void Restart() 
	{
		_ticks = _ticksTotal;
	}

	public boolean IsBetterOrEqual(Condition other, boolean additive)
	{
		if (this.GetMult() > other.GetMult())
			return true;

		if (this.GetMult() < other.GetMult())
			return false;
		
		if (additive)
			return true;

		if (this.GetTicks() >= other.GetTicks())
			return true;

		return false;
	}

	public boolean IsVisible()
	{
		return _showIndicator;
	}

	public boolean IsExpired() 
	{
		if (_ticks == -1)
			return false;

		return _ticks <= 0;
	}
	
	public boolean needsForceRemove()
	{
		return false;
	}

	public ConditionManager GetManager()
	{
		return Manager;
	}

	public String GetInformOn() 
	{
		return _informOn;
	}

	public String GetInformOff() 
	{
		return _informOff;
	}

	public void ModifyTicks(int amount) 
	{
		_ticks += amount;
		_ticksTotal += amount;
	}

	public void ModifyMult(int i) 
	{
		_mult = Math.max(0, _mult + i);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[ent=" + _ent + "]";
	}
}
