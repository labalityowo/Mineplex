package mineplex.minecraft.game.core.combat;

import java.util.List;
import java.util.Map;

import mineplex.minecraft.game.core.damage.DamageChange;

public class CombatDamage
{
	private final String _name;
	private final double _dmg;
	private final long _time;
	private final List<DamageChange> _mod;
	private final Map<String, Object> _metadata;

	CombatDamage(String name, double dmg, List<DamageChange> mod, Map<String, Object> metadata)
	{
		_name = name;
		_dmg = dmg;
		_time = System.currentTimeMillis();
		_mod = mod;
		_metadata = metadata;
	}

	public String GetName()
	{
		return _name;
	}

	public double GetDamage()
	{
		return _dmg;
	}
	
	public long GetTime()
	{
		return _time;
	}

	public List<DamageChange> getDamageMod()
	{
		return _mod;
	}

	/**
	 * Retrieves metadata that was associated with this damage via
	 * {@link mineplex.minecraft.game.core.damage.CustomDamageEvent#setMetadata(String, Object)}.
	 * <p/>
	 * There is no standardized metadata that should be expected.  Metadata is meant to be used
	 * on a per-minigame basis to store additional information about the damage.
	 * @return a non-null map containing the metadata
	 */
	public Map<String, Object> getMetadata()
	{
		return _metadata;
	}
}
