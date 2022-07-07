package nautilus.game.arcade.game.games.wizards;

import java.util.Set;

import mineplex.core.common.util.NautHashMap;

public class Wizard
{

	private NautHashMap<SpellType, Long> _cooldowns = new NautHashMap<SpellType, Long>();
	private NautHashMap<SpellType, Integer> _knownSpells = new NautHashMap<SpellType, Integer>();
	private SpellType[] _assignedWands = new SpellType[5];
	private float _mana;
	private float _manaPerTick = 2.5F / 20F;
	private float _maxMana;
	private int _soulStars;
	private float _cooldownModifier = 1;
	private int _wandsOwned;

	public void setWandsOwned(int wandsOwned)
	{
		_wandsOwned = wandsOwned;
	}

	public int getWandsOwned()
	{
		return _wandsOwned;
	}

	public float getCooldownModifier()
	{
		return _cooldownModifier;
	}

	public void decreaseCooldown()
	{
		_cooldownModifier -= 0.1;
	}

	public void addSoulStar()
	{
		_soulStars++;
	}

	public Wizard(float maxMana)
	{
		learnSpell(SpellType.ManaBolt);
		learnSpell(SpellType.WizardsCompass);

		_maxMana = maxMana;
	}

	public SpellType getSpell(int slot)
	{
		return _assignedWands[slot];
	}

	public void setSpell(int slot, SpellType spell)
	{
		_assignedWands[slot] = spell;
	}

	public long getCooldown(SpellType type)
	{
		if (_cooldowns.containsKey(type) && _cooldowns.get(type) >= System.currentTimeMillis())
		{
			return _cooldowns.get(type);
		}

		return 0;
	}

	public float getMana()
	{
		return _mana;
	}

	public float getManaPerTick()
	{
		return _manaPerTick + ((_soulStars * 0.2F) / 20);
	}

	public float getMaxMana()
	{
		return _maxMana;
	}

	public int getSpellLevel(SpellType type)
	{
		if (_knownSpells.containsKey(type))
		{
			return _knownSpells.get(type);
		}
		return 0;
	}

	public void learnSpell(SpellType type)
	{
		_knownSpells.put(type, getSpellLevel(type) + 1);
	}

	public void addMana(float newMana)
	{
		_mana += newMana;

		if (_mana > _maxMana)
		{
			_mana = _maxMana;
		}
	}

	public void setMana(float newMana)
	{
		_mana = newMana;
	}

	public void setUsedSpell(SpellType spell)
	{
		int cooldown = spell.getSpellCooldown(this);

		if (cooldown > 0)
		{
			_cooldowns.put(spell, System.currentTimeMillis() + (1000L * cooldown));
		}
	}

	public NautHashMap<SpellType, Long> getCooldowns()
	{
		return _cooldowns;
	}

	public void setManaPerTick(float manaPerTick)
	{
		_manaPerTick = manaPerTick;
	}

	public Set<SpellType> getKnownSpells()
	{
		return _knownSpells.keySet();
	}
}
