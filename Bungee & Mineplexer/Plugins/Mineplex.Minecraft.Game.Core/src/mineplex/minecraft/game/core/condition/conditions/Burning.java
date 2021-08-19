package mineplex.minecraft.game.core.condition.conditions;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class Burning extends Condition
{
	public Burning(ConditionManager manager, String reason, LivingEntity ent,
			LivingEntity source, ConditionType type, int mult, int ticks,
			boolean add, Material visualType, byte visualData,
			boolean showIndicator) 
	{
		super(manager, reason, ent, source, type, mult, ticks, add, visualType,
				visualData, showIndicator, false);
	}

	@Override
	public void Add() 
	{
	}

	@Override
	public void Remove() 
	{
		
	}
	
	@Override
	public void OnConditionAdd()
	{
		if (_ent.getFireTicks() > 0 && IsAdd())
			_ent.setFireTicks(_ent.getFireTicks() + _ticksTotal);
		else if (_ent.getFireTicks() < _ticksTotal)
			_ent.setFireTicks(_ticksTotal);
	}
}