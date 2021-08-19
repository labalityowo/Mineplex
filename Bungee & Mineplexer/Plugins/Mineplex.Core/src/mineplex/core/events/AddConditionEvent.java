package mineplex.core.events;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AddConditionEvent extends Event
{

	public enum CoreConditionType
	{
		CLOAK,
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
		WITHER
	}

	/**
	 * Allows adding Condition outside Arcade module
	 */

	private static final HandlerList handlers = new HandlerList();

	protected long _time;

	protected String _reason;

	protected String _informOn;
	protected String _informOff;

	protected LivingEntity _ent;
	protected LivingEntity _source;

	protected CoreConditionType _type;
	protected int _mult;
	protected int _ticks;
	protected int _ticksTotal;
	protected boolean _ambient;

	protected Material _indicatorType;
	protected byte _indicatorData;

	protected boolean _add = false;
	protected boolean _live = false;

	protected boolean _cancelPotion = false;

	protected boolean _showIndicator = true;

	public AddConditionEvent(String reason, LivingEntity ent, LivingEntity source,
							 CoreConditionType type, int mult, int ticks, boolean add, Material visualType, byte visualData, boolean showIndicator, boolean ambient)
	{
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

	public AddConditionEvent(String reason, LivingEntity ent, LivingEntity source,
							 CoreConditionType type, int mult, int ticks, boolean add, Material visualType, byte visualData, boolean showIndicator, boolean ambient, boolean cancelPotion)
	{
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

	public long getTime()
	{
		return _time;
	}

	public String getReason()
	{
		return _reason;
	}

	public String getInformOn()
	{
		return _informOn;
	}

	public String getInformOff()
	{
		return _informOff;
	}

	public LivingEntity getEnt()
	{
		return _ent;
	}

	public LivingEntity getSource()
	{
		return _source;
	}

	public CoreConditionType getType()
	{
		return _type;
	}

	public int getMult()
	{
		return _mult;
	}

	public int getTicks()
	{
		return _ticks;
	}

	public int getTicksTotal()
	{
		return _ticksTotal;
	}

	public boolean isAmbient()
	{
		return _ambient;
	}

	public Material getIndicatorType()
	{
		return _indicatorType;
	}

	public byte getIndicatorData()
	{
		return _indicatorData;
	}

	public boolean isAdd()
	{
		return _add;
	}

	public boolean isLive()
	{
		return _live;
	}

	public boolean isCancelPotion()
	{
		return _cancelPotion;
	}

	public boolean isShowIndicator()
	{
		return _showIndicator;
	}

	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}
