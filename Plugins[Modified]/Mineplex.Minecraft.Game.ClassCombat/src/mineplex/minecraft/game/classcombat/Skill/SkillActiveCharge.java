package mineplex.minecraft.game.classcombat.Skill;

import java.util.WeakHashMap;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public abstract class SkillActiveCharge extends SkillActive
{
	protected WeakHashMap<Player, Float> _charge = new WeakHashMap<Player, Float>();

	protected float _rateBase;
	protected float _rateBoost;

	public SkillActiveCharge(SkillFactory skills, String name,
			ClassType classType, SkillType skillType, int cost, int levels,
			int energy, int energyMod, long recharge, long rechargeMod,
			boolean rechargeInform, Material[] itemArray, Action[] actionArray,
			float base, float boost)
	{
		super(skills, name, classType, skillType, cost, levels, energy, energyMod,
				recharge, rechargeMod, rechargeInform, itemArray, actionArray);
		
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
	}

	public String GetChargeString()
	{
		return "Charges #"+(int)(0.02*2000)+"#"+(int)(0.005*2000)+" % per Second.";
	}
}
