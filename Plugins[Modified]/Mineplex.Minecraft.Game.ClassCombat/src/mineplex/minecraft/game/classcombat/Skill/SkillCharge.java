package mineplex.minecraft.game.classcombat.Skill;

import java.util.WeakHashMap;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;

import org.bukkit.entity.Player;

public class SkillCharge extends Skill
{
	protected WeakHashMap<Player, Float> _charge = new WeakHashMap<Player, Float>();
	protected WeakHashMap<Player, Long> _chargeStart = new WeakHashMap<Player, Long>();
	
	protected float _rateBase;
	protected float _rateBoost;
	
	protected float _energyPerCharge = 0;
	
	public SkillCharge(SkillFactory skills, String name, ClassType classType,
			SkillType skillType, int cost, int maxLevel,
			float base, float boost)
	{
		super(skills, name, classType, skillType, cost, maxLevel);
		
		_rateBase = base;
		_rateBoost = boost;
	}

	public boolean Charge(Player player)
	{
		//Level
		int level = getLevel(player);
		if (level == 0)		
			return false;

		//Add & Recharge	
		if (!_charge.containsKey(player))
			_charge.put(player, 0f);

		float charge = _charge.get(player);
		
		if (charge >= 1)
		{
			//Display
			DisplayProgress(player, GetName(level), charge);
			return true;
		}
			
		//Energy
		if (_energyPerCharge > 0)
		{
			if (!Factory.Energy().Use(player, GetName(), _energyPerCharge, true, false))
			{
				//Display
				DisplayProgress(player, GetName(level), charge);
				return true;
			}
		}

		//Increase Charge
		charge = Math.min(1f, charge + _rateBase + (_rateBoost * level));
		_charge.put(player, charge);

		//Display
		DisplayProgress(player, GetName(level), charge);
		
		return charge >= 1;
	}

	public float GetCharge(Player player)
	{
		if (!_charge.containsKey(player))
			return 0f;

		return _charge.get(player);
	}
	
	public void Reset(Player player)
	{
		_charge.remove(player);
		_chargeStart.remove(player);
	}

	public String GetChargeString()
	{
		return "Charges #"+(int)(0.02*2000)+"#"+(int)(0.005*2000)+" % per Second.";
	}
}
